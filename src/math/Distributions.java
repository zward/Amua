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
import java.text.MessageFormat;

import lang.Language;
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
	public static Numeric evaluate(String dist, Numeric params[], int df, Language language) throws NumericException{
		switch(dist){
		//Discrete
		case "Bern":{
			if(params.length==1){ //(p,~)
				if(df==-1 || df==3){return(Bernoulli.mean(params, language));} //Sample, Mean
				else if(df==4){return(Bernoulli.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Bern", language);} //Invalid parameters
			}
			else if(params.length==2){ //(k,p,f)
				if(df==0){return(Bernoulli.pmf(params, language));} //PMF
				else if(df==1){return(Bernoulli.cdf(params, language));} //CDF
				else if(df==2){return(Bernoulli.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Bern", language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Bern", language);} //Incorrect number of parameters
		}
		case "Bin": {
			if(params.length==2){ //(n,p,~)
				if(df==-1 || df==3){return(Binomial.mean(params, language));} //Sample, Mean
				else if(df==4){return(Binomial.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Bin", language);} //Invalid parameters
			}
			else if(params.length==3){ //(k,n,p,f)
				if(df==0){return(Binomial.pmf(params, language));} //PMF
				else if(df==1){return(Binomial.cdf(params, language));} //CDF
				else if(df==2){return(Binomial.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Bin", language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Bin", language);} //Incorrect number of parameters
		}
		case "Cat":{
			if(params.length==1){ //(p,~)
				if(df==-1 || df==3){return(Categorical.mean(params, language));} //Sample, Mean
				else if(df==4){return(Categorical.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Cat",language);} //Invalid parameters
			}
			else if(params.length==2){ //(k,p,f)
				if(df==0){return(Categorical.pmf(params, language));} //PMF
				else if(df==1){return(Categorical.cdf(params, language));} //CDF
				else if(df==2){return(Categorical.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Cat",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Cat",language);} //Incorrect number of parameters
		}
		case "DUnif": { 
			if(params.length==2){ //(a,b,~)
				if(df==-1 || df==3){return(DiscreteUniform.mean(params, language));} //Sample, Mean
				else if(df==4){return(DiscreteUniform.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"DUnif",language);} //Invalid parameters
			}
			else if(params.length==3){ //(k,a,b,f)
				if(df==0){return(DiscreteUniform.pmf(params, language));} //PMF
				else if(df==1){return(DiscreteUniform.cdf(params, language));} //CDF
				else if(df==2){return(DiscreteUniform.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"DUnif",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"DUnif",language);} //Incorrect number of parameters
		}
		case "Geom": {
			if(params.length==1){ //(p,~)
				if(df==-1 || df==3){return(Geometric.mean(params, language));} //Sample, Mean
				else if(df==4){return(Geometric.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Geom",language);} //Invalid parameters
			}
			else if(params.length==2){ //(k,p,f)
				if(df==0){return(Geometric.pmf(params, language));} //PMF
				else if(df==1){return(Geometric.cdf(params, language));} //CDF
				else if(df==2){return(Geometric.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Geom",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Geom",language);} //Incorrect number of parameters
		}
		case "HGeom": {
			if(params.length==3){ //(w,b,n,~)
				if(df==-1 || df==3){return(Hypergeometric.mean(params, language));} //Sample, Mean
				else if(df==4){return(Hypergeometric.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"HGeom",language);} //Invalid parameters
			}
			else if(params.length==4){ //(k,w,b,n,f)
				if(df==0){return(Hypergeometric.pmf(params, language));} //PMF
				else if(df==1){return(Hypergeometric.cdf(params, language));} //CDF
				else if(df==2){return(Hypergeometric.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"HGeom",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"HGeom",language);} //Incorrect number of parameters
		}
		case "NBin": {
			if(params.length==2){ //(r,p,~)
				if(df==-1 || df==3){return(NegativeBinomial.mean(params, language));} //Sample, Mean
				else if(df==4){return(NegativeBinomial.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"NBin",language);} //Invalid parameters
			}
			else if(params.length==3){ //(k,r,p,f)
				if(df==0){return(NegativeBinomial.pmf(params, language));} //PMF
				else if(df==1){return(NegativeBinomial.cdf(params, language));} //CDF
				else if(df==2){return(NegativeBinomial.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"NBin",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"NBin",language);} //Incorrect number of parameters
		}
		case "Pois": {
			if(params.length==1){ //(λ,~)
				if(df==-1 || df==3){return(Poisson.mean(params, language));} //Sample, Mean
				else if(df==4){return(Poisson.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Pois",language);} //Invalid parameters
			}
			else if(params.length==2){ //(k,λ,f)
				if(df==0){return(Poisson.pmf(params, language));} //PMF
				else if(df==1){return(Poisson.cdf(params, language));} //CDF
				else if(df==2){return(Poisson.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Pois",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Pois",language);} //Incorrect number of parameters
		}
		case "Zipf": {
			if(params.length==2){ //(s,n,~)
				if(df==-1 || df==3){return(Zipf.mean(params, language));} //Sample, Mean
				else if(df==4){return(Zipf.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Zipf",language);} //Invalid parameters
			}
			else if(params.length==3){ //(k,s,n,f)
				if(df==0){return(Zipf.pmf(params, language));} //PMF
				else if(df==1){return(Zipf.cdf(params, language));} //CDF
				else if(df==2){return(Zipf.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Zipf",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Zipf",language);} //Incorrect number of parameters
		}

		//Continuous
		case "Beta": {
			if(params.length==2){ //(a,b,~)
				if(df==-1 || df==3){return(Beta.mean(params, language));} //Sample, Mean
				else if(df==4){return(Beta.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Beta",language);} //Invalid parameters
			}
			else if(params.length==3){ //(x,a,b,f)
				if(df==0){return(Beta.pdf(params, language));} //PDF
				else if(df==1){return(Beta.cdf(params, language));} //CDF
				else if(df==2){return(Beta.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Beta",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Beta",language);} //Incorrect number of parameters
		}
		case "Cauchy": {
			if(params.length==2){ //(loc,scale,~)
				if(df==-1){ //Sample (return median) 
					double a=params[0].getDouble(language), b=params[1].getDouble(language);
					if(b<=0){
						//[val] should be >0
						String msg = MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ");
						throw new NumericException(msg,"Cauchy",language);
					}
					return(new Numeric(a));
				} 
				else if(df==3){return(Cauchy.mean(params, language));} //Sample, Mean
				else if(df==4){return(Cauchy.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Cauchy",language);} //Invalid parameters
			}
			else if(params.length==3){ //(x,loc,scale,f)
				if(df==0){return(Cauchy.pdf(params, language));} //PDF
				else if(df==1){return(Cauchy.cdf(params, language));} //CDF
				else if(df==2){return(Cauchy.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Cauchy",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Cauchy",language);} //Incorrect number of parameters
		}
		case "ChiSq":{
			if(params.length==1){ //(k,~)
				if(df==-1 || df==3){return(ChiSquare.mean(params, language));} //Sample, Mean
				else if(df==4){return(ChiSquare.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"ChiSq",language);} //Invalid parameters
			}
			else if(params.length==2){ //(x,k,f)
				if(df==0){return(ChiSquare.pdf(params, language));} //PDF
				else if(df==1){return(ChiSquare.cdf(params, language));} //CDF
				else if(df==2){return(ChiSquare.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"ChiSq",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"ChiSq",language);} //Incorrect number of parameters
		}
		case "Expo": {
			if(params.length==1){ //(lambda,~)
				if(df==-1 || df==3){return(Exponential.mean(params, language));} //Sample, Mean
				else if(df==4){return(Exponential.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Expo",language);} //Invalid parameters
			}
			else if(params.length==2){//(x,lambda,f): 
				if(df==0){return(Exponential.pdf(params, language));} //PDF
				else if(df==1){return(Exponential.cdf(params, language));} //CDF
				else if(df==2){return(Exponential.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Expo",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Expo",language);} //Incorrect number of parameters
		}
		case "Gamma":{
			if(params.length==2){ //(k,theta,~)
				if(df==-1 || df==3){return(Gamma.mean(params, language));} //Sample, Mean
				else if(df==4){return(Gamma.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Gamma",language);} //Invalid parameters
			}
			else if(params.length==3){ //(x,k,theta,f)
				if(df==0){return(Gamma.pdf(params, language));} //PDF
				else if(df==1){return(Gamma.cdf(params, language));} //CDF
				else if(df==2){return(Gamma.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Gamma",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Gamma",language);} //Incorrect number of parameters
		}
		case "Gumbel":{
			if(params.length==2){ //(mu,beta,~)
				if(df==-1 || df==3){return(Gumbel.mean(params, language));} //Sample, Mean
				else if(df==4){return(Gumbel.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Gumbel",language);} //Invalid parameters
			}
			else if(params.length==3){ //(x,mu,beta,f)
				if(df==0){return(Gumbel.pdf(params, language));} //PDF
				else if(df==1){return(Gumbel.cdf(params, language));} //CDF
				else if(df==2){return(Gumbel.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Gumbel",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Gumbel",language);} //Incorrect number of parameters
		}
		case "HalfCauchy":{
			if(params.length==1){ //(gamma,~)
				if(df==-1){ //Sample (return median)
					double gamma=params[0].getDouble(language);
					if(gamma<=0){
						//[val] should be >0
						String msg = MessageFormat.format(language.message.getString("err.val_should_be_gt0"), "γ");
						throw new NumericException(msg,"HalfCauchy",language);
					}
					return(new Numeric(gamma)); //median
				}
				else if(df==3){return(HalfCauchy.mean(params, language));} //Mean
				else if(df==4){return(HalfCauchy.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"HalfCauchy",language);} //Invalid parameters
			}
			else if(params.length==2){ //(x,gamma,f)
				if(df==0){return(HalfCauchy.pdf(params, language));} //PDF
				else if(df==1){return(HalfCauchy.cdf(params, language));} //CDF
				else if(df==2){return(HalfCauchy.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"HalfCauchy",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"HalfCauchy",language);} //Incorrect number of parameters
		}
		case "HalfNorm":{
			if(params.length==1){ //(sigma,~)
				if(df==-1 || df==3){return(HalfNormal.mean(params, language));} //Sample, Mean
				else if(df==4){return(HalfNormal.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"HalfNorm",language);} //Invalid parameters
			}
			else if(params.length==2){ //(x,sigma,f)
				if(df==0){return(HalfNormal.pdf(params, language));} //PDF
				else if(df==1){return(HalfNormal.cdf(params, language));} //CDF
				else if(df==2){return(HalfNormal.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"HalfNorm",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"HalfNorm",language);} //Incorrect number of parameters
		}
		case "Laplace": {
			if(params.length==2){ //(mu,b,~)
				if(df==-1 || df==3){return(Laplace.mean(params, language));} //Sample, Mean
				else if(df==4){return(Laplace.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Laplace",language);} //Invalid parameters
			}
			else if(params.length==3){ // (x,mu,b,f)
				if(df==0){return(Laplace.pdf(params, language));} //PDF
				else if(df==1){return(Laplace.cdf(params, language));} //CDF
				else if(df==2){return(Laplace.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Laplace",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Laplace",language);} //Incorrect number of parameters
		}
		case "Logistic": {
			if(params.length==2){ //(mu,s,~)
				if(df==-1 || df==3){return(Logistic.mean(params, language));} //Sample, Mean
				else if(df==4){return(Logistic.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Logistic",language);} //Invalid parameters
			}
			else if(params.length==3){ //(x,mu,s,f)
				if(df==0){return(Logistic.pdf(params, language));} //PDF
				else if(df==1){return(Logistic.cdf(params, language));} //CDF
				else if(df==2){return(Logistic.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Logistic",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Logistic",language);} //Incorrect number of parameters
		}
		case "LogNorm": {
			if(params.length==2){ //(mu,sigma,~)
				if(df==-1 || df==3){return(LogNormal.mean(params, language));} //Sample, Mean
				else if(df==4){return(LogNormal.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"LogNorm",language);} //Invalid parameters
			}
			else if(params.length==3){ //(x,mu,sigma,f)
				if(df==0){return(LogNormal.pdf(params, language));} //PDF
				else if(df==1){return(LogNormal.cdf(params, language));} //CDF
				else if(df==2){return(LogNormal.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"LogNorm",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"LogNorm",language);} //Incorrect number of parameters
		}
		case "Norm": {
			if(params.length==2){ //(mu,sigma,~)
				if(df==-1 || df==3){return(Normal.mean(params, language));} //Sample, Mean
				else if(df==4){return(Normal.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Norm",language);} //Invalid parameters
			}
			else if(params.length==3){ //(x,mu,sigma,f)
				if(df==0){return(Normal.pdf(params, language));} //PDF
				else if(df==1){return(Normal.cdf(params, language));} //CDF
				else if(df==2){return(Normal.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Norm",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Norm",language);} //Incorrect number of parameters
		}
		case "Pareto": {
			if(params.length==2){ //(k,alpha,~)
				if(df==-1 || df==3){return(Pareto.mean(params, language));} //Sample, Mean
				else if(df==4){return(Pareto.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Pareto",language);} //Invalid parameters
			}
			else if(params.length==3){ //(x,k,alpha,f)
				if(df==0){return(Pareto.pdf(params, language));} //PDF
				else if(df==1){return(Pareto.cdf(params, language));} //CDF
				else if(df==2){return(Pareto.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Pareto",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Pareto",language);} //Incorrect number of parameters
		}
		case "PERT": { 
			if(params.length==3){ //(a,b,c,~)
				if(df==-1 || df==3){return(PERT.mean(params, language));} //Sample, Mean
				else if(df==4){return(PERT.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"PERT",language);} //Invalid parameters
			}
			else if(params.length==4){ //(x,a,b,c,f)
				if(df==0){return(PERT.pdf(params, language));} //PDF
				else if(df==1){return(PERT.cdf(params, language));} //CDF
				else if(df==2){return(PERT.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"PERT",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"PERT",language);} //Incorrect number of parameters
		}
		case "StudentT":{
			if(params.length==1){ //(nu,~)
				if(df==-1){return(new Numeric(0));} //Sample (median) 
				else if(df==3){return(StudentT.mean(params, language));} //Mean
				else if(df==4){return(StudentT.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"StudentT",language);} //Invalid parameters
			}
			else if(params.length==2){ //(x,nu,f)
				if(df==0){return(StudentT.pdf(params, language));} //PDF
				else if(df==1){return(StudentT.cdf(params, language));} //CDF
				else if(df==2){return(StudentT.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"StudentT",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"StudentT",language);} //Incorrect number of parameters
		}
		case "Tri": {
			if(params.length==3){ //(a,b,c,~)
				if(df==-1 || df==3){return(Triangular.mean(params, language));} //Sample, Mean
				else if(df==4){return(Triangular.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Tri",language);} //Invalid parameters
			}
			else if(params.length==4){ //(x,a,b,c,f)
				if(df==0){return(Triangular.pdf(params, language));} //PDF
				else if(df==1){return(Triangular.cdf(params, language));} //CDF
				else if(df==2){return(Triangular.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Tri",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Tri",language);} //Incorrect number of parameters
		}
		case "Unif": {
			if(params.length==2){ //(a,b,~)
				if(df==-1 || df==3){return(Uniform.mean(params, language));} //Sample, Mean
				else if(df==4){return(Uniform.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Unif",language);} //Invalid parameters
			}
			else if(params.length==3){ //(x,a,b,f)
				if(df==0){return(Uniform.pdf(params, language));} //PDF
				else if(df==1){return(Uniform.cdf(params, language));} //CDF
				else if(df==2){return(Uniform.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Unif",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Unif",language);} //Incorrect number of parameters
		}
		case "Weibull": {
			if(params.length==2){ //(a,b,~)
				if(df==-1 || df==3){return(Weibull.mean(params, language));} //Sample, Mean
				else if(df==4){return(Weibull.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Weibull",language);} //Invalid parameters
			}
			else if(params.length==3){ //(x,a,b,f)
				if(df==0){return(Weibull.pdf(params, language));} //PDF
				else if(df==1){return(Weibull.cdf(params, language));} //CDF
				else if(df==2){return(Weibull.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Weibull",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Weibull",language);} //Incorrect number of parameters
		}

		//Truncated
		case "TruncNorm": {
			if(params.length==4){ //(mu,sigma,a,b,~) return mean
				if(df==-1 || df==3){return(TruncatedNormal.mean(params, language));} //Sample, Mean
				else if(df==4){return(TruncatedNormal.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"TruncNorm",language);} //Invalid parameters
			}
			else if(params.length==5){ //(x,mu,sigma,a,b,f)
				if(df==0){return(TruncatedNormal.pdf(params, language));} //PDF
				else if(df==1){return(TruncatedNormal.cdf(params, language));} //CDF
				else if(df==2){return(TruncatedNormal.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"TruncNorm",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"TruncNorm",language);} //Incorrect number of parameters
		}
		
		//Multivariate
		case "Dir": {
			if(params.length==1){ //(alpha) return mean
				if(df==-1 || df==3){return(Dirichlet.mean(params, language));} //Sample, Mean
				else if(df==4){return(Dirichlet.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Dir",language);} //Invalid parameters
			}
			else if(params.length==2){ //(x,alpha,f)
				if(df==0){return(Dirichlet.pdf(params, language));} //PDF
				else if(df==1){return(Dirichlet.cdf(params, language));} //CDF
				else if(df==2){return(Dirichlet.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Dir",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Dir",language);} //Incorrect number of parameters
		}
		case "MvNorm": {
			if(params.length==2){ //(mu,sigma) return mean
				if(df==-1 || df==3){return(MultivariateNormal.mean(params, language));} //Sample, Mean
				else if(df==4){return(MultivariateNormal.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"MvNorm",language);} //Invalid parameters
			}
			else if(params.length==3){ //(x,mu,sigma,f)
				if(df==0){return(MultivariateNormal.pdf(params, language));} //PDF
				else if(df==1){return(MultivariateNormal.cdf(params, language));} //CDF
				else if(df==2){return(MultivariateNormal.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"MvNorm",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"MvNorm",language);} //Incorrect number of parameters
		}
		case "Multi": {
			if(params.length==2){ //(n,p,~) return mean
				if(df==-1 || df==3){return(Multinomial.mean(params, language));} //Sample, Mean
				else if(df==4){return(Multinomial.variance(params, language));} //Variance
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Multi",language);} //Invalid parameters
			}
			else if(params.length==3){ //(k,n,p,f)
				if(df==0){return(Multinomial.pmf(params, language));} //PMF
				else if(df==1){return(Multinomial.cdf(params, language));} //CDF
				else if(df==2){return(Multinomial.quantile(params, language));} //Quantile
				else{throw new NumericException(language.message.getString("err.invalid_params"),"Multi",language);} //Invalid parameters
			}
			else{throw new NumericException(language.message.getString("err.incorrect_num_params"),"Multi",language);} //Incorrect number of parameters
		}

		} //End switch
		return(null);
	}

	public static Numeric sample(String dist, Numeric params[], MersenneTwisterFast generator, Language language) throws NumericException{
		switch(dist){
		//Discrete
		case "Bern": return(Bernoulli.sample(params, generator, language));
		case "Bin": return(Binomial.sample(params, generator, language));
		case "Cat": return(Categorical.sample(params, generator, language));
		case "DUnif": return(DiscreteUniform.sample(params, generator, language));
		case "Geom": return(Geometric.sample(params, generator, language));
		case "HGeom": return(Hypergeometric.sample(params, generator, language));
		case "NBin": return(NegativeBinomial.sample(params, generator, language));
		case "Pois": return(Poisson.sample(params, generator, language));
		case "Zipf": return(Zipf.sample(params, generator, language));
		//Continuous
		case "Beta": return(Beta.sample(params, generator, language));
		case "Cauchy": return(Cauchy.sample(params, generator, language));
		case "ChiSq": return(ChiSquare.sample(params, generator, language));
		case "Expo": return(Exponential.sample(params, generator, language));
		case "Gamma": return(Gamma.sample(params, generator, language));
		case "Gumbel": return(Gumbel.sample(params, generator, language));
		case "HalfCauchy": return(HalfCauchy.sample(params, generator, language));
		case "HalfNorm": return(HalfNormal.sample(params, generator, language));
		case "Laplace": return(Laplace.sample(params, generator, language));
		case "Logistic": return(Logistic.sample(params, generator, language));
		case "LogNorm": return(LogNormal.sample(params, generator, language));
		case "Norm": return(Normal.sample(params, generator, language));
		case "Pareto": return(Pareto.sample(params, generator, language));
		case "PERT": return(PERT.sample(params, generator, language));
		case "StudentT": return(StudentT.sample(params, generator, language));
		case "Tri": return(Triangular.sample(params, generator, language));
		case "Unif": return(Uniform.sample(params, generator, language));
		case "Weibull": return(Weibull.sample(params, generator, language));
		//Truncated
		case "TruncNorm": return(TruncatedNormal.sample(params, generator, language));
		//Multivariate
		case "Dir": return(Dirichlet.sample(params, generator, language));
		case "MvNorm": return(MultivariateNormal.sample(params, generator, language));
		case "Multi": return(Multinomial.sample(params, generator, language));
	
		} //End switch
		return(null);
	}
	
	public static String getDescription(String dist, Language language){
		String des=null;
		switch(dist){
		//Discrete
		case "Bern": return(Bernoulli.description(language));
		case "Bin": return(Binomial.description(language));
		case "Cat": return(Categorical.description(language));
		case "DUnif": return(DiscreteUniform.description(language));
		case "Geom": return(Geometric.description(language));
		case "HGeom": return(Hypergeometric.description(language));
		case "NBin": return(NegativeBinomial.description(language));
		case "Pois": return(Poisson.description(language));
		case "Zipf": return(Zipf.description(language));
		//Continuous
		case "Beta": return(Beta.description(language)); 
		case "Cauchy": return(Cauchy.description(language));
		case "ChiSq": return(ChiSquare.description(language));
		case "Expo": return(Exponential.description(language));
		case "Gamma": return(Gamma.description(language));
		case "Gumbel": return(Gumbel.description(language));
		case "HalfCauchy": return(HalfCauchy.description(language));
		case "HalfNorm": return(HalfNormal.description(language));
		case "Laplace": return(Laplace.description(language));
		case "Logistic": return(Logistic.description(language));
		case "LogNorm": return(LogNormal.description(language));
		case "Norm": return(Normal.description(language));
		case "Pareto": return(Pareto.description(language));
		case "PERT": return(PERT.description(language));
		case "StudentT": return(StudentT.description(language));
		case "Tri": return(Triangular.description(language));
		case "Unif": return(Uniform.description(language));
		case "Weibull": return(Weibull.description(language));
		//Truncated
		case "TruncNorm": return(TruncatedNormal.description(language));
		//Multivariate
		case "Dir": return(Dirichlet.description(language));
		case "MvNorm": return(MultivariateNormal.description(language));
		case "Multi": return(Multinomial.description(language));

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