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

package export_Cpp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import base.AmuaModel;
import main.*;
import markov.MarkovNode;
import markov.MarkovTree;

public class CppMarkovMonteCarlo{

	BufferedWriter out;
	AmuaModel myModel;
	MarkovTree markov;
	ErrorLog errorLog;
	int numDimensions;
	int tableFormat;
	String dir;
	CppModel cppModel;
	String dimNames[];
	MarkovNode states[];

	//Constructor
	public CppMarkovMonteCarlo(String dir1,AmuaModel myModel1,int tableFormat){
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
			fstream = new FileWriter(dir+"main.cpp"); //Create new file
			out = new BufferedWriter(fstream);

			cppModel=new CppModel(dir,out,myModel);

			cppModel.writeProperties();
			
			writeLine("");
			writeLine("#include <fstream>");
			writeLine("#include <iostream>");
			writeLine("#include <string>");
			writeLine("#include <vector>");
			writeLine("#include <math.h>");
			writeLine("#include \"functions.h\"");
			if(myModel.tables.size()>0){
				writeLine("#include \"Table.h\"");
			}
			writeLine("#include \"MarkovTrace.h\"");
			writeLine("#include <random>");
			writeLine("");
			writeLine("using namespace std;");
			writeLine("");
			
			//Inner classes
			writeLine("//Define inner class");
			writeLine("class Person{");
			writeLine("	//Attributes");
			writeLine("	public:");
			writeLine("		int state;");
			int numVars=myModel.variables.size();
			for(int v=0; v<numVars; v++){
				Variable curVar=myModel.variables.get(v);
				writeLine("		"+cppModel.defNumeric(curVar.name,curVar.value[0]));
			}
			writeLine("");
			writeLine("	//Constructor");
			writeLine("	public: Person(){}");
			writeLine("};"); //end person class
			writeLine("");
			
			writeLine("int main(int argc, char * argv[]) {");
			
			cppModel.writeTableClass();
			cppModel.writeTables(tableFormat);

			cppModel.writeParameters();
			
			writeLine("	//Intialize Random Number Generator");
			writeLine("	mt19937 generator; //built-in RNG - feel free to swap out");
			writeLine("	uniform_real_distribution<double> unif(0.0,1.0);");
			writeLine("	double rand;");
			writeLine("");
			writeLine("	//Initialize chain elements");
			writeLine("	int t;");
			writeLine("	bool terminate;");
			writeLine("	double * childProbs=new double[2]; //temp array to delete");
			writeLine("	int numStates;");
			writeLine("	string * stateNames;");
			writeLine("	MarkovTrace * trace;");
			writeLine("	double * initPrev, * cumPrev;");
			writeLine("	int numPeople;");
			writeLine("	Person * people;");
			
			writeLine("");
			writeLine("	//Initialize discount rates");
			writeLine("	int startDiscountCycle="+myModel.markov.discountStartCycle+";");
			out.write("	double discountRates["+numDimensions+"]={");
			if(myModel.markov.discountRewards){
				for(int d=0; d<numDimensions-1; d++){out.write(myModel.markov.discountRates[d]/100.0+",");}
				out.write(myModel.markov.discountRates[numDimensions-1]/100.0+"};"); out.newLine();
			}
			else{
				for(int d=0; d<numDimensions-1; d++){out.write("0,");}
				out.write("0};");  out.newLine();
			}
			writeLine("	bool halfCycle="+myModel.markov.halfCycleCorrection+"; //half-cycle correction");
			
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
				writeLine("	//********** Markov Chain: "+curChain.name+" **********");
				writeLine("	cout<<\"Running Markov Chain: "+curChain.name+" \";");
				if(myModel.CRN){
					writeLine("	generator.seed("+myModel.crnSeed+");");
				}
				writeLine("	numStates="+numStates+";");
				out.write("	stateNames=new string[numStates]{");
				for(int s=0; s<numStates-1; s++){
					out.write("\""+states[s].name+"\",");
				}
				out.write("\""+states[numStates-1].name+"\"};"); out.newLine();
				writeLine("	trace=new MarkovTrace(\""+curChain.name+"\",numStates,stateNames); //initialize trace");
				writeLine("	//Initialize prevalence");
				out.write("	initPrev=new double[numStates]{");
				for(int s=0; s<numStates-1; s++){
					out.write(cppModel.translate(states[s].prob,true)+",");
				}
				out.write(cppModel.translate(states[numStates-1].prob,true)+"};"); out.newLine();
				writeLine("	cumPrev=new double[numStates];");
				writeLine("	cumPrev[0]=initPrev[0];");
				writeLine("	for(int s=1; s<numStates; s++){");
				writeLine("		cumPrev[s]=cumPrev[s-1]+initPrev[s];");
				writeLine("	}");
				writeLine("");
				writeLine("	numPeople="+myModel.cohortSize+";");
				writeLine("	people=new Person[numPeople];");
				writeLine("	for(int p=0; p<numPeople; p++){");
				writeLine("		Person & curPerson=people[p];");
				writeLine("		rand=unif(generator);");
				writeLine("		int k=0;");
				writeLine("		while(rand>initPrev[k]){k++;}");
				writeLine("		people[p].state=k;");
				if(numVars>0){
					writeLine("		//Initialize variables");
					//independent vars
					for(int v=0; v<numVars; v++){
						Variable curVar=myModel.variables.get(v);
						if(curVar.independent==true){
							writeLine("		people[p]."+curVar.name+"="+cppModel.translate(curVar.expression,true)+";");
						}
					}
					//dependent vars
					for(int v=0; v<numVars; v++){
						Variable curVar=myModel.variables.get(v);
						if(curVar.independent==false){
							writeLine("		people[p]."+curVar.name+"="+cppModel.translate(curVar.expression,true)+";");
						}
					}
				}
				writeLine("	}");
				writeLine("");
				writeLine("	//Run chain");
				writeLine("	//Initialize outcomes");
				for(int d=0; d<numDimensions; d++){
					writeLine("	double "+curChain.nameExport+"_"+dimNames[d]+"=0, "+curChain.nameExport+"_Dis_"+dimNames[d]+"=0;");
				}
				writeLine("	t=0; //initialize cycle");
				writeLine("	terminate=false;");
				writeLine("	while(terminate==false){");
				writeLine("		//Update progress");
				writeLine("		if(t%10==0){cout<<t;}");
				writeLine("		else{cout<<\".\";}");
				writeLine("");
				writeLine("		//Cycle outcomes");
				out.write("		double prev["+numStates+"]={0");
				for(int s=1; s<numStates; s++){out.write(",0");}
				out.write("};"); out.newLine();
				for(int d=0; d<numDimensions; d++){
					writeLine("		double cycle"+dimNames[d]+"=0, cycle"+dimNames[d]+"_dis=0;");
				}
				writeLine("");
				writeLine("		//Update each person");
				writeLine("		for(int p=0; p<numPeople; p++){");
				writeLine("			Person & curPerson=people[p];");
				writeLine("			int curState=curPerson.state;");
				writeLine("			prev[curState]++; //record prevalence");
				
				//cycle variable updates
				if(curChain.hasVarUpdates){
					writeLine("");
					writeLine("			//Cycle variable updates");
					String updates[]=curChain.varUpdates.split(";");
					int numUpdates=updates.length;
					ArrayList<Variable> dependents=new ArrayList<Variable>();
					for(int u=0; u<numUpdates; u++){
						writeLine(cppModel.translate(updates[u],true)+"; //Orig: "+updates[u], 3);
						for(int d=0; d<curChain.curVariableUpdates[u].variable.dependents.size(); d++){
							Variable curDep=curChain.curVariableUpdates[u].variable.dependents.get(d);
							if(!dependents.contains(curDep)){
								dependents.add(curDep);
							}
						}
					}
					//update dependent variables
					if(dependents.size()>0){
						writeLine("			//Update dependent variables");
						for(int d=0; d<dependents.size(); d++){
							Variable curVar=dependents.get(d);
							writeLine("			curPerson."+curVar.name+"="+cppModel.translate(curVar.expression,true)+";");
						}
					}
					writeLine("");
				}
								
				writeLine("			if(curState==0){ //"+states[0].name);
				defineNode(states[0]);
				writeLine("			}");
				for(int s=1; s<numStates; s++){
					writeLine("			else if(curState=="+s+"){ //"+states[s].name);
					defineNode(states[s]);
					writeLine("			}");
				}
				writeLine("		} //end person for loop");
				writeLine("");
				writeLine("		//Update outcomes");
				for(int d=0; d<numDimensions; d++){
					writeLine("		double discount=1.0;");
					writeLine("		if(t>=startDiscountCycle){");
					writeLine("			discount=1.0/pow(1.0+discountRates["+d+"],(t-startDiscountCycle)+1);");
					writeLine("		}");
					writeLine("		cycle"+dimNames[d]+"_dis=cycle"+dimNames[d]+"*discount;");
					writeLine("		if(t==0 && halfCycle){ //half-cycle correction");
					writeLine("			cycle"+dimNames[d]+"*=0.5; cycle"+dimNames[d]+"_dis*=0.5;");
					writeLine("		}");
					writeLine("		"+curChain.nameExport+"_"+dimNames[d]+"+=cycle"+dimNames[d]+";");
					writeLine("		"+curChain.nameExport+"_Dis_"+dimNames[d]+"+=cycle"+dimNames[d]+"_dis;");
				}
				writeLine("");
				writeLine("		//Update trace");
				out.write("		trace->update(prev");
				for(int d=0; d<numDimensions; d++){out.write(", cycle"+dimNames[d]+", cycle"+dimNames[d]+"_dis");}
				out.write(");"); out.newLine();
				writeLine("");
				writeLine("		//Check termination condition");
				writeLine("		terminate = ("+cppModel.translate(curChain.terminationCondition, true)+");");
				writeLine("		if(terminate && halfCycle){ //half cycle-correction, update last trace row");
				writeLine("			trace->applyHalfCycle();");
				writeLine("			//Update cum rewards)");
				for(int d=0; d<numDimensions; d++){
					writeLine("			"+curChain.nameExport+"_"+dimNames[d]+"-=cycle"+dimNames[d]+"*0.5;");
					writeLine("			"+curChain.nameExport+"_Dis_"+dimNames[d]+"-=cycle"+dimNames[d]+"_dis*0.5;");
				}
				writeLine("		}");
				writeLine("");
				writeLine("		t++; //next cycle");
				writeLine("");
				writeLine("	} //end cycle while loop");
				writeLine("	trace->writeCSV(); //write trace");
				writeLine("	cout<<\" done!\\n\";");
				writeLine("");
				writeLine("	//Report totals");
				for(int d=0; d<numDimensions; d++){
					writeLine("	cout<<\""+dimNames[d]+": \"<<"+curChain.nameExport+"_"+dimNames[d]+"<<\"\\n\";");
					writeLine("	cout<<\""+dimNames[d]+" (Discounted): \"<<"+curChain.nameExport+"_Dis_"+dimNames[d]+"<<\"\\n\";");
				}
				writeLine("	cout<<\"\\n\";");
				//delete
				writeLine("");
				writeLine("	//free memory");
				writeLine("	delete[] stateNames;");
				writeLine("	delete trace;");
				writeLine("	delete[] initPrev; delete[] cumPrev;");
				writeLine("	delete[] people;");
			}
			writeLine("");
			writeLine("	delete[] childProbs;");
			cppModel.deleteTables();
			
