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

package base;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import math.Interpreter;

@XmlRootElement(name="node")
public class ModelNode{
	
	//Base data
	/**
	 * Type of node.
	 * Decision tree: 0=Decision, 1=Chance, 2=Terminal
	 * Markov model:  0=Decision, 1=Chain, 2=State, 3=Chance, 4=State Transition
	 */
	@XmlElement public int type;
	@XmlElement public String name;
	@XmlElement public int xPos,yPos;
	@XmlElement public int width,height;
	@XmlElement public int parentX,parentY;
	@XmlElement public int parentType;
	@XmlElement public ArrayList<Integer> childIndices;
	@XmlElement public int level=-1;
	@XmlElement	public boolean hasCost=false;
	@XmlElement	public boolean hasVarUpdates=false;
	@XmlElement public String varUpdates;
	@XmlElement public String notes;
	@XmlElement public boolean visible=true;
	@XmlElement public boolean collapsed=false;
	
	@XmlTransient public int numDimensions=1;
	@XmlTransient public AmuaModel myModel;
	
	//Visual attributes
	@XmlTransient public boolean selected;
	//@XmlTransient public boolean visible=true;
	//@XmlTransient public boolean collapsed=false;
	@XmlTransient public JLabel lblCollapsed;
	@XmlTransient public int curScale=100;
	@XmlTransient public Color textHighlights[];
	@XmlTransient public Border defaultBorder=BorderFactory.createMatteBorder(1,3,1,1, Color.BLUE);

	@XmlTransient public String nameExport; //Normalized name for export to another language
	@XmlTransient public JTextField textName;
		
	//Constructor
	public ModelNode(){
		
	}
		
	/*public void move(int xShift, int yShift, ArrayList<MarkovNode> nodes){
		//Current node
		xPos+=xShift;
		yPos+=yShift;
		for(int i=0; i<childIndices.size(); i++){
			int index=childIndices.get(i);
			MarkovNode child=nodes.get(index);
			child.parentX+=xShift;
			child.parentY+=yShift;
			child.move(xShift, yShift, nodes);
		}
	}*/
	
	public void displayCollapsed(){
		lblCollapsed=new JLabel("+");
		lblCollapsed.setFont(new Font("SansSerif", Font.PLAIN, scale(20)));
		lblCollapsed.setForeground(Color.DARK_GRAY);
	}
	
	protected void showComponent(boolean show, JComponent curObject){
		if(curObject!=null){curObject.setVisible(show);}
	}

	public void setPlainFont(JTextField curField){
		Font curFont=curField.getFont();
		curField.setFont(curFont.deriveFont(curFont.PLAIN));
	}

	public void setItalicFont(JTextField curField){
		Font curFont=curField.getFont();
		curField.setFont(curFont.deriveFont(curFont.ITALIC));
	}
	
	public int scale(int orig){
		double scale=curScale/100.0;
		int scaled=(int)(orig*scale);
		return(scaled);
	}

	protected float scaleF(float orig){
		double scale=curScale/100.0;
		float scaled=(float) (orig*scale);
		return(scaled);
	}

	protected void scaleFont(JComponent curComp){
		Font curFont=curComp.getFont();
		curComp.setFont(curFont.deriveFont(scaleF(curFont.getSize())));
	}
	
	public String[] parseDimensions(String text){
		String dims[]=new String[numDimensions];
		for(int i=0; i<numDimensions-1; i++){
			String curSymbol="("+myModel.dimInfo.dimSymbols[i]+")";
			int index=text.indexOf(curSymbol);
			String curText=text.substring(index+curSymbol.length());
			index=curText.indexOf(";");
			curText=curText.substring(0, index);
			curText=curText.replaceAll(" ",""); //Remove any spaces
			dims[i]=curText;
		}
		//Get last dimension
		String curSymbol="("+myModel.dimInfo.dimSymbols[numDimensions-1]+")";
		int index=text.indexOf(curSymbol);
		String curText=text.substring(index+curSymbol.length());
		curText=curText.replaceAll(" ",""); //Remove any spaces
		curText=curText.replaceAll(";",""); //Remove ; if present
		dims[numDimensions-1]=curText;

		return(dims);
	}
	
	public boolean validateText(String text){
		boolean valid=true;
		try{
			//double test=myModel.varHelper.evaluateExpression(text,false);
			double test=Interpreter.evaluate(text, myModel,false).getDouble();
			if(Double.isNaN(test)){valid=false;}
		}catch(Exception e){
			valid=false;
		}
		return(valid);
	}
	
	/*public double evaluateExpression(String text){
		//return(myModel.varHelper.evaluateExpression(text,false));
		return(Calculator.evaluate(text, myModel).getDouble());
	}*/

	class FocusGrabber implements Runnable {
		private JComponent component;

		public FocusGrabber(JComponent component) {
			this.component = component;
		}
		public void run() {
			component.grabFocus();
		}
	} 

}