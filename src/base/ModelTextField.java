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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

import main.VariableUpdate;
import math.Interpreter;

public class ModelTextField extends JTextField{
	public ModelTextField field;
	protected ModelNode node;
	protected ModelPanel panel;
	protected AmuaModel myModel;
	/**
	 * Decision tree: 0=Prob; 1=Cost, 2=Payoff
	 * Markov model: 0=Prob; 1=Cost/Reward; 2=Termination; 3=Rewards; 4=Var Updates; 5=Name; 6=Var Updates T0
	 */
	protected int fieldType;
	protected String checkProb, checkCosts[], checkVarUpdates;
	
	//Constructor
	public ModelTextField(ModelPanel panel1, ModelNode node, int type){
		field=this;
		this.panel=panel1;
		this.node=node;
		this.myModel=panel.myModel;
		fieldType=type;
		//this.setFont(new Font("Consolas", Font.PLAIN, 14));
		
		addKeyListener(new KeyAdapter(){ //Save text on Enter
			@Override
			public void keyPressed(KeyEvent e){
				int key=e.getKeyCode();
				if(key==KeyEvent.VK_ENTER){
					panel.requestFocusInWindow();
				}
			}
		});
	}

	public void validateEntry(){
		//overwritten by subclasses
	}
	
	public void updateHistory(){
		//overwritten by subclasses
	}
	
	protected void validateProb(){
		checkProb=this.getText();
		Font curFont=this.getFont();
		this.setFont(curFont.deriveFont(curFont.PLAIN));
		this.setForeground(Color.BLACK);
		boolean valid=true;
		if(checkProb.length()==0){ //No entry, default to 0
			checkProb="0";
			this.setText("0");
		}
		else if(checkProb.matches("C") || checkProb.matches("c")){ //Complementary
			this.setText("C");
			this.setFont(curFont.deriveFont(curFont.ITALIC));
		}
		else{ //Validate expression
			if(node.validateText(checkProb)==false){valid=false;}
			else{
				try{
					double testProb=Interpreter.evaluate(checkProb, myModel,false).getDouble();
					if(testProb<0 || testProb>1){valid=false;}
				}catch(Exception e){
					valid=false;
				}
			}
		}
		if(valid==false){this.setForeground(Color.RED);}
	}	
	
	protected void validateCost(){
		boolean valid=true;
		this.setForeground(Color.BLACK);
		try{
			checkCosts=node.parseDimensions(this.getText());
			int i=0;
			while(valid==true && i<node.numDimensions){
				if(node.validateText(checkCosts[i])==false){valid=false;}
				i++;
			}
		}catch(Exception ex){valid=false;}
		if(valid==false){this.setForeground(Color.RED);}
	}
	
	protected void validateVarUpdates(){
		checkVarUpdates=this.getText();
		boolean valid=true;
		this.setForeground(Color.BLACK);
		try{
			String strExpr[]=checkVarUpdates.split(";");
			int i=0;
			while(valid==true && i<strExpr.length){
				VariableUpdate test=new VariableUpdate(strExpr[i],myModel);
				i++;
			}
		}catch(Exception ex){valid=false;}
		if(valid==false){this.setForeground(Color.RED);}
	}
}