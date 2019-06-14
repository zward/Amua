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
import main.MersenneTwisterFast;
import math.distributions.Bernoulli;
import math.distributions.Beta;
import math.distributions.Binomial;
import math.distributions.Categorical;
import math.distributions.Cauchy;
import math.distributions.ChiSquare;
import math.distributions.Dirichlet;
import math.distributions.DiscreteUniform;
import math.distributions.Exponential;
import math.distributions.Gamma;
import math.distributions.Geometric;
import math.distributions.Gumbel;
import math.distributions.HalfCauchy;
import math.distributions.HalfNormal;
import math.distributions.Hypergeometric;
import math.distributions.Laplace;
import math.distributions.LogNormal;
import math.distributions.Logistic;
import math.distributions.Multinomial;
import math.distributions.MultivariateNormal;
import math.distributions.NegativeBinomial;
import math.distributions.Normal;
import math.distributions.PERT;
import math.distributions.Pareto;
import math.distributions.Poisson;
import math.distributions.StudentT;
import math.distributions.Triangular;
import math.distributions.TruncatedNormal;
import math.distributions.Uniform;
import math.distributions.Weibull;
import math.distributions.Zipf;

public final class Distributions{
	
	public static boolean isDistribution(String dist){
		switch(dist){
		//Discrete
		case "Bern": return(true);
		case "Bin": return(true);
		case "Cat": return(true);
		case "DUnif": return(true);
		case "Geom": return(true);
		case "HGeom": return(true);
		case "NBin": return(true);
		case "Pois": return(true);
		case "Zipf": return(true);
		//Continuous
		case "Beta": return(true);
		case "Cauchy": return(true);
		case "ChiSq": return(true);
		case "Expo": return(true);
		case "Gamma": return(true);
		case "Gumbel": return(true);
		case "HalfCauchy": return(true);
		case "HalfNorm": return(true);
		case "Laplace": return(true);
		case "Logistic": return(true);
		case "LogNorm": return(true);
		case "Norm": return(true);
		case "Pareto": return(true);
		case "PERT": return(true);
		case "StudentT": return(true);
		case "Tri": return(true);
		case "Unif": return(true);
		case "Weibull": return(true);
		//Truncated
		case "TruncNorm": return(true);
		//MultiVariate
		case "Dir": return(true);
		case "MvNorm": return(true);
		case "Multi": return(true);
		
		}
		return(false);
	}
	
