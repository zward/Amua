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

package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.GumbelDistribution;
import org.apache.commons.math3.distribution.LaplaceDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.LogisticDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.distribution.TriangularDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.special.Gamma;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;

public class VarHelper{
	String breaks[];
	ArrayList<String> functions, dists;
	ArrayList<TreeVariable> variables; //Link to tree.variables
	MersenneTwisterFast generator;
	int samplingStrategy=0; //0=All independent, 1=Linked-Variable, 2=Linked-Across variables
	double curRand;
	Function min,max,bound;
	ErrorLog errorLog;

	/**
	 * Default constructor
	 */
	public VarHelper(ArrayList<TreeVariable> variables, String dimSymbols[], ErrorLog errorLog){
		this.variables=variables;
		this.errorLog=errorLog;
		breaks=new String[]{" ",",",";","(",")","[","]","+","-","/","*","^","%"};
		/*int length=breaks.length;
		breaks=Arrays.copyOf(breaks, length+dimSymbols.length);
		for(int i=0; i<dimSymbols.length; i++){
			breaks[length+i]=dimSymbols[i];
		}*/

		functions=new ArrayList<String>();
		functions.add("abs"); //absolute value
		functions.add("acos"); // arc cosine
		functions.add("asin"); // arc sine
		functions.add("atan"); // arc tangent
		functions.add("cbrt"); // cubic root
		functions.add("ceil"); // nearest upper integer
		functions.add("cos"); // cosine
		functions.add("cosh"); // hyperbolic cosine
		functions.add("exp"); // euler's number raised to the power (e^x)
		functions.add("floor");// nearest lower integer
		functions.add("log"); // logarithmus naturalis (base e)
		functions.add("log10"); // logarithm (base 10)
		functions.add("log2"); // logarithm (base 2)
		functions.add("sin"); // sine
		functions.add("sinh"); // hyperbolic sine
		functions.add("sqrt"); // square root
		functions.add("tan"); //tangent
		functions.add("tanh"); //hyperbolic tangent
		functions.add("signum"); //signum function
		//Constants
		functions.add("pi");
		functions.add("π");
		functions.add("e");
		functions.add("φ"); //Golden ratio

		dists=new ArrayList<String>();
		//Discrete
		dists.add("Bin");
		dists.add("DUnif");
		dists.add("Geom");
		dists.add("HGeom");
		dists.add("NBin");
		dists.add("Pois");
		dists.add("Zipf");
		//Continuous
		dists.add("Beta");
		dists.add("Expo");
		dists.add("Gam");
		dists.add("Gumb");
		dists.add("Lap");
		dists.add("Logi");
		dists.add("LogN");
		dists.add("Norm");
		dists.add("Par");
		dists.add("Tri");
		dists.add("TriBe");
		dists.add("Unif");
		dists.add("Weib");
		//Truncated
		dists.add("tNorm");

		//Custom functions
		functions.add("min");
		min = new Function("min",2) {
			@Override
			public double apply(double... args) {
				return(Math.min(args[0], args[1]));
			}
		};

		functions.add("max");
		max = new Function("max",2) {
			@Override
			public double apply(double... args) {
				return(Math.max(args[0], args[1]));
			}
		};

		functions.add("bound");
		bound = new Function("bound",3) {
			@Override
			public double apply(double... args) {
				double val=args[0];
				if(val<args[1]){val=args[1];} //min
				if(val>args[2]){val=args[2];} //max
				return(val);
			}
		};
	}

	public boolean isReserved(String test){
		boolean reserved=false;
		if(functions.contains(test)){reserved=true;}
		else if(dists.contains(test)){reserved=true;}
		else if(test.matches("c") || test.matches("C") || test.matches("x") || test.matches("f") || test.matches("F")){reserved=true;}
		return(reserved);
	}

	public void updateBreaks(String dimSymbols[]){
		/*breaks=new String[]{" ",",",";","(",")","[","]","+","-","/","*","^","%"};
		int length=breaks.length;
		breaks=Arrays.copyOf(breaks, length+dimSymbols.length);
		for(int i=0; i<dimSymbols.length; i++){
			breaks[length+i]=dimSymbols[i];
		}*/
	}

	public int getVariableIndex(String name){
		int varIndex=-1;
		int numVars=variables.size();
		int i=0;
		while(varIndex==-1 && i<numVars){
			if(variables.get(i).name.matches(name)){varIndex=i;}
			i++;
		}
		return(varIndex);
	}

	public int getNextIndex(String text){
		int index=text.length();
		for(int i=0; i<breaks.length; i++){
			int curIndex=text.indexOf(breaks[i]);
			if(curIndex!=-1){
				index=Math.min(index,curIndex);
			}
		}
		return(index);
	}

	public boolean isBreak(String curChar){
		boolean curBreak=false;
		int i=0;
		while(curBreak==false && i<breaks.length){
			if(curChar.equals(breaks[i])){curBreak=true;}
			i++;
		}
		return(curBreak);
	}

	public boolean isVariable(String word){
		return(getVariableIndex(word)!=-1);
	}

	public boolean isFunction(String word){
		return(functions.contains(word));
	}

	public boolean isDistribution(String word){
		return(dists.contains(word));
	}

	public boolean isItalics(String test){
		boolean italics=false;
		if(test.matches("c") || test.matches("C") || test.matches("x") || test.matches("f") || test.matches("F")){italics=true;}
		return(italics);
	}

	public double evaluateExpression(String expression){
		double result=0;

		String curText=expression;
		
		//Parse expression word by word
		ArrayList<String> varNames=new ArrayList<String>();
		ArrayList<Double> varVals=new ArrayList<Double>();

		int len=curText.length();
		while(len>0){
			int index=getNextIndex(curText);
			String word=curText.substring(0, index);
			//See if variable
			int varIndex=getVariableIndex(word);
			if(varIndex!=-1){
				TreeVariable curVar=variables.get(varIndex);
				if(!varNames.contains(curVar.name)){ //Add unique vars
					varNames.add(curVar.name);
					varVals.add(curVar.value);
				}
				if(curVar.valid==false){
					return(Double.NaN); //Throw error
				}
			}
			else if(isDistribution(word)){ //Check for distribution, replace with value
				//Get parameters
				int index1=curText.indexOf("(");
				int index2=curText.indexOf(")");
				String strParams=curText.substring(index1+1,index2).replaceAll(" ", "");
				String params[]=strParams.split(",");
				//Replace variables with values
				for(int i=0; i<params.length; i++){
					varIndex=getVariableIndex(params[i]);
					if(varIndex!=-1){params[i]=variables.get(varIndex).value+"";}
				}

				String val=getDistValue(word,params)+"";
				String rep=curText.substring(0,index2+1);
				rep=rep.replaceAll("\\(", "\\\\(");
				rep=rep.replaceAll("\\)", "\\\\)");
				expression=expression.replaceAll(rep, val);
				index=index2; //Move to end of dist parameters
			}

			if(index==len){len=0;} //End of word
			else{
				curText=curText.substring(index+1);
				len=curText.length();
			}
		}

		ExpressionBuilder eb=new ExpressionBuilder(expression);
		//Add custom functions
		eb.function(min); eb.function(max); eb.function(bound);

		int numVars=varNames.size();
		if(numVars>0){eb.variables(new HashSet<String>(varNames));}
		Expression ex=eb.build();
		for(int v=0; v<numVars; v++){ex.setVariable(varNames.get(v), varVals.get(v));}
		result=ex.evaluate();

		return(result);
	}

