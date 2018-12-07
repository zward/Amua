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

package export_Java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import base.AmuaModel;
import main.*;
import markov.MarkovNode;
import markov.MarkovTree;

public class JavaMarkovMonteCarlo{

	BufferedWriter out;
	AmuaModel myModel;
	MarkovTree markov;
	ErrorLog errorLog;
	int numDimensions;
	int tableFormat;
	String dir;
	JavaModel javaModel;
	String dimNames[];
	MarkovNode states[];

	//Constructor
	public JavaMarkovMonteCarlo(String dir1,AmuaModel myModel1,int tableFormat){
		try{
			myModel=myModel1;
			markov=myModel.markov;
			errorLog=myModel.errorLog;
			this.tableFormat=tableFormat;
			new File(dir1).mkdir(); //make directory
			this.dir=dir1+File.separator;
			dimNames=myModel.dimInfo.dimNames;
			numDimensions=dimNames.length;
			//Open file for writing
			FileWriter fstream;
			fstream = new FileWriter(dir+"main.java"); //Create new file
			out = new BufferedWriter(fstream);

			javaModel=new JavaModel(dir,out,myModel);

			javaModel.writeProperties();
			writeLine("");
			if(tableFormat==1){ //csv
				writeLine("import java.io.File;");
			}
			writeLine("import java.io.BufferedWriter;");
			writeLine("import java.io.FileWriter;");
			writeLine("import java.util.ArrayList;");
			writeLine("import java.util.Random;");
			writeLine("");
			writeLine("public class main {");
			writeLine("");	
			writeLine("	public static void main(String[] args) {");
			writeLine("		//Instantiate main (outer) class");
			writeLine("		main outer=new main();");
			writeLine("");
			
			if(myModel.tables.size()>0){
				javaModel.writeTableClass();
				javaModel.writeTables(tableFormat);
			}

			javaModel.writeParameters();
			int numVars=myModel.variables.size();

			writeLine("		//Intialize Random Number Generator");
			writeLine("		Random generator=new Random(); //built-in Java RNG - feel free to swap out");
			writeLine("		double rand;");
			writeLine("		//Initialize chain elements");
			writeLine("		int t;");
			writeLine("		boolean terminate;");
			writeLine("		double childProbs[];");
			writeLine("		int numStates;");
			writeLine("		String stateNames[];");
			writeLine("		MarkovTrace trace;");
			writeLine("		double initPrev[], cumPrev[];");
			writeLine("		int numPeople;");
			writeLine("		Person people[];");
			
			writeLine("");
			writeLine("		//Initialize discount rates");
			out.write("		double discountRates[]=new double[]{");
			if(myModel.markov.discountRewards){
				for(int d=0; d<numDimensions-1; d++){out.write(myModel.markov.discountRates[d]/100.0+",");}
				out.write(myModel.markov.discountRates[numDimensions-1]/100.0+"};"); out.newLine();
				writeLine("		int startDiscountCycle="+myModel.markov.discountStartCycle+";");
			}
			else{
				for(int d=0; d<numDimensions-1; d++){out.write("0,");}
				out.write("0};");  out.newLine();
			}
			writeLine("		boolean halfCycle="+myModel.markov.halfCycleCorrection+"; //half-cycle correction");
			
			//Define Markov Chains
			ArrayList<MarkovNode> chains=new ArrayList<MarkovNode>();
			for(int n=0; n<markov.nodes.size(); n++){
				MarkovNode curNode=markov.nodes.get(n);
				if(curNode.type==1){
					chains.add(curNode);
				}
			}
					
			for(int c=0; c<chains.size(); c++){ //for each chain
				MarkovNode curChain=chains.get(c);
				int numStates=curChain.childIndices.size();
				states=new MarkovNode[numStates];
				for(int s=0; s<numStates; s++){
					int index=curChain.childIndices.get(s);
					states[s]=markov.nodes.get(index);
				}
				writeLine("");
				writeLine("		//********** Markov Chain: "+curChain.name+" **********");
				writeLine("		System.out.print(\"Running Markov Chain: "+curChain.name+" \");");
				if(myModel.CRN){
					writeLine("		generator.setSeed("+myModel.crnSeed+");");
				}
				writeLine("		numStates="+numStates+";");
				out.write("		stateNames=new String[]{");
				for(int s=0; s<numStates-1; s++){
					out.write("\""+states[s].name+"\",");
				}
				out.write("\""+states[numStates-1].name+"\"};"); out.newLine();
				writeLine("		trace=outer.new MarkovTrace(\""+curChain.name+"\",stateNames); //initialize trace");
				writeLine("		//Initialize prevalence");
				out.write("		initPrev=new double[]{");
				for(int s=0; s<numStates-1; s++){
					out.write(javaModel.translate(states[s].prob,true)+",");
				}
				out.write(javaModel.translate(states[numStates-1].prob,true)+"};"); out.newLine();
				writeLine("		cumPrev=new double[numStates];");
				writeLine("		cumPrev[0]=initPrev[0];");
				writeLine("		for(int s=1; s<numStates; s++){");
				writeLine("			cumPrev[s]=cumPrev[s-1]+initPrev[s];");
				writeLine("		}");
				writeLine("");
				writeLine("		numPeople="+myModel.cohortSize+";");
				writeLine("		people=new Person[numPeople];");
				writeLine("		for(int p=0; p<numPeople; p++){");
				writeLine("			people[p]=outer.new Person();");
				writeLine("			rand=generator.nextDouble();");
				writeLine("			int k=0;");
				writeLine("			while(rand>initPrev[k]){k++;}");
				writeLine("			people[p].state=k;");
				if(numVars>0){
					writeLine("			//Initialize variables");
					for(int v=0; v<numVars; v++){
						Variable curVar=myModel.variables.get(v);
						writeLine("			people[p]."+curVar.name+"="+javaModel.translate(curVar.initValue,true)+";");
					}
				}
				writeLine("		}");
				writeLine("");
				writeLine("		//Run chain");
				writeLine("		//Initialize outcomes");
				for(int d=0; d<numDimensions; d++){
					writeLine("		double "+curChain.nameExport+"_"+dimNames[d]+"=0, "+curChain.nameExport+"_Dis_"+dimNames[d]+"=0;");
				}
				writeLine("		t=0; //initialize cycle");
				writeLine("		terminate=false;");
				writeLine("		while(terminate==false){");
				writeLine("			//Update progress");
				writeLine("			if(t%10==0){System.out.print(t);}");
				writeLine("			else{System.out.print(\".\");}");
				writeLine("");
				writeLine("			//Cycle outcomes");
				writeLine("			double prev[]=new double[numStates];");
				for(int d=0; d<numDimensions; d++){
					writeLine("			double cycle"+dimNames[d]+"=0, cycle"+dimNames[d]+"_dis=0;");
				}
				writeLine("");
				writeLine("			//Update each person");
				writeLine("			for(int p=0; p<numPeople; p++){");
				writeLine("				Person curPerson=people[p];");
				writeLine("				int curState=curPerson.state;");
				writeLine("				prev[curState]++; //record prevalence");
				writeLine("				if(curState==0){ //"+states[0].name);
				defineNode(states[0]);
				writeLine("				}");
				for(int s=1; s<numStates; s++){
					writeLine("				else if(curState=="+s+"){ //"+states[s].name);
					defineNode(states[s]);
					writeLine("				}");
				}
				writeLine("			} //end person for loop");
				writeLine("");
				writeLine("			//Update outcomes");
				for(int d=0; d<numDimensions; d++){
					writeLine("			double discount=1.0;");
					writeLine("			if(t>=startDiscountCycle){");
					writeLine("				discount=1.0/Math.pow(1.0+discountRates["+d+"],(t-startDiscountCycle)+1);");
					writeLine("			}");
					writeLine("			cycle"+dimNames[d]+"_dis=cycle"+dimNames[d]+"*discount;");
					writeLine("			if(t==0 && halfCycle){ //half-cycle correction");
					writeLine("				cycle"+dimNames[d]+"*=0.5; cycle"+dimNames[d]+"_dis*=0.5;");
					writeLine("			}");
					writeLine("			"+curChain.nameExport+"_"+dimNames[d]+"+=cycle"+dimNames[d]+";");
					writeLine("			"+curChain.nameExport+"_Dis_"+dimNames[d]+"+=cycle"+dimNames[d]+"_dis;");
				}
				writeLine("");
				writeLine("			//Update trace");
				out.write("			trace.update(prev");
				for(int d=0; d<numDimensions; d++){out.write(", cycle"+dimNames[d]+", cycle"+dimNames[d]+"_dis");}
				out.write(");"); out.newLine();
				writeLine("");
				writeLine("			//Check termination condition");
				writeLine("			terminate = ("+javaModel.translate(curChain.terminationCondition, true)+");");
				writeLine("			if(terminate && halfCycle){ //half cycle-correction, update last trace row");
				writeLine("				trace.applyHalfCycle();");
				writeLine("				//Update cum rewards)");
				for(int d=0; d<numDimensions; d++){
					writeLine("				"+curChain.nameExport+"_"+dimNames[d]+"-=cycle"+dimNames[d]+"*0.5;");
					writeLine("				"+curChain.nameExport+"_Dis_"+dimNames[d]+"-=cycle"+dimNames[d]+"_dis*0.5;");
				}
				writeLine("			}");
				writeLine("");
				writeLine("			t++; //next cycle");
				writeLine("");
				writeLine("		} //end cycle while loop");
				writeLine("		trace.writeCSV(); //write trace");
				writeLine("		System.out.println(\" done!\");");
				writeLine("");
				writeLine("		//Report totals");
				for(int d=0; d<numDimensions; d++){
					writeLine("		System.out.println(\""+dimNames[d]+": \"+"+curChain.nameExport+"_"+dimNames[d]+");");
					writeLine("		System.out.println(\""+dimNames[d]+" (Discounted): \"+"+curChain.nameExport+"_Dis_"+dimNames[d]+");");
				}
				writeLine("		System.out.println();");
			}
			
			out.write("	}"); out.newLine(); //end method
			out.newLine();
			
			javaModel.defineFunctions();
			
			//Inner classes
			writeLine("	//Define inner class");
			writeLine("	class Person{");
			writeLine("		//Attributes");
			writeLine("		int state;");
			for(int v=0; v<numVars; v++){
				Variable curVar=myModel.variables.get(v);
				writeLine("		"+javaModel.defNumeric(curVar.name,curVar.value));
			}
			writeLine("");
			writeLine("		//Constructor");
			writeLine("		public Person(){");
			writeLine("		}");
			writeLine("	}"); //end person class
			writeLine("");
			
			javaModel.writeMarkovTrace();
			
			out.write("}"); //Close class
			out.close();

		}catch(Exception e){
			recordError(e);
		}
	}