	/**
	 * 
	 * @param dist
	 * @param params
	 * @param df Distribution function: 0=Density/Mass, 1=CDF, 2=Inverse CDF (quantile)
	 * @return
	 * @throws Exception 
	 */
	public static Numeric evaluate(String dist, Numeric params[], int df) throws NumericException{
		switch(dist){
		//Discrete
		case "Bern":{
			if(params.length==1){ //(p,~)
				if(df==-1 || df==3){return(Bernoulli.mean(params));} //Sample, Mean
				else if(df==4){return(Bernoulli.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Bern");}
			}
			else if(params.length==2){ //(k,p,f)
				if(df==0){return(Bernoulli.pmf(params));} //PMF
				else if(df==1){return(Bernoulli.cdf(params));} //CDF
				else if(df==2){return(Bernoulli.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Bern");}
			}
			else{throw new NumericException("Incorrect number of parameters","Bern");}
		}
		case "Bin": {
			if(params.length==2){ //(n,p,~)
				if(df==-1 || df==3){return(Binomial.mean(params));} //Sample, Mean
				else if(df==4){return(Binomial.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Bin");}
			}
			else if(params.length==3){ //(k,n,p,f)
				if(df==0){return(Binomial.pmf(params));} //PMF
				else if(df==1){return(Binomial.cdf(params));} //CDF
				else if(df==2){return(Binomial.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Bin");}
			}
			else{throw new NumericException("Incorrect number of parameters","Bin");}
		}
		case "Cat":{
			if(params.length==1){ //(p,~)
				if(df==-1 || df==3){return(Categorical.mean(params));} //Sample, Mean
				else if(df==4){return(Categorical.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Cat");}
			}
			else if(params.length==2){ //(k,p,f)
				if(df==0){return(Categorical.pmf(params));} //PMF
				else if(df==1){return(Categorical.cdf(params));} //CDF
				else if(df==2){return(Categorical.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Cat");}
			}
			else{throw new NumericException("Incorrect number of parameters","Cat");}
		}
		case "DUnif": { 
			if(params.length==2){ //(a,b,~)
				if(df==-1 || df==3){return(DiscreteUniform.mean(params));} //Sample, Mean
				else if(df==4){return(DiscreteUniform.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","DUnif");}
			}
			else if(params.length==3){ //(k,a,b,f)
				if(df==0){return(DiscreteUniform.pmf(params));} //PMF
				else if(df==1){return(DiscreteUniform.cdf(params));} //CDF
				else if(df==2){return(DiscreteUniform.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","DUnif");}
			}
			else{throw new NumericException("Incorrect number of parameters","DUnif");}
		}
		case "Geom": {
			if(params.length==1){ //(p,~)
				if(df==-1 || df==3){return(Geometric.mean(params));} //Sample, Mean
				else if(df==4){return(Geometric.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Geom");}
			}
			else if(params.length==2){ //(k,p,f)
				if(df==0){return(Geometric.pmf(params));} //PMF
				else if(df==1){return(Geometric.cdf(params));} //CDF
				else if(df==2){return(Geometric.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Geom");}
			}
			else{throw new NumericException("Incorrect number of parameters","Geom");}
		}
		case "HGeom": {
			if(params.length==3){ //(w,b,n,~)
				if(df==-1 || df==3){return(Hypergeometric.mean(params));} //Sample, Mean
				else if(df==4){return(Hypergeometric.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","HGeom");}
			}
			else if(params.length==4){ //(k,w,b,n,f)
				if(df==0){return(Hypergeometric.pmf(params));} //PMF
				else if(df==1){return(Hypergeometric.cdf(params));} //CDF
				else if(df==2){return(Hypergeometric.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","HGeom");}
			}
			else{throw new NumericException("Incorrect number of parameters","HGeom");}
		}
		case "NBin": {
			if(params.length==2){ //(r,p,~)
				if(df==-1 || df==3){return(NegativeBinomial.mean(params));} //Sample, Mean
				else if(df==4){return(NegativeBinomial.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","NBin");}
			}
			else if(params.length==3){ //(k,r,p,f)
				if(df==0){return(NegativeBinomial.pmf(params));} //PMF
				else if(df==1){return(NegativeBinomial.cdf(params));} //CDF
				else if(df==2){return(NegativeBinomial.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","NBin");}
			}
			else{throw new NumericException("Incorrect number of parameters","NBin");}
		}
		case "Pois": {
			if(params.length==1){ //(λ,~)
				if(df==-1 || df==3){return(Poisson.mean(params));} //Sample, Mean
				else if(df==4){return(Poisson.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Pois");}
			}
			else if(params.length==2){ //(k,λ,f)
				if(df==0){return(Poisson.pmf(params));} //PMF
				else if(df==1){return(Poisson.cdf(params));} //CDF
				else if(df==2){return(Poisson.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Pois");}
			}
			else{throw new NumericException("Incorrect number of parameters","Pois");}
		}
		case "Zipf": {
			if(params.length==2){ //(s,n,~)
				if(df==-1 || df==3){return(Zipf.mean(params));} //Sample, Mean
				else if(df==4){return(Zipf.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Zipf");}
			}
			else if(params.length==3){ //(k,s,n,f)
				if(df==0){return(Zipf.pmf(params));} //PMF
				else if(df==1){return(Zipf.cdf(params));} //CDF
				else if(df==2){return(Zipf.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Zipf");}
			}
			else{throw new NumericException("Incorrect number of parameters","Zipf");}
		}

		//Continuous
		case "Beta": {
			if(params.length==2){ //(a,b,~)
				if(df==-1 || df==3){return(Beta.mean(params));} //Sample, Mean
				else if(df==4){return(Beta.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Beta");}
			}
			else if(params.length==3){ //(x,a,b,f)
				if(df==0){return(Beta.pdf(params));} //PDF
				else if(df==1){return(Beta.cdf(params));} //CDF
				else if(df==2){return(Beta.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Beta");}
			}
			else{throw new NumericException("Incorrect number of parameters","Beta");}
		}
		case "Cauchy": {
			if(params.length==2){ //(loc,scale,~)
				if(df==-1){ //Sample (return median) 
					double a=params[0].getDouble(), b=params[1].getDouble();
					if(b<=0){throw new NumericException("γ should be >0","Cauchy");}
					return(new Numeric(a));
				} 
				else if(df==3){return(Cauchy.mean(params));} //Sample, Mean
				else if(df==4){return(Cauchy.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Cauchy");}
			}
			else if(params.length==3){ //(x,loc,scale,f)
				if(df==0){return(Cauchy.pdf(params));} //PDF
				else if(df==1){return(Cauchy.cdf(params));} //CDF
				else if(df==2){return(Cauchy.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Cauchy");}
			}
			else{throw new NumericException("Incorrect number of parameters","Cauchy");}
		}
		case "ChiSq":{
			if(params.length==1){ //(k,~)
				if(df==-1 || df==3){return(ChiSquare.mean(params));} //Sample, Mean
				else if(df==4){return(ChiSquare.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","ChiSq");}
			}
			else if(params.length==2){ //(x,k,f)
				if(df==0){return(ChiSquare.pdf(params));} //PDF
				else if(df==1){return(ChiSquare.cdf(params));} //CDF
				else if(df==2){return(ChiSquare.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","ChiSq");}
			}
			else{throw new NumericException("Incorrect number of parameters","ChiSq");}
		}
		case "Expo": {
			if(params.length==1){ //(lambda,~)
				if(df==-1 || df==3){return(Exponential.mean(params));} //Sample, Mean
				else if(df==4){return(Exponential.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Expo");}
			}
			else if(params.length==2){//(x,lambda,f): 
				if(df==0){return(Exponential.pdf(params));} //PDF
				else if(df==1){return(Exponential.cdf(params));} //CDF
				else if(df==2){return(Exponential.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Expo");}
			}
			else{throw new NumericException("Incorrect number of parameters","Expo");}
		}
		case "Gamma":{
			if(params.length==2){ //(k,theta,~)
				if(df==-1 || df==3){return(Gamma.mean(params));} //Sample, Mean
				else if(df==4){return(Gamma.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Gamma");}
			}
			else if(params.length==3){ //(x,k,theta,f)
				if(df==0){return(Gamma.pdf(params));} //PDF
				else if(df==1){return(Gamma.cdf(params));} //CDF
				else if(df==2){return(Gamma.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Gamma");}
			}
			else{throw new NumericException("Incorrect number of parameters","Gamma");}
		}
		case "Gumbel":{
			if(params.length==2){ //(mu,beta,~)
				if(df==-1 || df==3){return(Gumbel.mean(params));} //Sample, Mean
				else if(df==4){return(Gumbel.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Gumbel");}
			}
			else if(params.length==3){ //(x,mu,beta,f)
				if(df==0){return(Gumbel.pdf(params));} //PDF
				else if(df==1){return(Gumbel.cdf(params));} //CDF
				else if(df==2){return(Gumbel.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Gumbel");}
			}
			else{throw new NumericException("Incorrect number of parameters","Gumbel");}
		}
		case "HalfCauchy":{
			if(params.length==1){ //(gamma,~)
				if(df==-1){ //Sample (return median)
					double gamma=params[0].getDouble();
					if(gamma<=0){throw new NumericException("γ should be >0","HalfCauchy");}
					return(new Numeric(gamma)); //median
				}
				else if(df==3){return(HalfCauchy.mean(params));} //Mean
				else if(df==4){return(HalfCauchy.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","HalfCauchy");}
			}
			else if(params.length==2){ //(x,gamma,f)
				if(df==0){return(HalfCauchy.pdf(params));} //PDF
				else if(df==1){return(HalfCauchy.cdf(params));} //CDF
				else if(df==2){return(HalfCauchy.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","HalfCauchy");}
			}
			else{throw new NumericException("Incorrect number of parameters","HalfCauchy");}
		}
		case "HalfNorm":{
			if(params.length==1){ //(sigma,~)
				if(df==-1 || df==3){return(HalfNormal.mean(params));} //Sample, Mean
				else if(df==4){return(HalfNormal.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","HalfNorm");}
			}
			else if(params.length==2){ //(x,sigma,f)
				if(df==0){return(HalfNormal.pdf(params));} //PDF
				else if(df==1){return(HalfNormal.cdf(params));} //CDF
				else if(df==2){return(HalfNormal.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","HalfNorm");}
			}
			else{throw new NumericException("Incorrect number of parameters","HalfNorm");}
		}
		case "Laplace": {
			if(params.length==2){ //(mu,b,~)
				if(df==-1 || df==3){return(Laplace.mean(params));} //Sample, Mean
				else if(df==4){return(Laplace.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Laplace");}
			}
			else if(params.length==3){ // (x,mu,b,f)
				if(df==0){return(Laplace.pdf(params));} //PDF
				else if(df==1){return(Laplace.cdf(params));} //CDF
				else if(df==2){return(Laplace.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Laplace");}
			}
			else{throw new NumericException("Incorrect number of parameters","Laplace");}
		}
		case "Logistic": {
			if(params.length==2){ //(mu,s,~)
				if(df==-1 || df==3){return(Logistic.mean(params));} //Sample, Mean
				else if(df==4){return(Logistic.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Logistic");}
			}
			else if(params.length==3){ //(x,mu,s,f)
				if(df==0){return(Logistic.pdf(params));} //PDF
				else if(df==1){return(Logistic.cdf(params));} //CDF
				else if(df==2){return(Logistic.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Logistic");}
			}
			else{throw new NumericException("Incorrect number of parameters","Logistic");}
		}
		case "LogNorm": {
			if(params.length==2){ //(mu,sigma,~)
				if(df==-1 || df==3){return(LogNormal.mean(params));} //Sample, Mean
				else if(df==4){return(LogNormal.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","LogNorm");}
			}
			else if(params.length==3){ //(x,mu,sigma,f)
				if(df==0){return(LogNormal.pdf(params));} //PDF
				else if(df==1){return(LogNormal.cdf(params));} //CDF
				else if(df==2){return(LogNormal.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","LogNorm");}
			}
			else{throw new NumericException("Incorrect number of parameters","LogNorm");}
		}
		case "Norm": {
			if(params.length==2){ //(mu,sigma,~)
				if(df==-1 || df==3){return(Normal.mean(params));} //Sample, Mean
				else if(df==4){return(Normal.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Norm");}
			}
			else if(params.length==3){ //(x,mu,sigma,f)
				if(df==0){return(Normal.pdf(params));} //PDF
				else if(df==1){return(Normal.cdf(params));} //CDF
				else if(df==2){return(Normal.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Norm");}
			}
			else{throw new NumericException("Incorrect number of parameters","Norm");}
		}
		case "Pareto": {
			if(params.length==2){ //(k,alpha,~)
				if(df==-1 || df==3){return(Pareto.mean(params));} //Sample, Mean
				else if(df==4){return(Pareto.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Pareto");}
			}
			else if(params.length==3){ //(x,k,alpha,f)
				if(df==0){return(Pareto.pdf(params));} //PDF
				else if(df==1){return(Pareto.cdf(params));} //CDF
				else if(df==2){return(Pareto.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Pareto");}
			}
			else{throw new NumericException("Incorrect number of parameters","Pareto");}
		}
		case "PERT": { 
			if(params.length==3){ //(a,b,c,~)
				if(df==-1 || df==3){return(PERT.mean(params));} //Sample, Mean
				else if(df==4){return(PERT.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","PERT");}
			}
			else if(params.length==4){ //(x,a,b,c,f)
				if(df==0){return(PERT.pdf(params));} //PDF
				else if(df==1){return(PERT.cdf(params));} //CDF
				else if(df==2){return(PERT.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","PERT");}
			}
			else{throw new NumericException("Incorrect number of parameters","PERT");}
		}
		case "StudentT":{
			if(params.length==1){ //(nu,~)
				if(df==-1){return(new Numeric(0));} //Sample (median) 
				else if(df==3){return(StudentT.mean(params));} //Mean
				else if(df==4){return(StudentT.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","StudentT");}
			}
			else if(params.length==2){ //(x,nu,f)
				if(df==0){return(StudentT.pdf(params));} //PDF
				else if(df==1){return(StudentT.cdf(params));} //CDF
				else if(df==2){return(StudentT.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","StudentT");}
			}
			else{throw new NumericException("Incorrect number of parameters","StudentT");}
		}
		case "Tri": {
			if(params.length==3){ //(a,b,c,~)
				if(df==-1 || df==3){return(Triangular.mean(params));} //Sample, Mean
				else if(df==4){return(Triangular.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Tri");}
			}
			else if(params.length==4){ //(x,a,b,c,f)
				if(df==0){return(Triangular.pdf(params));} //PDF
				else if(df==1){return(Triangular.cdf(params));} //CDF
				else if(df==2){return(Triangular.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Tri");}
			}
			else{throw new NumericException("Incorrect number of parameters","Tri");}
		}
		case "Unif": {
			if(params.length==2){ //(a,b,~)
				if(df==-1 || df==3){return(Uniform.mean(params));} //Sample, Mean
				else if(df==4){return(Uniform.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Unif");}
			}
			else if(params.length==3){ //(x,a,b,f)
				if(df==0){return(Uniform.pdf(params));} //PDF
				else if(df==1){return(Uniform.cdf(params));} //CDF
				else if(df==2){return(Uniform.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Unif");}
			}
			else{throw new NumericException("Incorrect number of parameters","Unif");}
		}
		case "Weibull": {
			if(params.length==2){ //(a,b,~)
				if(df==-1 || df==3){return(Weibull.mean(params));} //Sample, Mean
				else if(df==4){return(Weibull.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Weibull");}
			}
			else if(params.length==3){ //(x,a,b,f)
				if(df==0){return(Weibull.pdf(params));} //PDF
				else if(df==1){return(Weibull.cdf(params));} //CDF
				else if(df==2){return(Weibull.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Weibull");}
			}
			else{throw new NumericException("Incorrect number of parameters","Weibull");}
		}

		//Truncated
		case "TruncNorm": {
			if(params.length==4){ //(mu,sigma,a,b,~) return mean
				if(df==-1 || df==3){return(TruncatedNormal.mean(params));} //Sample, Mean
				else if(df==4){return(TruncatedNormal.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","TruncNorm");}
			}
			else if(params.length==5){ //(x,mu,sigma,a,b,f)
				if(df==0){return(TruncatedNormal.pdf(params));} //PDF
				else if(df==1){return(TruncatedNormal.cdf(params));} //CDF
				else if(df==2){return(TruncatedNormal.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","TruncNorm");}
			}
			else{throw new NumericException("Incorrect number of parameters","TruncNorm");}
		}
		
		//Multivariate
		case "Dir": {
			if(params.length==1){ //(alpha) return mean
				if(df==-1 || df==3){return(Dirichlet.mean(params));} //Sample, Mean
				else if(df==4){return(Dirichlet.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Dir");}
			}
			else if(params.length==2){ //(x,alpha,f)
				if(df==0){return(Dirichlet.pdf(params));} //PDF
				else if(df==1){return(Dirichlet.cdf(params));} //CDF
				else if(df==2){return(Dirichlet.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Dir");}
			}
			else{throw new NumericException("Incorrect number of parameters","Dir");}
		}
		case "MvNorm": {
			if(params.length==2){ //(mu,sigma) return mean
				if(df==-1 || df==3){return(MultivariateNormal.mean(params));} //Sample, Mean
				else if(df==4){return(MultivariateNormal.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","MvNorm");}
			}
			else if(params.length==3){ //(x,mu,sigma,f)
				if(df==0){return(MultivariateNormal.pdf(params));} //PDF
				else if(df==1){return(MultivariateNormal.cdf(params));} //CDF
				else if(df==2){return(MultivariateNormal.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","MvNorm");}
			}
			else{throw new NumericException("Incorrect number of parameters","MvNorm");}
		}
		case "Multi": {
			if(params.length==2){ //(n,p,~) return mean
				if(df==-1 || df==3){return(Multinomial.mean(params));} //Sample, Mean
				else if(df==4){return(Multinomial.variance(params));} //Variance
				else{throw new NumericException("Invalid parameters","Multi");}
			}
			else if(params.length==3){ //(k,n,p,f)
				if(df==0){return(Multinomial.pmf(params));} //PMF
				else if(df==1){return(Multinomial.cdf(params));} //CDF
				else if(df==2){return(Multinomial.quantile(params));} //Quantile
				else{throw new NumericException("Invalid parameters","Multi");}
			}
			else{throw new NumericException("Incorrect number of parameters","Multi");}
		}

		} //End switch
		return(null);
	}

	public static Numeric sample(String dist, Numeric params[], double rand,MersenneTwisterFast generator) throws NumericException{
		switch(dist){
		//Discrete
		case "Bern": return(Bernoulli.sample(params,rand));
		case "Bin": return(Binomial.sample(params, rand));
		case "Cat": return(Categorical.sample(params, rand));
		case "DUnif": return(DiscreteUniform.sample(params, rand));
		case "Geom": return(Geometric.sample(params, rand));
		case "HGeom": return(Hypergeometric.sample(params,rand));
		case "NBin": return(NegativeBinomial.sample(params, rand));
		case "Pois": return(Poisson.sample(params, rand));
		case "Zipf": return(Zipf.sample(params, rand));
		//Continuous
		case "Beta": return(Beta.sample(params, rand));
		case "Cauchy": return(Cauchy.sample(params,rand));
		case "ChiSq": return(ChiSquare.sample(params, rand));
		case "Expo": return(Exponential.sample(params, rand));
		case "Gamma": return(Gamma.sample(params, rand));
		case "Gumbel": return(Gumbel.sample(params, rand));
		case "HalfCauchy": return(HalfCauchy.sample(params, rand));
		case "HalfNorm": return(HalfNormal.sample(params, rand));
		case "Laplace": return(Laplace.sample(params, rand));
		case "Logistic": return(Logistic.sample(params, rand));
		case "LogNorm": return(LogNormal.sample(params, rand));
		case "Norm": return(Normal.sample(params, rand));
		case "Pareto": return(Pareto.sample(params, rand));
		case "PERT": return(PERT.sample(params, rand));
		case "StudentT": return(StudentT.sample(params, rand));
		case "Tri": return(Triangular.sample(params, rand));
		case "Unif": return(Uniform.sample(params, rand));
		case "Weibull": return(Weibull.sample(params, rand));
		//Truncated
		case "TruncNorm": return(TruncatedNormal.sample(params, rand));
		//Multivariate
		case "Dir": return(Dirichlet.sample(params, generator));
		case "MvNorm": return(MultivariateNormal.sample(params, generator));
		case "Multi": return(Multinomial.sample(params, generator));
	
		} //End switch
		return(null);
	}
	
	public static String getDescription(String dist){
		String des=null;
		switch(dist){
		//Discrete
		case "Bern": return(Bernoulli.description());
		case "Bin": return(Binomial.description());
		case "Cat": return(Categorical.description());
		case "DUnif": return(DiscreteUniform.description());
		case "Geom": return(Geometric.description());
		case "HGeom": return(Hypergeometric.description());
		case "NBin": return(NegativeBinomial.description());
		case "Pois": return(Poisson.description());
		case "Zipf": return(Zipf.description());
		//Continuous
		case "Beta": return(Beta.description()); 
		case "Cauchy": return(Cauchy.description());
		case "ChiSq": return(ChiSquare.description());
		case "Expo": return(Exponential.description());
		case "Gamma": return(Gamma.description());
		case "Gumbel": return(Gumbel.description());
		case "HalfCauchy": return(HalfCauchy.description());
		case "HalfNorm": return(HalfNormal.description());
		case "Laplace": return(Laplace.description());
		case "Logistic": return(Logistic.description());
		case "LogNorm": return(LogNormal.description());
		case "Norm": return(Normal.description());
		case "Pareto": return(Pareto.description());
		case "PERT": return(PERT.description());
		case "StudentT": return(StudentT.description());
		case "Tri": return(Triangular.description());
		case "Unif": return(Uniform.description());
		case "Weibull": return(Weibull.description());
		//Truncated
		case "TruncNorm": return(TruncatedNormal.description());
		//Multivariate
		case "Dir": return(Dirichlet.description());
		case "MvNorm": return(MultivariateNormal.description());
		case "Multi": return(Multinomial.description());

		} //End switch
		return(des);
	}
	
	public static String getDefaultParams(String dist){
		switch(dist){
		//Discrete
		case "Bern": return("Bern(p,~)");
		case "Bin": return("Bin(n,p,~)");
		case "Cat": return("Cat(p,~)");
		case "DUnif": return("DUnif(a,b,~)");
		case "Geom": return("Geom(p,~)");
		case "HGeom": return("HGeom(w,b,n,~)");
		case "NBin": return("NBin(r,p,~)");
		case "Pois": return("Pois(λ,~)");
		case "Zipf": return("Zipf(s,n,~)");
		//Continuous
		case "Beta": return("Beta(a,b,~)");
		case "Cauchy": return("Cauchy(x₀,γ,~)");
		case "ChiSq": return("ChiSq(k,~)");
		case "Expo": return("Expo(λ,~)");
		case "Gamma": return("Gamma(k,θ,~)");
		case "Gumbel": return("Gumbel(μ,β,~)");
		case "HalfCauchy": return("HalfCauchy(γ,~)");
		case "HalfNorm": return("HalfNorm(σ,~)");
		case "Laplace": return("Laplace(μ,b,~)");
		case "Logistic": return("Logistic(μ,s,~)");
		case "LogNorm": return("LogNorm(μ,σ,~)");
		case "Norm": return("Norm(μ,σ,~)");
		case "Pareto": return("Pareto(k,α,~)");
		case "PERT": return("PERT(a,b,c,~)");
		case "StudentT": return("StudentT(ν,~)");
		case "Tri": return("Tri(a,b,c,~)");
		case "Unif": return("Unif(a,b,~)");
		case "Weibull": return("Weibull(a,b,~)");
		//Truncated
		case "TruncNorm": return("TruncNorm(μ,σ,a,b,~)");
		//MultiVariate
		case "Dir": return("Dir(α,~)");
		case "MvNorm": return("MvNorm(μ,Σ)");
		case "Multi": return("Multi(n,p,~)");
		
		}
		return("");
	}
}