	public double sampleExpression(String expression){
		double result=0;
		String curText=expression;
		
		//Parse expression word by word
		ArrayList<String> varNames=new ArrayList<String>();
		ArrayList<Double> varVals=new ArrayList<Double>();

		int len=curText.length();
		while(len>0){
			int index=getNextIndex(curText);
			String word=curText.substring(0, index);
			//See if variable
			int varIndex=getVariableIndex(word);
			if(varIndex!=-1){
				TreeVariable curVar=variables.get(varIndex);
				if(!varNames.contains(curVar.name)){ //Add unique vars
					varNames.add(curVar.name);
					varVals.add(curVar.value);
				}
			}
			else if(isDistribution(word)){ //Check for distribution, replace with value
				//Get parameters
				int index1=curText.indexOf("(");
				int index2=curText.indexOf(")");
				String strParams=curText.substring(index1+1,index2).replaceAll(" ", "");
				String params[]=strParams.split(",");
				//Replace variables with values
				for(int i=0; i<params.length; i++){
					varIndex=getVariableIndex(params[i]);
					if(varIndex!=-1){params[i]=variables.get(varIndex).value+"";}
				}

				double rand;
				if(samplingStrategy==0){rand=generator.nextDouble();}
				else{rand=curRand;}

				String val=sampleDistValue(word,params,rand)+"";
				String rep=curText.substring(0,index2+1);
				rep=rep.replaceAll("\\(", "\\\\(");
				rep=rep.replaceAll("\\)", "\\\\)");
				expression=expression.replaceAll(rep, val);
				index=index2; //Move to end of dist parameters
			}

			if(index==len){len=0;} //End of word
			else{
				curText=curText.substring(index+1);
				len=curText.length();
			}
		}

		ExpressionBuilder eb=new ExpressionBuilder(expression);
		eb.function(min); eb.function(max); eb.function(bound);

		int numVars=varNames.size();
		if(numVars>0){eb.variables(new HashSet<String>(varNames));}
		Expression ex=eb.build();
		for(int v=0; v<numVars; v++){ex.setVariable(varNames.get(v), varVals.get(v));}
		result=ex.evaluate();

		return(result);
	}

	public boolean containsVar(String varName, String text){
		boolean contains=false;
		String curText=text;
		int len=curText.length();
		while(contains==false && len>0){
			int index=getNextIndex(curText);
			String word=curText.substring(0, index);
			//See if matches variable
			if(word.matches(varName)){contains=true;}
			if(index==len){len=0;} //End of word
			else{
				curText=curText.substring(index+1);
				len=curText.length();
			}
		}
		return(contains);
	}

