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

public class JavaMarkovCohort{

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
	public JavaMarkovCohort(String dir1,AmuaModel myModel1,int tableFormat){
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
			javaModel.writeVariables();

			writeLine("		//Initialize chain elements");
			writeLine("		int t;");
			writeLine("		boolean terminate;");
			writeLine("		int numStates;");
			writeLine("		String stateNames[];");
			writeLine("		MarkovTrace trace;");
			writeLine("		double curPrev[], newPrev[];");
			writeLine("		int cohortSize="+myModel.cohortSize+";");
			writeLine("		double childProbs[];");
			writeLine("		double sumProb;");
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
				if(curNode.type==1){chains.add(curNode);}
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
				writeLine("		numStates="+numStates+";");
				out.write("		stateNames=new String[]{");
				for(int s=0; s<numStates-1; s++){out.write("\""+states[s].name+"\",");}
				out.write("\""+states[numStates-1].name+"\"};"); out.newLine();
				writeLine("		trace=outer.new MarkovTrace(\""+curChain.name+"\",stateNames); //initialize trace");
				writeLine("		//Initialize prevalence");
				writeLine("		curPrev=new double[numStates]; newPrev=new double[numStates];");
				for(int s=0; s<numStates; s++){
					writeLine("		curPrev["+s+"]=cohortSize*"+javaModel.translate(states[s].prob,false)+"; newPrev["+s+"]=curPrev["+s+"]; //"+states[s].name);
				}
				if(numVars>0){
					writeLine("		//Initialize variables");
					for(int v=0; v<numVars; v++){
						Variable curVar=myModel.variables.get(v);
						writeLine("		"+curVar.name+"="+javaModel.translate(curVar.initValue,false)+";");
					}
				}
				writeLine("");
				writeLine("		//Run chain");
				writeLine("		//Initialize outcomes");
				for(int d=0; d<numDimensions; d++){
					writeLine("		double "+curChain.nameExport+"_"+dimNames[d]+"=0, "+curChain.nameExport+"_Dis_"+dimNames[d]+"=0;");
				}
				writeLine("		t=0; //initialize cycle");
				writeLine("		terminate=false;");
				writeLine("		while(terminate==false){");
				writeLine("			//update progress");
				writeLine("			if(t%10==0){System.out.print(t);}");
				writeLine("			else{System.out.print(\".\");}");
				writeLine("");
				writeLine("			//Cycle outcomes");
				for(int d=0; d<numDimensions; d++){
					writeLine("			double cycle"+dimNames[d]+"=0, cycle"+dimNames[d]+"_dis=0;");
				}
				writeLine("");
				writeLine("			//Update prevalence");
				writeLine("			for(int s=0; s<numStates; s++){");
				writeLine("				curPrev[s]=newPrev[s];");
				writeLine("			}");
				writeLine("			//Simulate state transitions");
				for(int s=0; s<numStates; s++){
					writeLine("");
					defineNode(states[s],s);
				}
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
				out.write("			trace.update(curPrev");
				for(int d=0; d<numDimensions; d++){out.write(", cycle"+dimNames[d]+", cycle"+dimNames[d]+"_dis");}
				out.write(");"); out.newLine();
				writeLine("");
				writeLine("			//Check termination condition");
				writeLine("			terminate = ("+javaModel.translate(curChain.terminationCondition, false)+");");
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
				writeLine("		System.out.println(\"done!\");");
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
		
			javaModel.writeMarkovTrace();
			
			out.write("}"); //Close class
			out.close();

		}catch(Exception e){
			recordError(e);
		}
	}

	private void defineNode(MarkovNode curNode, int stateIndex){
		try{
			//int level=(curNode.level-curNode.chain.level)+2; //tab indents
			int level=3;
			writeLine("//"+curNode.name,level);
			
			String localPrev;
			
			if(curNode.type==2){ //State, update rewards
				writeLine("//Update rewards",level);
				for(int d=0; d<numDimensions; d++){
					String dim="cycle"+dimNames[d];
					writeLine(dim+"+=curPrev["+stateIndex+"]*"+javaModel.translate(curNode.rewards[d],false)+";",level);
				}
				localPrev="curPrev["+stateIndex+"]";
			}
			else{
				localPrev="prev_"+curNode.nameExport;
			}
			
			if(curNode.hasVarUpdates){ //update variables
				writeLine("//Update variables",level);
				String updates[]=curNode.varUpdates.split(";");
				int numUpdates=updates.length;
				for(int u=0; u<numUpdates; u++){
					writeLine(javaModel.translate(updates[u],false)+"; //Orig: "+updates[u], level);
				}
			}

			if(curNode.hasCost){ //add cost
				writeLine("//Update costs",level);
				for(int d=0; d<numDimensions; d++){
					String dim="cycle"+dimNames[d];
					writeLine(dim+"+="+localPrev+"*"+javaModel.translate(curNode.cost[d],false)+";",level);
				}
			}

			int numChildren=curNode.childIndices.size();
			if(numChildren==0){ //transition node
				int indexTo=getStateIndex(curNode.transition);
				writeLine("//Transition to "+states[indexTo].name,level);
				writeLine("newPrev["+stateIndex+"]-="+localPrev+";",level);
				writeLine("newPrev["+indexTo+"]+="+localPrev+";",level);
			}
			else{
				MarkovNode children[]=new MarkovNode[numChildren];
				for(int i=0; i<numChildren; i++){
					int index=curNode.childIndices.get(i);
					children[i]=markov.nodes.get(index);
				}
				writeLine("//Calculate child probs",level);
				writeLine("sumProb=0;",level);
				writeLine("childProbs=new double["+numChildren+"];",level);
				int compIndex=-1;
				for(int i=0; i<numChildren; i++){
					String prob=children[i].prob;
					if(prob.matches("C") || prob.matches("c")){compIndex=i;} //Complementary
					else{
						writeLine("childProbs["+i+"]="+javaModel.translate(prob, false)+"; sumProb+=childProbs["+i+"]; //Prob "+children[i].name,level);
					}
				}
				if(compIndex!=-1){
					writeLine("childProbs["+compIndex+"]=1.0-sumProb; //Complementary prob",level);
				}
				for(int i=0; i<numChildren; i++){
					String childPrev="prev_"+children[i].nameExport;
					writeLine("double "+childPrev+"="+localPrev+"*childProbs["+i+"];",level);
				}
				for(int i=0; i<numChildren; i++){
					defineNode(children[i],stateIndex);
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
