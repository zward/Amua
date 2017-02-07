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

package export;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Date;
import main.*;


public class ExcelTree{

	BufferedWriter out;
	PanelTree panelTree;
	DecisionTree tree;
	ErrorLog errorLog;
	int numDim;
	int numVars;
	int numCols;

	//Constructor
	public ExcelTree(String filepath,PanelTree myPanel){
		try{
			panelTree=myPanel;
			tree=myPanel.tree;
			errorLog=myPanel.errorLog;
			numDim=tree.dimNames.length;
			filepath=filepath.replaceAll(".xml", "");
			//Open file for writing
			FileWriter fstream;
			fstream = new FileWriter(filepath+".xml"); //Create new file
			out = new BufferedWriter(fstream);

			//Write file heading
			writeLine("<?xml version=\"1.0\"?>");
			writeLine("<?mso-application progid=\"Excel.Sheet\"?>");
			writeLine("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"");
			writeLine(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"");
			writeLine(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"");
			writeLine(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"");
			writeLine(" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
			writeLine(" <DocumentProperties xmlns=\"urn:schemas-microsoft-com:office:office\">");
			writeLine("  <Author>"+System.getProperty("user.name")+"</Author>");
			writeLine("  <LastAuthor>"+System.getProperty("user.name")+"</LastAuthor>");
			writeLine("  <Created>"+new Date()+"</Created>");
			writeLine("  <Version>15.00</Version>");
			writeLine(" </DocumentProperties>");
			writeLine(" <OfficeDocumentSettings xmlns=\"urn:schemas-microsoft-com:office:office\">");
			writeLine("  <AllowPNG/>");
			writeLine(" </OfficeDocumentSettings>");
			writeLine(" <ExcelWorkbook xmlns=\"urn:schemas-microsoft-com:office:excel\">");
			writeLine("  <WindowHeight>5352</WindowHeight>");
			writeLine("  <WindowWidth>16488</WindowWidth>");
			writeLine("  <WindowTopX>0</WindowTopX>");
			writeLine("  <WindowTopY>0</WindowTopY>");
			writeLine("  <ProtectStructure>False</ProtectStructure>");
			writeLine("  <ProtectWindows>False</ProtectWindows>");
			writeLine(" </ExcelWorkbook>");
			//Define cell styles
			defineStyles(out);
			
			//Define variable names
			String sheetName=myPanel.name;
			sheetName=sheetName.replaceAll("-", ""); //Replace special chars
			numCols=2+(3*numDim);
			numVars=tree.variables.size();
			if(numVars>0){
				numCols+=4;
				int varCol=numCols-1;
				writeLine(" <Names>");
				for(int v=0; v<numVars; v++){
					writeLine("  <NamedRange ss:Name=\""+tree.variables.get(v).name+"\" ss:RefersTo=\""+sheetName+"!R"+(v+2)+"C"+varCol+"\"/>");
				}
				writeLine(" </Names>");
			}
			
			//Create model sheet - write all inputs
			int numNodes=tree.nodes.size();
			writeLine(" <Worksheet ss:Name=\""+sheetName+"\">");
			
			int numRows=Math.max(numNodes, numVars);
			writeLine("  <Table ss:ExpandedColumnCount=\""+numCols+"\" ss:ExpandedRowCount=\""+numRows+"\" x:FullColumns=\"1\" x:FullRows=\"1\" ss:DefaultRowHeight=\"14.4\">");
			//Headers
			writeLine("   <Row>");
			writeLine("    <Cell ss:StyleID=\"HeaderL\"><Data ss:Type=\"String\">Node</Data></Cell>");
			writeLine("    <Cell ss:StyleID=\"HeaderC\"><Data ss:Type=\"String\">Probability</Data></Cell>");
			//Cost
			for(int d=0; d<numDim; d++){writeLine("    <Cell ss:StyleID=\"HeaderC\"><Data ss:Type=\"String\">Cost-"+tree.dimNames[d]+" ("+tree.dimSymbols[d]+")</Data></Cell>");}
			//Payoff
			for(int d=0; d<numDim; d++){writeLine("    <Cell ss:StyleID=\"HeaderC\"><Data ss:Type=\"String\">Payoff-"+tree.dimNames[d]+" ("+tree.dimSymbols[d]+")</Data></Cell>");}
			//Expected Value
			for(int d=0; d<numDim-1; d++){writeLine("    <Cell ss:StyleID=\"HeaderC\"><Data ss:Type=\"String\">Expected-"+tree.dimNames[d]+" ("+tree.dimSymbols[d]+")</Data></Cell>");}
			writeLine("    <Cell ss:StyleID=\"HeaderR\"><Data ss:Type=\"String\">Expected-"+tree.dimNames[numDim-1]+" ("+tree.dimSymbols[numDim-1]+")</Data></Cell>");
			if(numVars>0){
				writeLine("    <Cell ss:Index=\""+(numCols-2)+"\" ss:StyleID=\"HeaderL\"><Data ss:Type=\"String\">Variable Name</Data></Cell>");
				writeLine("    <Cell ss:StyleID=\"HeaderC\"><Data ss:Type=\"String\">Value</Data></Cell>");
				writeLine("    <Cell ss:StyleID=\"HeaderR\"><Data ss:Type=\"String\">Expression</Data></Cell>");
			}
			writeLine("   </Row>");
			//Write all inputs
			for(int i=1; i<numNodes-1; i++){
				writeNode(tree.nodes.get(i),false,i);
			}
			writeNode(tree.nodes.get(numNodes-1),true,numNodes-1); //Write final node
			//Write any remaining variables
			for(int i=numNodes; i<numVars; i++){
				writeLine("   <Row>");
				TreeVariable curVar=tree.variables.get(i);
				String styleL="Left", styleC="Grey", styleR="Right";
				if(i==numVars-1){//Last var
					styleL="BottomLeft"; styleC="GreyB"; styleR="BottomRight";
				}
				writeLine("    <Cell ss:Index=\""+(numCols-2)+"\" ss:StyleID=\""+styleL+"\"><Data ss:Type=\"String\">"+curVar.name+"</Data></Cell>");
				writeLine("    <Cell ss:StyleID=\""+styleC+"\"><Data ss:Type=\"Number\">"+curVar.value+"</Data></Cell>");
				writeLine("    <Cell ss:StyleID=\""+styleR+"\"><Data ss:Type=\"String\">"+curVar.expression+"</Data></Cell>");
				writeLine("   </Row>");
			}
			writeLine("  </Table>");

			//Close workbook
			writeLine("  <WorksheetOptions xmlns=\"urn:schemas-microsoft-com:office:excel\">");
			writeLine("   <PageSetup>");
			writeLine("    <Header x:Margin=\"0.3\"/>");
			writeLine("    <Footer x:Margin=\"0.3\"/>");
			writeLine("    <PageMargins x:Bottom=\"0.75\" x:Left=\"0.7\" x:Right=\"0.7\" x:Top=\"0.75\"/>");
			writeLine("   </PageSetup>");
			writeLine("   <Print>");
			writeLine("    <ValidPrinterInfo/>");
			writeLine("    <HorizontalResolution>600</HorizontalResolution>");
			writeLine("    <VerticalResolution>600</VerticalResolution>");
			writeLine("   </Print>");
			writeLine("   <Selected/>");
			writeLine("   <Panes>");
			writeLine("    <Pane>");
			writeLine("     <Number>3</Number>");
			writeLine("     <ActiveRow>0</ActiveRow>");
			writeLine("     <ActiveCol>0</ActiveCol>");
			writeLine("    </Pane>");
			writeLine("   </Panes>");
			writeLine("   <ProtectObjects>False</ProtectObjects>");
			writeLine("   <ProtectScenarios>False</ProtectScenarios>");
			writeLine("  </WorksheetOptions>");
			writeLine(" </Worksheet>");
			writeLine("</Workbook>");
			out.close();

		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void writeLine(String line){
		try{
			out.write(line); out.newLine();
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}
	
	private void writeNode(TreeNode node,boolean last, int curIndex){
		try{
			String nodeStyle="Default";
			String style="Default";
			String styleL="Left";
			String styleR="Right";
			String emptyData="<Data ss:Type=\"String\" x:Ticked=\"1\">&#45;&#45;-</Data>";
			String evL="GreyL";
			String evC="Grey";
			String evR="GreyR";
			if(last==false){
				if(node.type==0){nodeStyle="RedL";}
				else if(node.type==1){nodeStyle="GreenL";}
				else if(node.type==2){nodeStyle="BlueL";}
			}
			else{
				style="Bottom";
				styleL="BottomLeft";
				styleR="BottomRight";
				evL="GreyLB";
				evC="GreyB";
				evR="GreyRB";
				if(node.type==0){nodeStyle="RedB";}
				else if(node.type==1){nodeStyle="GreenB";}
				else if(node.type==2){nodeStyle="BlueB";}
			}

			out.write("   <Row>"); out.newLine();
			//Node name
			out.write("    <Cell ss:StyleID=\""+nodeStyle+"\"><Data ss:Type=\"String\">"+node.name+"</Data></Cell>"); out.newLine();
			if(node.parentType==0){ //Parent is Decision node
				out.write("    <Cell ss:StyleID=\""+style+"\">"+emptyData+"</Cell>"); out.newLine();
			}
			else{
				if(node.prob.matches("C")){ //Complementary prob - enter formula
					int parentIndex=tree.getParentIndex(curIndex);
					TreeNode parent=tree.nodes.get(parentIndex);
					String formulaP="1.0";
					int numChildren=parent.childIndices.size();
					for(int i=0; i<numChildren; i++){
						int childIndex=parent.childIndices.get(i);
						if(childIndex!=curIndex){
							int row=childIndex-curIndex; //Get relative position of child row
							formulaP+="-R["+row+"]C";
						}
					}
					out.write("    <Cell ss:StyleID=\""+evL+"\" ss:Formula=\"="+formulaP+"\"><Data ss:Type=\"Number\">"+0+"</Data></Cell>"); out.newLine();
				}
				else{ //Write out expression
					out.write("    <Cell ss:StyleID=\""+evL+"\" ss:Formula=\"="+node.prob+"\"><Data ss:Type=\"Number\">"+0+"</Data></Cell>"); out.newLine();
				}
			}
			if(node.type!=2){ //Not terminal
				for(int d=0; d<numDim; d++){writeLine("    <Cell ss:StyleID=\""+style+"\" ss:Formula=\"="+node.cost[d]+"\"><Data ss:Type=\"Number\">0</Data></Cell>");}
				for(int d=0; d<numDim; d++){writeLine("    <Cell ss:StyleID=\""+style+"\">"+emptyData+"</Cell>");}
				//Calculate expected value
				int numChildren=node.childIndices.size();
				for(int d=0; d<numDim; d++){
					int pCol=5+d;
					String formula="RC[-"+(2*numDim)+"]+";
					for(int i=0; i<numChildren; i++){
						int row=node.childIndices.get(i)-curIndex; //Get relative position of child row
						formula+="(R["+row+"]C[-"+pCol+"]*R["+row+"]C)+";
					}
					//Trim last '+'
					formula=formula.substring(0, formula.length()-1);
					String curEVStyle=evC;
					if(d==0){curEVStyle=evL;}
					else if(d==numDim-1){curEVStyle=evR;}
					writeLine("    <Cell ss:StyleID=\""+curEVStyle+"\" ss:Formula=\"="+formula+"\"><Data ss:Type=\"Number\">"+0+"</Data></Cell>");
				}
			}
			else{ //Is terminal
				for(int d=0; d<numDim; d++){writeLine("    <Cell ss:StyleID=\""+style+"\">"+emptyData+"</Cell>");}
				for(int d=0; d<numDim; d++){writeLine("    <Cell ss:StyleID=\""+style+"\" ss:Formula=\"="+node.payoff[d]+"\"><Data ss:Type=\"Number\">0</Data></Cell>");}
				//EV
				for(int d=0; d<numDim; d++){
					String curEVStyle=evC;
					if(d==0){curEVStyle=evL;}
					else if(d==numDim-1){curEVStyle=evR;}
					writeLine("    <Cell ss:StyleID=\""+curEVStyle+"\" ss:Formula=\"=RC[-"+numDim+"]\"><Data ss:Type=\"Number\">0</Data></Cell>");
				}
			}
			
			//Write variable
			if(curIndex<=numVars){
				TreeVariable curVar=tree.variables.get(curIndex-1);
				styleL="Left"; style="Grey"; styleR="Right";
				if(curIndex==numVars){//Last var
					styleL="BottomLeft"; style="GreyB"; styleR="BottomRight";
				}
				writeLine("    <Cell ss:Index=\""+(numCols-2)+"\" ss:StyleID=\""+styleL+"\"><Data ss:Type=\"String\">"+curVar.name+"</Data></Cell>");
				writeLine("    <Cell ss:StyleID=\""+style+"\"><Data ss:Type=\"Number\">"+curVar.value+"</Data></Cell>");
				writeLine("    <Cell ss:StyleID=\""+styleR+"\"><Data ss:Type=\"String\">"+curVar.expression+"</Data></Cell>");
			}
			
			writeLine("   </Row>");
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}

	private void defineStyles(BufferedWriter out){
		try{

			//Define cell styles
			writeLine(" <Styles>");
			//Normal
			writeLine("  <Style ss:ID=\"Default\">");
			writeLine("   <Alignment ss:Vertical=\"Bottom\"/>");
			writeLine("   <Borders/>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\" ss:Color=\"#000000\"/>"); 
			writeLine("   <Interior/>"); 
			writeLine("   <NumberFormat/>"); 
			writeLine("   <Protection/>"); 
			writeLine("  </Style>"); 
			//Header-left
			writeLine("  <Style ss:ID=\"HeaderL\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("    <Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\" ss:Color=\"#000000\" ss:Bold=\"1\"/>"); 
			writeLine("  </Style>"); 
			//Header-center
			writeLine("  <Style ss:ID=\"HeaderC\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("    <Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\" ss:Color=\"#000000\" ss:Bold=\"1\"/>"); 
			writeLine("  </Style>"); 
			//Header-right
			writeLine("  <Style ss:ID=\"HeaderR\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("    <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("    <Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\" ss:Color=\"#000000\" ss:Bold=\"1\"/>"); 
			writeLine("  </Style>"); 
			//Red fill-left
			writeLine("  <Style ss:ID=\"RedL\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("	  <Interior ss:Color=\"#FF0000\" ss:Pattern=\"Solid\"/>"); 
			writeLine("  </Style>"); 
			//Green fill-left
			writeLine("  <Style ss:ID=\"GreenL\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("	  <Interior ss:Color=\"#00FF00\" ss:Pattern=\"Solid\"/>"); 
			writeLine("  </Style>"); 
			//Blue fill-left
			writeLine("  <Style ss:ID=\"BlueL\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("	  <Interior ss:Color=\"#0000FF\" ss:Pattern=\"Solid\"/>"); 
			writeLine("  </Style>"); 
			//Red fill-bottom
			writeLine("  <Style ss:ID=\"RedB\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("	  <Interior ss:Color=\"#FF0000\" ss:Pattern=\"Solid\"/>"); 
			writeLine("  </Style>"); 
			//Green fill-bottom
			writeLine("  <Style ss:ID=\"GreenB\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("	  <Interior ss:Color=\"#00FF00\" ss:Pattern=\"Solid\"/>"); 
			writeLine("  </Style>"); 
			//Blue fill-bottom
			writeLine("  <Style ss:ID=\"BlueB\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("	  <Interior ss:Color=\"#0000FF\" ss:Pattern=\"Solid\"/>"); 
			writeLine("  </Style>"); 
			//No fill-left
			writeLine("  <Style ss:ID=\"Left\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("  </Style>"); 
			//No fill-right
			writeLine("  <Style ss:ID=\"Right\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("  </Style>"); 
			//No fill-bottom
			writeLine("  <Style ss:ID=\"Bottom\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("  </Style>"); 
			//No fill-bottom+left
			writeLine("  <Style ss:ID=\"BottomLeft\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("  </Style>"); 
			//No fill-bottom+right
			writeLine("  <Style ss:ID=\"BottomRight\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("    <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("  </Style>"); 
			//Grey fill-left
			writeLine("  <Style ss:ID=\"GreyL\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("	  <Interior ss:Color=\"#D9D9D9\" ss:Pattern=\"Solid\"/>"); 
			writeLine("  </Style>"); 
			//Grey fill-right
			writeLine("  <Style ss:ID=\"GreyR\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("	  <Interior ss:Color=\"#D9D9D9\" ss:Pattern=\"Solid\"/>"); 
			writeLine("  </Style>"); 
			//Grey fill-left bottom
			writeLine("  <Style ss:ID=\"GreyLB\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("    <Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("	  <Interior ss:Color=\"#D9D9D9\" ss:Pattern=\"Solid\"/>"); 
			writeLine("  </Style>"); 
			//Grey fill-right bottom
			writeLine("  <Style ss:ID=\"GreyRB\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("    <Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("	  <Interior ss:Color=\"#D9D9D9\" ss:Pattern=\"Solid\"/>"); 
			writeLine("  </Style>"); 
			//Grey fill
			writeLine("  <Style ss:ID=\"Grey\">"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("	  <Interior ss:Color=\"#D9D9D9\" ss:Pattern=\"Solid\"/>"); 
			writeLine("  </Style>"); 
			//Grey fill-bottom
			writeLine("  <Style ss:ID=\"GreyB\">"); 
			writeLine("   <Borders>"); 
			writeLine("    <Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/>"); 
			writeLine("   </Borders>"); 
			writeLine("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\"/>"); 
			writeLine("	  <Interior ss:Color=\"#D9D9D9\" ss:Pattern=\"Solid\"/>"); 
			writeLine("  </Style>"); 

			writeLine(" </Styles>"); 

		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}
}