			cppModel.defineFunctions();
			
			cppModel.writeMarkovTrace();
			
			out.write("}"); //Close class
			out.close();

		}catch(Exception e){
			recordError(e);
		}
	}

	private void defineNode(MarkovNode curNode){
		try{

			int level=(curNode.level-curNode.chain.level)+3; //tab indents

			if(curNode.type==2){ //State, update rewards
				writeLine("//Update rewards",level);
				for(int d=0; d<numDimensions; d++){
					String dim="cycle"+dimNames[d];
					writeLine(dim+"+="+cppModel.translate(curNode.rewards[d],true)+";",level);
				}
			}
			
			if(curNode.hasVarUpdates){ //update variables
				writeLine("//Update variables",level);
				String updates[]=curNode.varUpdates.split(";");
				int numUpdates=updates.length;
				ArrayList<Variable> dependents=new ArrayList<Variable>();
				for(int u=0; u<numUpdates; u++){
					writeLine(cppModel.translate(updates[u],true)+"; //Orig: "+updates[u], level);
					for(int d=0; d<curNode.curVariableUpdates[u].variable.dependents.size(); d++){
						Variable curDep=curNode.curVariableUpdates[u].variable.dependents.get(d);
						if(!dependents.contains(curDep)){
							dependents.add(curDep);
						}
					}
				}
				//update dependent variables
				if(dependents.size()>0){
					writeLine("//Update dependent variables",level);
					for(int d=0; d<dependents.size(); d++){
						Variable curVar=dependents.get(d);
						writeLine("curPerson."+curVar.name+"="+cppModel.translate(curVar.expression,true)+";",level);
					}
				}
			}

			if(curNode.hasCost){ //add cost
				writeLine("//Update costs",level);
				for(int d=0; d<numDimensions; d++){
					String dim="cycle"+dimNames[d];
					writeLine(dim+"+="+cppModel.translate(curNode.cost[d],true)+";",level);
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
				writeLine("delete[] childProbs;",level);
				writeLine("childProbs=new double["+numChildren+"];",level);
				int compIndex=-1;
				for(int i=0; i<numChildren; i++){
					String prob=children[i].prob;
					if(prob.matches("C") || prob.matches("c")){compIndex=i;} //Complementary
					else{
						if(i==0){writeLine("childProbs[0]="+cppModel.translate(prob,true)+"; //Prob "+children[i].name,level);}
						else{writeLine("childProbs["+i+"]=childProbs["+(i-1)+"]+"+cppModel.translate(prob,true)+"; //Prob "+children[i].name,level);}
					}
				}
				
				writeLine("//Sample event",level);
				writeLine("rand=unif(generator);",level);
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
