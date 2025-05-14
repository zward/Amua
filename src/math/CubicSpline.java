/**
 * Amua - An open source modeling framework.
 * Copyright (C) 2017 Zachary J. Ward
 *
 * This file is part of Amua. Amua is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Amua is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Amua.  If not, see <http://www.gnu.org/licenses/>.
 */

package math;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class CubicSpline{

	public double knots[];
	public double knotHeights[];
	public int numSplines;
	/**
	 * [Spline #][Coeff]
	 */
	public double splineCoeffs[][];
	/**
	 * 0=Natural, 1=Clamped, 2=Not-a-knot, 3=Periodic
	 */
	public int boundaryCondition;

	public CubicSpline(double data[][], int yCol, String boundary){
		//old(data,yCol);
		
		int numPoints=data.length;
		knots=new double[numPoints];
		knotHeights=new double[numPoints];
		for(int i=0; i<numPoints; i++){
			knots[i]=data[i][0];
			knotHeights[i]=data[i][yCol];
		}

		numSplines=numPoints-1;
		splineCoeffs=new double[numSplines][4];
		if(boundary.matches("Natural")){boundaryCondition=0;}
		else if(boundary.matches("Clamped")){boundaryCondition=1;}
		else if(boundary.matches("Not-a-knot")){boundaryCondition=2;}
		else if(boundary.matches("Periodic")){boundaryCondition=3;}
		
		//Intercept (a0)
		for(int i=0; i<numSplines; i++){
			splineCoeffs[i][0]=data[i][yCol]; //starting function value
		}
		
		//Construct linear system
		int numCoeffs=numSplines*3;
		double matrix[][]=new double[numCoeffs][numCoeffs];
		double constraints[]=new double[numCoeffs];
		
		//f(x) constraints - ensure each spline passes through the function at the right-hand knot
		int row=0, col=0;
		for(int i=0; i<numSplines; i++){ //For each spline
			double h=knots[i+1]-knots[i];
			constraints[row]=data[i+1][yCol]-splineCoeffs[i][0]; //y1-a0
			matrix[row][col]=h; //a1
			matrix[row][col+1]=h*h; //a2
			matrix[row][col+2]=h*h*h; //a3
			row++;
			col+=3;
		}
		
		//f'(x) constraints - ensure equal first derivative at internal points
		col=0;
		for(int i=0; i<numSplines-1; i++){
			double h=knots[i+1]-knots[i];
			constraints[row]=0;
			matrix[row][col]=1; //a1
			matrix[row][col+1]=2*h; //a2
			matrix[row][col+2]=3*h*h; //a3
			matrix[row][col+3]=-1; //b1
			row++;
			col+=3;
		}
		
		//f''(x) constraints - ensure equal second derivative at internal points
		col=0;
		for(int i=0; i<numSplines-1; i++){
			double h=knots[i+1]-knots[i];
			constraints[row]=0;
			matrix[row][col]=0; //a1
			matrix[row][col+1]=2; //a2 (was 1)
			matrix[row][col+2]=6*h; //a3 (was 3*h)
			matrix[row][col+3]=0; //b1
			matrix[row][col+4]=-2; //b2 (was -1)
			row++;
			col+=3;
		}
		
		//Boundary conditions
		if(boundaryCondition==0){ //natural - f''=0 at boundaries
			//left
			constraints[row]=0;
			matrix[row][1]=2; //a2
			row++;
			//right
			double h=knots[numSplines]-knots[numSplines-1];
			constraints[row]=0;
			matrix[row][numCoeffs-2]=2; //a2 (was 1)
			matrix[row][numCoeffs-1]=6*h; //a3 (was 3*h)
		}
		else if(boundaryCondition==1){ //clamped - fixed f' at boundaries
			//left
			constraints[row]=0;
			matrix[row][0]=1; //a1
			row++;
			//right
			double h=knots[numSplines]-knots[numSplines-1];
			constraints[row]=0;
			matrix[row][numCoeffs-3]=1;
			matrix[row][numCoeffs-2]=2*h;
			matrix[row][numCoeffs-1]=3*h*h;
		}
		else if(boundaryCondition==2){ //Not-a-knot - f''' equal at outermost inner points
			//left
			constraints[row]=0;
			matrix[row][2]=6; //a3
			matrix[row][5]=-6; //b3
			row++;
			//right
			constraints[row]=0;
			matrix[row][numCoeffs-4]=6; //a3
			matrix[row][numCoeffs-1]=6; //a3
		}
		else if(boundaryCondition==3){ //Periodic - f' and f'' equal at boundaries
			double h=knots[numSplines]-knots[numSplines-1];
			//f'
			constraints[row]=0;
			matrix[row][0]=1; //a1
			matrix[row][numCoeffs-3]=-1; //b1
			matrix[row][numCoeffs-2]=-2*h; //b2
			matrix[row][numCoeffs-1]=-3*h*h; //b3
			row++;
			//f''
			constraints[row]=0;
			matrix[row][0]=0; //a1
			matrix[row][1]=2; //a2
			matrix[row][numCoeffs-2]=-2; //b2
			matrix[row][numCoeffs-1]=-6*h; //b3
		}
		
		//Solve system of equations
		RealMatrix A = MatrixUtils.createRealMatrix(matrix);
		DecompositionSolver solver = new LUDecomposition(A).getSolver();
		RealVector b=new ArrayRealVector(constraints);
		RealVector solution=solver.solve(b);

		row=0;
		for(int i=0; i<numSplines; i++){
			for(int j=1; j<4; j++){ //a1-a3
				splineCoeffs[i][j]=solution.getEntry(row);
				row++;
			}
		}
		
	}
	
	public double evaluate(double x){
		double y=Double.NaN;
		//Find domain
		int index=-1;
		if(x<knots[0]){ //Extrapolate left
			x=x-knots[0];
			double a[]=splineCoeffs[0];
			if(boundaryCondition==0 || boundaryCondition==1){ //Natural or clamped
				double slope=a[1];
				y=slope*x+knotHeights[0];
			}
			else{ //Not-a-knot or periodic
				index=0;
				y=splineCoeffs[index][0]+splineCoeffs[index][1]*x+splineCoeffs[index][2]*x*x+splineCoeffs[index][3]*x*x*x;
			}
		}
		else if(x>knots[numSplines]){ //Extrapolate right
			double a[]=splineCoeffs[numSplines-1];
			if(boundaryCondition==0 || boundaryCondition==1){ //Natural or clamped
				x=x-knots[numSplines];
				double h=knots[numSplines]-knots[numSplines-1];
				double slope=a[1]+2*a[2]*h+3*a[3]*h*h;
				y=slope*x+knotHeights[numSplines];
			}
			else{ //Not-a-knot or periodic
				index=numSplines-1;
				x=x-knots[index];
				y=splineCoeffs[index][0]+splineCoeffs[index][1]*x+splineCoeffs[index][2]*x*x+splineCoeffs[index][3]*x*x*x;
			}
		}
		else{ //Interpolate
			index=0;
			while(x>knots[index+1] && index<numSplines-1){index++;}
			x=x-knots[index];
			y=splineCoeffs[index][0]+splineCoeffs[index][1]*x+splineCoeffs[index][2]*x*x+splineCoeffs[index][3]*x*x*x;
		}
		return(y);
	}
	
	

}