	private void defineNode(MarkovNode curNode){
		try{

			int level=(curNode.level-curNode.chain.level)+4; //tab indents

			if(curNode.type==2){ //State, update rewards
				writeLine("//Update rewards",level);
				for(int d=0; d<numDimensions; d++){
					String dim="cycle"+dimNames[d];
					writeLine(dim+"+="+javaModel.translate(curNode.rewards[d],true)+";",level);
				}
			}
			
			if(curNode.hasVarUpdates){ //update variables
				writeLine("//Update variables",level);
				String updates[]=curNode.varUpdates.split(";");
				int numUpdates=updates.length;
				for(int u=0; u<numUpdates; u++){
					writeLine(javaModel.translate(updates[u],true)+"; //Orig: "+updates[u], level);
				}
			}

			if(curNode.hasCost){ //add cost
				writeLine("//Update costs",level);
				for(int d=0; d<numDimensions; d++){
					String dim="cycle"+dimNames[d];
					writeLine(dim+"+="+javaModel.translate(curNode.cost[d],true)+";",level);
				}
			}

			int numChildren=curNode.childIndices.size();
			if(numChildren==0){ //transition node
				int indexTo=getStateIndex(curNode.transition);
				writeLine("curPerson.state="+indexTo+"; //Transition to "+states[indexTo].name,level);
			}
			else{
				MarkovNode children[]=new MarkovNode[numChildren];
				for(int i=0; i<numChildren; i++){
					int index=curNode.childIndices.get(i);
					children[i]=markov.nodes.get(index);
				}
				writeLine("//Calculate child probs",level);
				writeLine("childProbs=new double["+numChildren+"];",level);
				int compIndex=-1;
				for(int i=0; i<numChildren; i++){
					String prob=children[i].prob;
					if(prob.matches("C") || prob.matches("c")){compIndex=i;} //Complementary
					else{
						if(i==0){writeLine("childProbs[0]="+javaModel.translate(prob,true)+"; //Prob "+children[i].name,level);}
						else{writeLine("childProbs["+i+"]=childProbs["+(i-1)+"]+"+javaModel.translate(prob,true)+"; //Prob "+children[i].name,level);}
					}
				}
				
				writeLine("//Sample event",level);
				writeLine("rand=generator.nextDouble();",level);
				if(compIndex==-1){ //no comp prob
					writeLine("if(rand<childProbs[0]){ //"+children[0].name,level);
					defineNode(children[0]);
					writeLine("} //end "+children[0].name,level);
					for(int i=1; i<numChildren; i++){
						writeLine("else if(rand<childProbs["+i+"]){ //"+children[i].name,level);
						defineNode(children[i]);
						writeLine("} //end "+children[i].name,level);
					}
				}
				else{ //write comp prob as last 'else'
					if(compIndex!=0){
						writeLine("if(rand<childProbs[0]){ //"+children[0].name,level);
						defineNode(children[0]);
						writeLine("} //end "+children[0].name,level);
						for(int i=1; i<numChildren; i++){
							if(compIndex!=i){
								writeLine("else if(rand<childProbs["+i+"]){ //"+children[i].name,level);
								defineNode(children[i]);
								writeLine("} //end "+children[i].name,level);
							}
						}
					}
					else{ //compIndex == 0
						writeLine("if(rand<childProbs[1]){ //"+children[1].name,level);
						defineNode(children[1]);
						writeLine("} //end "+children[1].name,level);
						for(int i=2; i<numChildren; i++){
							if(compIndex!=i){
								writeLine("else if(rand<childProbs["+i+"]){ //"+children[i].name,level);
								defineNode(children[i]);
								writeLine("} //end "+children[i].name,level);
							}
						}
					}
					//write comp index
					writeLine("else{ //"+children[compIndex].name,level);
					defineNode(children[compIndex]);
					writeLine("} //end "+children[compIndex].name,level);
				}
			}
		}catch(Exception e){
			recordError(e);
		}
	}

	private int getStateIndex(String name){
		int index=-1;
		boolean found=false;
		while(found==false){
			index++;
			if(states[index].name.equals(name)){
				found=true;
			}
		}
		return(index);
	}
	
	
	private void writeLine(String line){
		try{
			out.write(line); out.newLine();
		}catch(Exception e){
			recordError(e);
		}
	}

	private void writeLine(String line, int tab){
		try{
			for(int t=0; t<tab; t++){out.write("	");}
			out.write(line); out.newLine();
		}catch(Exception e){
			recordError(e);
		}
	}

	private void recordError(Exception e){
		e.printStackTrace();
		errorLog.recordError(e);
		JOptionPane.showMessageDialog(null, e.toString());
	}
}