	public double getDistValue(String dist, String params[]){
		switch(dist){
		//Discrete
		case "Bin": {
			if(params.length==2){ //(n,p) - return mean
				int n=Integer.parseInt(params[0]);
				double p=Double.parseDouble(params[1]);
				return(n*p); //E(n,p)=np
			}
			else if(params.length==4){ //(k,n,p,f): Prob of k successes out of n trials with prob p
				int k=(int)Double.parseDouble(params[0]), n=Integer.parseInt(params[1]); //Cast k to int to avoid errors from plotFx
				double p=Double.parseDouble(params[2]), val=0;
				if(params[3].equals("f")){val=bin(k,n,p);}//PMF
				else if(params[3].equals("F")){for(int i=0; i<=k; i++){val+=bin(i,n,p);}} //CDF
				else{val=Double.NaN;} //Throw error
				return(val);
			}
			else{return(Double.NaN);} //Throw error
		}
		case "DUnif": { 
			if(params.length==2){ //(a,b) - return mean
				double a=Double.parseDouble(params[0]), b=Double.parseDouble(params[1]);
				return((a+b)/2.0);
			}
			else if(params.length==4){ //(k,a,b,f)
				int k=(int)Double.parseDouble(params[0]), a=Integer.parseInt(params[1]), b=Integer.parseInt(params[2]);
				double val=0;
				if(params[3].equals("f")){ //PMF
					if(k<a || k>b){val=0;}
					else{val=1.0/((b-a+1)*1.0);}
				}
				else if(params[3].equals("F")){ //CDF
					if(k<a){val=0;}
					else if(k>=b){val=1;}
					else{val=(k-a+1)/((b-a+1)*1.0);}
				}
				else{val=Double.NaN;} //Throw error
				return(val);
			}
			else{return(Double.NaN);} //Throw error
		}
		case "Geom": {
			if(params.length==1){ //(p) - return mean
				double p=Double.parseDouble(params[0]);
				return((1.0-p)/p); //E(p)=(1-p)/p
			}
			else if(params.length==3){ //(k,p,f): Prob of failure after k trials
				int k=(int)Double.parseDouble(params[0]); //Cast k to int to avoid errors from plotFx
				double p=Double.parseDouble(params[1]);
				double val=0;
				if(params[2].equals("f")){val=Math.pow(1.0-p, k)*p;} //PMF
				else if(params[2].equals("F")){for(int i=0; i<=k; i++){val+=Math.pow(1.0-p, i)*p;}} //CDF
				else{val=Double.NaN;} //Throw error
				return(val);
			}
			else{return(Double.NaN);} //Throw error
		}
		case "HGeom": {
			if(params.length==3){
				int w=Integer.parseInt(params[0]), b=Integer.parseInt(params[1]), n=Integer.parseInt(params[2]);
				return((n*w)/((w+b)*1.0)); //E(w,b,n)=(nw)/(w+b)
			}
			else if(params.length==5){
				int k=(int)Double.parseDouble(params[0]), w=Integer.parseInt(params[1]), b=Integer.parseInt(params[2]), n=Integer.parseInt(params[3]);
				double val=0;
				if(params[4].equals("f")){val=(choose(w,k)*choose(b,n-k))/(choose(w+b,n)*1.0);}
				else if(params[4].equals("F")){
					for(int i=0; i<=k; i++){
						val+=(choose(w,i)*choose(b,n-i))/(choose(w+b,n)*1.0);
					}
				}
				else{val=Double.NaN;} //Throw error
				return(val);
			}
			else{return(Double.NaN);} //Throw error
		}
		case "NBin": {
			if(params.length==2){ //(r,p) - return mean
				int r=Integer.parseInt(params[0]);
				double p=Double.parseDouble(params[1]);
				return((r*(1-p))/p); //E(r,p)=[r(1-p)]/p
			}
			else if(params.length==4){ //(k,r,p,f)
				int k=(int)Double.parseDouble(params[0]); //Cast to int to avoid errors in plotFx
				int r=Integer.parseInt(params[1]);
				double p=Double.parseDouble(params[2]);
				if(params[3].equals("f")){ //PMF
					return(choose(r+k-1,r-1)*Math.pow(p, r)*Math.pow(1-p, k));
				}
				else if(params[3].equals("F")){ //CDF
					double val=0;
					for(int i=0; i<=k; i++){
						val+=(choose(r+k-1,r-1)*Math.pow(p, r)*Math.pow(1-p, k));
					}
					return(val);
				}
				else{return(Double.NaN);} //Throw error
			}
			else{return(Double.NaN);} //Throw error
		}
		case "Pois": {
			if(params.length==1){ //lambda - return mean
				double lambda=Double.parseDouble(params[0]);
				return(lambda);
			}
			else if(params.length==3){ //(k, lambda,f): PMF of Pois(lambda) at k
				int k=(int)Double.parseDouble(params[0]); //Cast double to integer to avoid parsing errors from plotFx
				double lambda=Double.parseDouble(params[1]);
				double val=0;
				if(params[2].equals("f")){val=Math.exp(k*Math.log(lambda)-lambda-Gamma.logGamma(k+1));}
				else if(params[2].equals("F")){
					for(int i=0; i<=k; i++){
						val+=Math.exp(i*Math.log(lambda)-lambda-Gamma.logGamma(i+1));
					}
				}
				else{val=Double.NaN;}
				return(val);
			}
			else{return(Double.NaN);} //Throw error
		}
		case "Zipf": {
			if(params.length==2){ //return mean
				double s=Double.parseDouble(params[0]); int n=Integer.parseInt(params[1]);
				ZipfDistribution zipf=new ZipfDistribution(null,n,s);
				return(zipf.getNumericalMean());
			}
			else if(params.length==4){ //(k,s,n,f): PMF at k
				int k=(int)Double.parseDouble(params[0]); //Cast to int to avoid errors in plotFx
				double s=Double.parseDouble(params[1]); int n=Integer.parseInt(params[2]);
				ZipfDistribution zipf=new ZipfDistribution(null,n,s);
				if(params[3].equals("f")){return(zipf.probability(k));} //PMF
				else if(params[3].equals("F")){return(zipf.cumulativeProbability(k));} //CDF
				else{return(Double.NaN);} //Throw error
			}
			else{return(Double.NaN);} //Throw error
		}

		//Continuous
		case "Beta": {
			if(params.length==2){ //return mean
				double a=Double.parseDouble(params[0]), b=Double.parseDouble(params[1]);
				return(a/(a+b)); //a,b
			}
			else if(params.length==4){ //(x,a,b,f)
				double x=Double.parseDouble(params[0]), a=Double.parseDouble(params[1]), b=Double.parseDouble(params[2]);
				BetaDistribution beta=new BetaDistribution(null,a,b);
				if(params[3].equals("f")){return(beta.density(x));} //PDF
				else if(params[3].equals("F")){return(beta.cumulativeProbability(x));} //CDF
				else{return(Double.NaN);} //Throw error
			}
			else{return(Double.NaN);} //Throw error
		}
		case "Expo": {
			if(params.length==1){ //lambda - return mean
				double lambda=Double.parseDouble(params[0]);
				return(1.0/lambda);
			}
			else if(params.length==3){//(x,lambda,f): 
				double x=Double.parseDouble(params[0]), lambda=Double.parseDouble(params[1]);
				if(params[2].equals("f")){return(lambda*Math.exp(-lambda*x));} //PDF
				else if(params[2].equals("F")){return(1.0-Math.exp(-lambda*x));} //CDF
				else{return(Double.NaN);} //Throw error
			}
			else{return(Double.NaN);} //Throw error
		}
		case "Gam":{
			if(params.length==2){
				double k=Double.parseDouble(params[0]), theta=Double.parseDouble(params[1]);
				return(k*theta); //mean
			}
			else if(params.length==4){ //(x,k,theta,f)
				double x=Double.parseDouble(params[0]), k=Double.parseDouble(params[1]), theta=Double.parseDouble(params[2]);
				GammaDistribution gamma=new GammaDistribution(null,k,theta);
				if(params[3].equals("f")){return(gamma.density(x));} //PDF
				else if(params[3].equals("F")){return(gamma.cumulativeProbability(x));} //CDF
				else{return(Double.NaN);} //Throw error
			}
			else{return(Double.NaN);} //Throw error
		}
		case "Gumb":{
			if(params.length==2){
				double mu=Double.parseDouble(params[0]), beta=Double.parseDouble(params[1]);
				GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
				return(gumbel.getNumericalMean()); //mean
			}
			else if(params.length==4){ //(x,mu,beta,f)
				double x=Double.parseDouble(params[0]), mu=Double.parseDouble(params[1]), beta=Double.parseDouble(params[2]);
				GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
				if(params[3].equals("f")){return(gumbel.density(x));} //PDF
				else if(params[3].equals("F")){return(gumbel.cumulativeProbability(x));} //CDF
				else{return(Double.NaN);} //Throw error
			}
			else{return(Double.NaN);} //Throw error
		}
		case "Lap": {
			if(params.length==2){ //return mean
				double mu=Double.parseDouble(params[0]);
				return(mu); 
			}
			else if(params.length==4){ // (x,mu,beta,f)
				double x=Double.parseDouble(params[0]), mu=Double.parseDouble(params[1]), beta=Double.parseDouble(params[2]);
				LaplaceDistribution lap=new LaplaceDistribution(null,mu,beta);
				if(params[3].equals("f")){return(lap.density(x));} //PDF
				else if(params[3].equals("F")){return(lap.cumulativeProbability(x));} //CDF
				else{return(Double.NaN);} //Throw error
			}
			else{return(Double.NaN);} //Throw error
		}
		case "Logi": {
			if(params.length==2){ //return mean
				double mu=Double.parseDouble(params[0]);
				return(mu); 
			}
			else if(params.length==4){ //(x,mu,s,f)
				double x=Double.parseDouble(params[0]), mu=Double.parseDouble(params[1]), s=Double.parseDouble(params[2]);
				LogisticDistribution logi=new LogisticDistribution(null,mu,s);
				if(params[3].equals("f")){return(logi.density(x));} //PDF
				else if(params[3].equals("F")){return(logi.cumulativeProbability(x));} //CDF
				else{return(Double.NaN);} //Throw error
			}
			else{return(Double.NaN);} //Throw error
		}
		case "LogN": {
			if(params.length==2){ //return mean
				double mu=Double.parseDouble(params[0]), sigma=Double.parseDouble(params[1]);
				return(Math.exp(mu+sigma/2.0)); //exp(mu+sigmaSq/2.0)
			}
			else if(params.length==4){ //(x,mu,sigma,f)
				double x=Double.parseDouble(params[0]), mu=Double.parseDouble(params[1]), sigma=Double.parseDouble(params[2]);
				LogNormalDistribution lnorm=new LogNormalDistribution(null,mu,sigma);
				if(params[3].equals("f")){return(lnorm.density(x));} //PDF
				else if(params[3].equals("F")){return(lnorm.cumulativeProbability(x));} //CDF
				else{return(Double.NaN);} //Throw error
			}
			else{return(Double.NaN);} //Throw error
		}
		case "Norm": {
			if(params.length==2){ //return mean
				double mu=Double.parseDouble(params[0]); //sigma=Double.parseDouble(params[1]);
				return(mu);
			}
			else if(params.length==4){ //(x,mu,sigma,f)
				double x=Double.parseDouble(params[0]), mu=Double.parseDouble(params[1]), sigma=Double.parseDouble(params[2]);
				NormalDistribution norm=new NormalDistribution(null,mu,sigma);
				if(params[3].equals("f")){return(norm.density(x));} //PDF
				else if(params[3].equals("F")){return(norm.cumulativeProbability(x));} //CDF
				else{return(Double.NaN);} //Throw error
			}
			else{return(Double.NaN);} //Throw error
		}
		case "Par": {
			if(params.length==2){ //return mean
				double k=Double.parseDouble(params[0]), alpha=Double.parseDouble(params[1]); 
				if(alpha<=1){return(Double.POSITIVE_INFINITY);}
				else{return((alpha*k)/(alpha-1));}
			}
			else if(params.length==4){ //(x,k,alpha,f)
				double x=Double.parseDouble(params[0]), k=Double.parseDouble(params[1]), alpha=Double.parseDouble(params[2]);
				ParetoDistribution par=new ParetoDistribution(null,k,alpha);
				if(params[3].equals("f")){return(par.density(x));} //PDF
				else if(params[3].equals("F")){return(par.cumulativeProbability(x));} //CDF
				else{return(Double.NaN);} //Throw error
			}
			else{return(Double.NaN);} //Throw error
		}
		case "Tri": {
			if(params.length==3){ //return mean
				double a=Double.parseDouble(params[0]), b=Double.parseDouble(params[1]), c=Double.parseDouble(params[2]); 
				return((a+b+c)/3.0);
			}
			else if(params.length==5){ //(x,a,b,c,f)
				double x=Double.parseDouble(params[0]), a=Double.parseDouble(params[1]), b=Double.parseDouble(params[2]), c=Double.parseDouble(params[3]);
				TriangularDistribution tri=new TriangularDistribution(null,a,c,b);
				if(params[4].equals("f")){return(tri.density(x));} //PDF
				else if(params[4].equals("F")){return(tri.cumulativeProbability(x));} //CDF
				else{return(Double.NaN);} //Throw error
			}
			else{return(Double.NaN);} //Throw error
		}
		case "TriBe": {
			if(params.length==3){ //return mean
				double a=Double.parseDouble(params[0]), b=Double.parseDouble(params[1]), c=Double.parseDouble(params[2]);
				double u=(a+(4.0*c)+b)/6.0; //Weighted mean, mode is worth 4x
				double a1=((u-a)*(2*c-a-b))/((c-u)*(b-a));
				double a2=(a1*(b-u))/(u-a);
				if(c==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
				double mean=a1/(a1+a2); //Mean between 0 and 1
				mean=a+(b-a)*mean; //Re-scale back to original scale
				return(mean);
			}
			else if(params.length==5){ //(x,a,b,c,f)
				double x=Double.parseDouble(params[0]), a=Double.parseDouble(params[1]), b=Double.parseDouble(params[2]), c=Double.parseDouble(params[3]);
				double u=(a+(4.0*c)+b)/6.0; //Weighted mean, mode is worth 4x
				double a1=((u-a)*(2*c-a-b))/((c-u)*(b-a));
				double a2=(a1*(b-u))/(u-a);
				if(c==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
				x=(x-a)/(b-a); //Transform x to 0,1
				BetaDistribution beta=new BetaDistribution(null,a1,a2);
				if(params[4].equals("f")){return(beta.density(x)/(b-a));} //PDF, rescaled to range
				else if(params[4].equals("F")){return(beta.cumulativeProbability(x));} //CDF
				else{return(Double.NaN);} //Throw error
			}
			else{return(Double.NaN);} //Throw error
		}
		case "Unif": {
			if(params.length==2){ //return mean
				double a=Double.parseDouble(params[0]), b=Double.parseDouble(params[1]);
				return((a+b)/2.0);
			}
			else if(params.length==4){ //(x,a,b,f)
				double x=Double.parseDouble(params[0]), a=Double.parseDouble(params[1]), b=Double.parseDouble(params[2]);
				UniformRealDistribution uni=new UniformRealDistribution(null,a,b);
				if(params[3].equals("f")){return(uni.density(x));} //PDF
				else if(params[3].equals("F")){return(uni.cumulativeProbability(x));} //CDF
				else{return(Double.NaN);} //Throw error
			}
			else{return(Double.NaN);} //Throw error
		}
		case "Weib": {
			if(params.length==2){
				double a=Double.parseDouble(params[0]), b=Double.parseDouble(params[1]);
				WeibullDistribution weib=new WeibullDistribution(null,a,b);
				return(weib.getNumericalMean()); //mean
			}
			else if(params.length==4){ //(x,a,b,f)
				double x=Double.parseDouble(params[0]), a=Double.parseDouble(params[1]), b=Double.parseDouble(params[2]);
				WeibullDistribution weib=new WeibullDistribution(null,a,b);
				if(params[3].equals("f")){return(weib.density(x));} //PDF
				else if(params[3].equals("F")){return(weib.cumulativeProbability(x));} //CDF
				else{return(Double.NaN);} //Throw error
			}
			else{return(Double.NaN);} //Throw error
		}

		//Truncated
		case "tNorm": {
			if(params.length==4){ //(mu,sigma,a,b) return mean
				double mu=Double.parseDouble(params[0]), sigma=Double.parseDouble(params[1]), a=Double.parseDouble(params[2]), b=Double.parseDouble(params[3]);
				double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
				NormalDistribution norm=new NormalDistribution(null,0,1); //Std normal
				double z=norm.cumulativeProbability(beta)-norm.cumulativeProbability(alpha);
				double phiA=norm.density(alpha), phiB=norm.density(beta);
				double mean=mu+((phiA-phiB)/z)*sigma;
				return(mean);
			}
			else if(params.length==6){ //(x,mu,sigma,a,b,f)
				double x=Double.parseDouble(params[0]), mu=Double.parseDouble(params[1]), sigma=Double.parseDouble(params[2]), a=Double.parseDouble(params[3]), b=Double.parseDouble(params[4]);
				NormalDistribution norm=new NormalDistribution(null,0,1); //Std normal
				if(params[5].equals("f")){ //PDF
					if(x<a || x>b){return(0);}
					else{
						double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
						double z=norm.cumulativeProbability(beta)-norm.cumulativeProbability(alpha);
						double xi=(x-mu)/sigma;
						double phiXi=norm.density(xi);
						return(phiXi/(sigma*z));
					}
				}
				else if(params[5].equals("F")){ //CDF
					if(x<a){return(0);}
					else if(x>b){return(1);}
					else{
						double alpha=(a-mu)/sigma, beta=(b-mu)/sigma;
						double z=norm.cumulativeProbability(beta)-norm.cumulativeProbability(alpha);
						double xi=(x-mu)/sigma;
						double PHIxi=norm.cumulativeProbability(xi);
						double PHIa=norm.cumulativeProbability(alpha);
						return((PHIxi-PHIa)/z);
					}
				}
				else{return(Double.NaN);} //Throw error
			}
			else{return(Double.NaN);} //Throw error
		}

		} //End switch
		return(0);
	}

	public double sampleDistValue(String dist, String params[],double rand){
		switch(dist){
		//Discrete
		case "Bin": {
			if(params.length==2){
				int n=Integer.parseInt(params[0]), k=-1;
				double p=Double.parseDouble(params[1]), cdf=0;
				while(rand>cdf){
					cdf+=bin(k+1,n,p);
					k++;
				}
				return(k);
			}
			else{return(getDistValue(dist,params));}
		}
		case "DUnif": {
			if(params.length==2){
				int a=Integer.parseInt(params[0]), b=Integer.parseInt(params[1]);
				double val=a+rand*(b+1-a);
				return(Math.floor(val));
			}
			else{return(getDistValue(dist,params));}
		}
		case "Geom": {
			if(params.length==1){
				double p=Double.parseDouble(params[0]), cdf=0;
				int k=-1;
				while(rand>cdf){
					cdf+=Math.pow(1.0-p, k+1)*p;
					k++;
				}
				return(k);
			}
			else{return(getDistValue(dist,params));}
		}
		case "HGeom": {
			if(params.length==3){
				int w=Integer.parseInt(params[0]), b=Integer.parseInt(params[1]), n=Integer.parseInt(params[2]), k=-1;
				double cdf=0;
				while(rand>cdf){
					cdf+=(choose(w,(k+1))*choose(b,n-(k+1)))/(choose(w+b,n)*1.0);
					k++;
				}
				return(k);
			}
			else{return(getDistValue(dist,params));}
		}
		case "NBin": {
			if(params.length==2){
				int r=Integer.parseInt(params[0]), k=0;
				double p=Double.parseDouble(params[1]), cdf=0;
				while(rand>cdf){
					cdf+=choose(r+k-1,r-1)*Math.pow(p,r)*Math.pow(1-p, k);
					k++;
				}
				return(k);
			}
			else{return(getDistValue(dist,params));}
		}
		case "Pois": { 
			if(params.length==1){
				double lambda=Double.parseDouble(params[0]);
				int k=-1;
				double cdf=0;
				while(rand>cdf){
					double curMass=Math.exp((k+1)*Math.log(lambda)-lambda-Gamma.logGamma(k+2));
					cdf+=curMass;
					k++;
				}
				return(k);
			}
			else{return(getDistValue(dist,params));}
		}
		case "Zipf": {
			if(params.length==2){
				double s=Double.parseDouble(params[0]); int n=Integer.parseInt(params[1]);
				ZipfDistribution zipf=new ZipfDistribution(null,n,s);
				return(zipf.inverseCumulativeProbability(rand));
			}
			else{return(getDistValue(dist,params));}
		}

		//Continuous
		case "Beta": {
			if(params.length==2){
				double a=Double.parseDouble(params[0]), b=Double.parseDouble(params[1]);
				BetaDistribution beta=new BetaDistribution(null,a,b);
				return(beta.inverseCumulativeProbability(rand));
			}
			else{return(getDistValue(dist,params));}
		}
		case "Expo": {
			if(params.length==1){
				double lambda=Double.parseDouble(params[0]);
				return(-Math.log(1-rand)/lambda);
			}
			else{return(getDistValue(dist,params));}
		}
		case "Gam":{ 
			if(params.length==2){
				double k=Double.parseDouble(params[0]), theta=Double.parseDouble(params[1]);
				GammaDistribution gamma=new GammaDistribution(null,k,theta);
				return(gamma.inverseCumulativeProbability(rand));
			}
			else{return(getDistValue(dist,params));}
		}
		case "Gumb":{
			if(params.length==2){
				double mu=Double.parseDouble(params[0]), beta=Double.parseDouble(params[1]);
				GumbelDistribution gumbel=new GumbelDistribution(null,mu,beta);
				return(gumbel.inverseCumulativeProbability(rand)); //mean
			}
			else{return(getDistValue(dist,params));}
		}
		case "Lap":{
			if(params.length==2){
				double mu=Double.parseDouble(params[0]), beta=Double.parseDouble(params[1]);
				LaplaceDistribution lap=new LaplaceDistribution(null,mu,beta);
				return(lap.inverseCumulativeProbability(rand)); //mean
			}
			else{return(getDistValue(dist,params));}
		}
		case "Logi":{
			if(params.length==2){
				double mu=Double.parseDouble(params[0]), s=Double.parseDouble(params[1]);
				LogisticDistribution logi=new LogisticDistribution(null,mu,s);
				return(logi.inverseCumulativeProbability(rand)); //mean
			}
			else{return(getDistValue(dist,params));}
		}
		case "LogN": {
			if(params.length==2){
				double mu=Double.parseDouble(params[0]), sigma=Double.parseDouble(params[1]);
				LogNormalDistribution lnorm=new LogNormalDistribution(null,mu,sigma);
				return(lnorm.inverseCumulativeProbability(rand));
			}
			else{return(getDistValue(dist,params));}
		}
		case "Norm": {
			if(params.length==2){
				double mu=Double.parseDouble(params[0]), sigma=Double.parseDouble(params[1]);
				NormalDistribution norm=new NormalDistribution(null,mu,sigma);
				return(norm.inverseCumulativeProbability(rand));
			}
			else{return(getDistValue(dist,params));}
		}
		case "Par": {
			if(params.length==2){
				double k=Double.parseDouble(params[0]), alpha=Double.parseDouble(params[1]); 
				ParetoDistribution par=new ParetoDistribution(null,k,alpha);
				return(par.inverseCumulativeProbability(rand));
			}
			else{return(getDistValue(dist,params));}
		}
		case "Tri": {
			if(params.length==3){
				double a=Double.parseDouble(params[0]), b=Double.parseDouble(params[1]), c=Double.parseDouble(params[2]);
				TriangularDistribution tri=new TriangularDistribution(null,a,c,b);
				return(tri.inverseCumulativeProbability(rand));
			}
			else{return(getDistValue(dist,params));}
		}
		case "TriBe": {
			if(params.length==3){
				double a=Double.parseDouble(params[0]), b=Double.parseDouble(params[1]), c=Double.parseDouble(params[2]); 
				double u=(a+(4.0*c)+b)/6.0; //Weighted mean, mode is worth 4x
				double a1=((u-a)*(2.0*c-a-b))/((c-u)*(b-a));
				double a2=(a1*(b-u))/(u-a);
				if(c==u){a1=3.0; a2=3.0;} //Check symmetric case where a1 is div/0
				BetaDistribution beta=new BetaDistribution(null,a1,a2);
				double val=beta.inverseCumulativeProbability(rand);
				val=a+(b-a)*val; //Re-scale back to original min/max
				return(val);
			}
			else{return(getDistValue(dist,params));}
		}
		case "Unif": {
			if(params.length==2){
				double a=Double.parseDouble(params[0]), b=Double.parseDouble(params[1]);
				return(a+rand*(b-a));
			}
			else{return(getDistValue(dist,params));}
		}
		case "Weib": {
			if(params.length==2){
				double a=Double.parseDouble(params[0]), b=Double.parseDouble(params[1]);
				WeibullDistribution weib=new WeibullDistribution(null,a,b);
				return(weib.inverseCumulativeProbability(rand));
			}
			else{return(getDistValue(dist,params));}
		}

		//Truncated
		case "tNorm": {
			if(params.length==4){
				double mu=Double.parseDouble(params[0]), sigma=Double.parseDouble(params[1]), a=Double.parseDouble(params[2]), b=Double.parseDouble(params[3]);
				NormalDistribution norm=new NormalDistribution(null,mu,sigma);
				boolean ok=false;
				double val=Double.NaN;
				while(ok==false){
					rand=generator.nextDouble(); //Get next rand
					val=norm.inverseCumulativeProbability(rand);
					if(val>=a && val<=b){ok=true;} //Within truncated distribution
				}
				return(val);
			}
			else{return(getDistValue(dist,params));}
		}

		} //End switch
		return(0);
	}

	public String getDistDescription(String dist){
		String des=null;
		switch(dist){
		//Discrete
		case "Bin": 
			des="<html><b>Binomial Distribution</b><br>";
			des+="Used to model the number of successes that occur in a fixed number of repeated trials<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>n</i>: Number of trials<br>";
			des+="<i>p</i>: Probability of success<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>Bin(n,p)</b>: Returns a random variable (EV in base case) from the Binomial distribution. Integer in {0,1,...,n}<br>";
			des+="<b>Bin(k,n,p,<i>f</i>)</b>: Returns the value of the Binomial PMF at k. Pr(X=k)<br>";
			des+="<b>Bin(k,n,p,<i>F</i>)</b>: Returns the value of the Binomial CDF at k. Pr(X≤k)<br></html>";
			return(des);
		case "DUnif":
			des="<html><b>Discrete Uniform</b><br>";
			des+="Used to model a discrete distribution where all values are equally likely<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>a</i>: Minimum value, inclusive (integer)<br>";
			des+="<i>b</i>: Maximum value, inclusive (integer)<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>DUnif(a,b)</b>: Returns a random variable (EV in base case) from the Discrete Uniform distribution. Integer in {a,a+1,...,b}<br>";
			des+="<b>DUnif(k,a,b,<i>f</i>)</b>: Returns the value of the Discrete Uniform PMF at k. Pr(X=k)<br>";
			des+="<b>DUnif(k,a,b,<i>F</i>)</b>: Returns the value of the Discrete Uniform CDF at k. Pr(X≤k)<br></html>";
			return(des);
		case "Geom":
			des="<html><b>Geometric Distribution</b><br>";
			des+="Used to model the number of successes that occur before the first failure<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>p</i>: Probability of success<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>Geom(p)</b>: Returns a random variable (EV in base case) from the Geometric distribution. Integer in {0,1,...}<br>";
			des+="<b>Geom(k,p,<i>f</i>)</b>: Returns the value of the Geometric PMF at k. Pr(X=k)<br>";
			des+="<b>Geom(k,p,<i>F</i>)</b>: Returns the value of the Geometric CDF at k. Pr(X≤k)<br></html>";
			return(des);
		case "HGeom":
			des="<html><b>Hypergeometric Distribution</b><br>";
			des+="Used to model the number of successes in a fixed number of draws without replacement<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>w</i>: Number of possible successes<br>";
			des+="<i>b</i>: Number of other possible outcomes<br>";
			des+="<i>n</i>: Number of draws<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>HGeom(w,b,n)</b>: Returns a random variable (EV in base case) from the the Hypergeometric distribution. Integer in {0,1,...,min(w,n)}<br>";
			des+="<b>HGeom(k,w,b,n,<i>f</i>)</b>: Returns the value of the Hypergeometric PMF at k. Pr(X=k)<br>";
			des+="<b>HGeom(k,w,b,n,<i>F</i>)</b>: Returns the value of the Hypergeometric CDF at k. Pr(X≤k)<br></html>";
			return(des);
		case "NBin":
			des="<html><b>Negative Binomial Distribution</b><br>";
			des+="Used to model the number of successes that occur among repeated trials before a specified number of failures happen<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>r</i>: Number of failures until the trials are stopped<br>";
			des+="<i>p</i>: Probability of success<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>NBin(r,p)</b>: Returns a random variable (EV in base case) from the Negative Binomial distribution. Integer in {0,1,...}<br>";
			des+="<b>NBin(k,r,p,<i>f</i>)</b>: Returns the value of the Negative Binomial PMF at k. Pr(X=k)<br>";
			des+="<b>NBin(k,r,p,<i>F</i>)</b>: Returns the value of the Negative Binomial CDF at k. Pr(X≤k)<br></html>";
			return(des);
		case "Pois": 
			des="<html><b>Poisson Distribution</b><br>";
			des+="Used to model the number of events that occur in a fixed interval of time/space with a known average rate<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>λ</i>: Average number of events in the interval<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>Pois(λ)</b>: Returns a random variable (EV in base case) from the Poisson distribution. Integer in {0,1,...}<br>";
			des+="<b>Pois(k,λ,<i>f</i>)</b>: Returns the value of the Poisson PMF at k. Pr(X=k)<br>";
			des+="<b>Pois(k,λ,<i>F</i>)</b>: Returns the value of the Poisson CDF at k. Pr(X≤k)<br></html>";
			return(des);
		case "Zipf": 
			des="<html><b>Zipf Distribution</b><br>";
			des+="Used to model discrete power law distributions<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>s</i>: Exponent (real number >1) <br>";
			des+="<i>n</i>: Number of elements (Integer >1) <br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>Zipf(s,n)</b>: Returns a random variable (EV in base case) from the Zipf distribution. Integer in {1,2,...,n}<br>";
			des+="<b>Zipf(k,s,n,<i>f</i>)</b>: Returns the value of the Zipf PMF at k. Pr(X=k)<br>";
			des+="<b>Zipf(k,s,n,<i>F</i>)</b>: Returns the value of the Zipf CDF at k. Pr(X≤k)<br></html>";
			return(des);

			//Continuous
		case "Beta": 
			des="<html><b>Beta Distribution</b><br>";
			des+="A continuous distribution bounded by 0 and 1.  Often used to model probabilities.<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>a</i>: Shape parameter (>0)<br>";
			des+="<i>b</i>: Shape parameter (>0)<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>Beta(a,b)</b>: Returns a random variable (EV in base case) from the Beta distribution. Real number in [0,1]<br>";
			des+="<b>Beta(x,a,b,<i>f</i>)</b>: Returns the density of the Beta PDF at x<br>";
			des+="<b>Beta(x,a,b,<i>F</i>)</b>: Returns the value of the Beta CDF at x. Pr(X≤x)<br></html>";
			return(des);
		case "Expo":
			des="<html><b>Exponential Distribution</b><br>";
			des+="Used to model the time between events<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>λ</i>: Average rate of events<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>Expo(λ)</b>: Returns a random variable (EV in base case) from the Exponential distribution. Real number >0<br>";
			des+="<b>Expo(x,λ,<i>f</i>)</b>: Returns the density of the Exponential PDF at x<br>";
			des+="<b>Expo(x,λ,<i>F</i>)</b>: Returns the value of the Exponential CDF at x. Pr(X≤x)<br></html>";
			return(des);
		case "Gam":
			des="<html><b>Gamma Distribution</b><br>";
			des+="A continuous distribution that yields positive real numbers.<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>k</i>: Shape (>0)<br>";
			des+="<i>θ</i>: Scale (>0)<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>Gam(k,θ)</b>: Returns a random variable (EV in base case) from the Gamma distribution. Real number >0<br>";
			des+="<b>Gam(x,k,θ,<i>f</i>)</b>: Returns the density of the Gamma PDF at x<br>";
			des+="<b>Gam(x,k,θ,<i>F</i>)</b>: Returns the value of the Gamma CDF at x. Pr(X≤x)<br></html>";
			return(des);
		case "Gumb":
			des="<html><b>Gumbel Distribution</b><br>";
			des+="Used to model the distribution of the extrema (max/min) of a number of samples of various distributions<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>μ</i>: Location<br>";
			des+="<i>β</i>: Scale (>0)<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>Gumb(μ,β)</b>: Returns a random variable (EV in base case) from the Gumbel distribution. Real number<br>";
			des+="<b>Gumb(x,μ,β,<i>f</i>)</b>: Returns the density of the Gumbel PDF at x<br>";
			des+="<b>Gumb(x,μ,β,<i>F</i>)</b>: Returns the value of the Gumbel CDF at x. Pr(X≤x)<br></html>";
			return(des);
		case "Lap": 
			des="<html><b>Laplace Distribution</b><br>";
			des+="A continuous distribution that can be thought of as two exponential distributions put back-to-back<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>μ</i>: Location<br>";
			des+="<i>β</i>: Scale (>0)<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>Lap(μ,β)</b>: Returns a random variable (EV in base case) from the Laplace distribution. Real number<br>";
			des+="<b>Lap(x,μ,β,<i>f</i>)</b>: Returns the density of the Laplace PDF at x<br>";
			des+="<b>Lap(x,μ,β,<i>F</i>)</b>: Returns the value of the Laplace CDF at x. Pr(X≤x)<br></html>";
			return(des);
		case "Logi": 
			des="<html><b>Logistic Distribution</b><br>";
			des+="A continuous distribution that resembles the normal distribution in shape but has heavier tails<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>μ</i>: Location<br>";
			des+="<i>s</i>: Scale (>0)<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>Logi(μ,s)</b>: Returns a random variable (EV in base case) from the Logistic distribution. Real number<br>";
			des+="<b>Logi(x,μ,s,<i>f</i>)</b>: Returns the density of the Logistic PDF at x<br>";
			des+="<b>Logi(x,μ,s,<i>F</i>)</b>: Returns the value of the Logistic CDF at x. Pr(X≤x)<br></html>";
			return(des);
		case "LogN": 
			des="<html><b>Log-Normal Distribution</b><br>";
			des+="A continuous distribution of a random variable whose logarithm follows a normal distribution<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>μ</i>: Location<br>";
			des+="<i>σ</i>: Scale (>0)<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>LogN(μ,σ)</b>: Returns a random variable (EV in base case) from the Log-Normal distribution. Real number >0<br>";
			des+="<b>LogN(x,μ,σ,<i>f</i>)</b>: Returns the density of the Log-Normal PDF at x<br>";
			des+="<b>LogN(x,μ,σ,<i>F</i>)</b>: Returns the value of the Log-Normal CDF at x. Pr(X≤x)<br></html>";
			return(des);
		case "Norm": 
			des="<html><b>Normal Distribution</b><br>";
			des+="Canonical bell-shaped distribution<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>μ</i>: Mean<br>";
			des+="<i>σ</i>: Standard deviation<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>Norm(μ,σ)</b>: Returns a random variable (EV in base case) from the Normal distribution. Real number<br>";
			des+="<b>Norm(x,μ,σ,<i>f</i>)</b>: Returns the density of the Normal PDF at x<br>";
			des+="<b>Norm(x,μ,σ,<i>F</i>)</b>: Returns the value of the Normal CDF at x. Pr(X≤x)<br></html>";
			return(des);
		case "Par": 
			des="<html><b>Pareto Distribution</b><br>";
			des+="A power law probability distribution<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>k</i>: Scale (>0) minimum possible value of x<br>";
			des+="<i>α</i>: Shape (>0) Pareto index<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>Par(k,α)</b>: Returns a random variable (EV in base case) from the Pareto distribution. Positive real number<br>";
			des+="<b>Par(x,k,α,<i>f</i>)</b>: Returns the density of the Pareto PDF at x<br>";
			des+="<b>Par(x,k,α,<i>F</i>)</b>: Returns the value of the Pareto CDF at x. Pr(X≤x)<br></html>";
			return(des);
		case "Tri": 
			des="<html><b>Triangular Distribution</b><br>";
			des+="A simple distribution to model the minimum, most likely, and maximum values of a random variable<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>a</i>: Minimum value, inclusive (real number)<br>";
			des+="<i>b</i>: Maximum value, exclusive (><i>a</i>)<br>";
			des+="<i>c</i>: Mode (most likely value)<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>Tri(a,b,c)</b>: Returns a random variable (EV in base case) from the Triangular distribution. Real number in [a,b)<br>";
			des+="<b>Tri(x,a,b,c,<i>f</i>)</b>: Returns the density of the Triangular PDF at x<br>";
			des+="<b>Tri(x,a,b,c,<i>F</i>)</b>: Returns the value of the Triangular CDF at x. Pr(X≤x)<br></html>";
			return(des);
		case "TriBe": 
			des="<html><b>Triangular to Beta Distribution</b><br>";
			des+="Converts a Triangular distribution to a Beta-shaped distribution using the PERT method<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>a</i>: Minimum value, inclusive (real number)<br>";
			des+="<i>b</i>: Maximum value, exclusive (><i>a</i>)<br>";
			des+="<i>c</i>: Mode (most likely value)<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>TriBe(a,b,c)</b>: Returns a random variable (EV in base case) from the resulting Beta-shaped distribution. Real number in [a,b)<br>";
			des+="<b>TriBe(x,a,b,c,<i>f</i>)</b>: Returns the density of the resulting Beta-shaped PDF at x<br>";
			des+="<b>TriBe(x,a,b,c,<i>F</i>)</b>: Returns the value of the resulting Beta-shaped CDF at x. Pr(X≤x)<br></html>";
			return(des);
		case "Unif": 
			des="<html><b>Uniform Distribution</b><br>";
			des+="Used to model a continuous distribution where all values are equally likely <br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>a</i>: Minimum value (inclusive)<br>";
			des+="<i>b</i>: Maximum value (exclusive)<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>Unif(a,b)</b>: Returns a random variable (EV in base case) from the Uniform distribution. Real number in [a,b)<br>";
			des+="<b>Unif(x,a,b,<i>f</i>)</b>: Returns the density of the Uniform PDF at x<br>";
			des+="<b>Unif(x,a,b,<i>F</i>)</b>: Returns the value of the Uniform CDF at x. Pr(X≤x)<br></html>";
			return(des);
		case "Weib": 
			des="<html><b>Weibull Distribution</b><br>";
			des+="Often used to model time-to-failure<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>a</i>: Shape parameter (>0)<br>";
			des+="<i>b</i>: Scale parameter (>0)<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>Weib(a,b)</b>: Returns a random variable (EV in base case) from the Weibull distribution. Positive real number<br>";
			des+="<b>Weib(x,a,b,<i>f</i>)</b>: Returns the density of the Weibull PDF at x<br>";
			des+="<b>Weib(x,a,b,<i>F</i>)</b>: Returns the value of the Weibull CDF at x. Pr(X≤x)<br></html>";
			return(des);

			//Truncated
		case "tNorm": 
			des="<html><b>Truncated Normal Distribution</b><br>";
			des+="A normal distribution bound by min/max values.  Values falling outside the bounds are re-distributed (normally) within the specified support to maintain the shape of the distribution.<br><br>";
			des+="<u>Parameters</u><br>";
			des+="<i>μ</i>: Mean<br>";
			des+="<i>σ</i>: Standard deviation<br>";
			des+="<i>a</i>: Minimum value<br>";
			des+="<i>b</i>: Maximum value<br><br>";
			des+="<u>Functions</u><br>";
			des+="<b>tNorm(μ,σ,a,b)</b>: Returns a random variable (EV in base case) from the Truncated Normal distribution. Real number between a and b<br>";
			des+="<b>tNorm(x,μ,σ,a,b,<i>f</i>)</b>: Returns the density of the Truncated Normal PDF at x<br>";
			des+="<b>tNorm(x,μ,σ,a,b,<i>F</i>)</b>: Returns the value of the Truncated Normal CDF at x. Pr(X≤x)<br></html>";
			return(des);

		} //End switch
		return(des);
	}

	public String getVarDescription(String var){
		String des="";
		int index=getVariableIndex(var);
		TreeVariable curVar=variables.get(index);
		des="<html><b>"+curVar.name+"</b><br>";
		des+="Expression: "+curVar.expression+"<br>";
		des+="Expected Value: "+curVar.value+"<br>";
		if(curVar.notes!=null && !curVar.notes.isEmpty()){
			des+="Notes: "+curVar.notes+"<br>";
		}
		des+="</html>";
		return(des);
	}


	public String getFxDescription(String fx){
		String des="";
		switch(fx){
		case "abs": return("<html><b>abs</b><br>Absolute value</html>");
		case "acos": return("<html><b>acos</b><br>Arccosine</html>");
		case "asin": return("<html><b>asin</b><br>Arcsine</html>");
		case "atan": return("<html><b>atan</b><br>Arctangent</html>");
		case "bound": return("<html><b>bound</b><br>Bounds a value by a ceiling and a floor<br>Arguments: bound(val,min,max)<br>Ex: bound(3,4,5)=4</html>");
		case "cbrt": return("<html><b>cbrt</b><br>Cubic root</html>");
		case "ceil": return("<html><b>ceil</b><br>Nearest upper integer (ceiling)</html>");
		case "cos": return("<html><b>cos</b><br>Cosine</html>");
		case "cosh": return("<html><b>cosh</b><br>Hyperbolic cosine</html>");
		case "exp": return("<html><b>exp</b><br>Euler's number raised to the power (e^x)</html>");
		case "floor": return("<html><b>floor</b><br>Nearest lower integer</html>");
		case "log": return("<html><b>log</b><br>Natural log (base e)</html>");
		case "log10": return("<html><b>log10</b><br>Log base 10</html>");
		case "log2": return("<html><b>log10</b><br>Log base 2</html>");
		case "min": return("<html><b>min</b><br>Returns the minimum of two values</html>");
		case "max": return("<html><b>max</b><br>Returns the maximum of two values</html>");
		case "sin": return("<html><b>sin</b><br>Sine</html>");
		case "sinh": return("<html><b>sinh</b><br>Hyperbolic sine</html>");
		case "sqrt": return("<html><b>sqrt</b><br>Square root</html>");
		case "tan": return("<html><b>sqrt</b><br>Tangent</html>");
		case "tanh": return("<html><b>sqrt</b><br>Hyperbolic tangent</html>");
		case "signum": return("<html><b>signum</b><br>Signum function (extracts the sign of a real number)</html>");
		//Constants
		case "pi": return("<html><b>pi (π)</b><br>Pi (3.141...)</html>");
		case "π": return("<html><b>pi (π)</b><br>Pi (3.141...)</html>");
		case "e": return("<html><b>e</b><br>Euler's number (2.718...)</html>");
		case "φ": return("<html><b>φ</b><br>Golden ratio (1.618...)</html>");
		}
		return(des);
	}

	/**
	 * 
	 * @param k
	 * @param n
	 * @param p
	 * @return
	 */
	private double bin(int k, int n, double p){
		double binCoeff=choose(n,k);
		double pSuccess=Math.pow(p, k);
		double pFail=Math.pow(1.0-p, n-k);
		double val=binCoeff*pSuccess*pFail;
		return(val);
	}

	/**
	 * 
	 * @param n
	 * @param k
	 * @return
	 */
	private double choose(int n, int k){
		if(k>n || k<0){return(0);}
		double value=1;
		for(int i=1; i<=k; i++){
			double ratio=(n+1-i)/(i*1.0);
			value*=ratio;
		}
		return(value);
	}
}