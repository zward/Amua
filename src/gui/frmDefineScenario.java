/**
 * Amua - An open source modeling framework.
 * Copyright (C) 2017-2019 Zachary J. Ward
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

package gui;

import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import base.AmuaModel;
import main.Parameter;
import main.Scenario;
import main.StyledTextPane;
import main.Variable;
import math.Interpreter;
import math.Numeric;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

/**
 *
 */
public class frmDefineScenario {

	/**
	 * JFrame for form
	 */
	public JDialog frmDefineScenario;
	AmuaModel myModel;
	private JTextField textName;
	private JTextPane paneValue;
	JTextArea textNotes;
	Scenario scenario;
	int scenarioNum;
	StyledTextPane paneExpression;
	frmScenarios schedule;
	//uncertainty
	private JTextField textIterations;
	JCheckBox chckbxSeed1, chckbxSeed2;
	JCheckBox chckbxSampleParameters;
	private JTextField textSeed1,textSeed2;
	private JTextField textCohortSize;

	public frmDefineScenario(AmuaModel myModel, int runNum, frmScenarios schedule){
		this.myModel=myModel;
		this.scenarioNum=runNum;
		this.schedule=schedule;
		initialize();
		if(runNum==-1){scenario=new Scenario(myModel);}
		else{scenario=schedule.scenarios.get(scenarioNum);}
		getScenario();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmDefineScenario = new JDialog();
			frmDefineScenario.setModalityType(ModalityType.APPLICATION_MODAL);
			frmDefineScenario.setTitle("Amua - Define Scenario");
			frmDefineScenario.setResizable(false);
			frmDefineScenario.setBounds(100, 100, 679, 434);
			frmDefineScenario.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmDefineScenario.getContentPane().setLayout(null);

			JButton btnSave = new JButton("Save");
			btnSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean proceed=true;
					//Ensure name is valid and unique
					String testName=textName.getText();
					String testNotes=textNotes.getText();
					int testIterations=-1, testCohortSize=-1, testSeed1=-1, testSeed2=-1;
					if(testName.length()==0){
						JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a name!"); 
						proceed=false;
					}
					else{
						//Ensure name is unique
						boolean unique=true;
						int i=0;
						int numScenarios=schedule.scenarios.size();
						while(unique==true && i<numScenarios){
							if(i!=scenarioNum && schedule.scenarios.get(i).name.equals(testName)){unique=false;}
							i++;
						}
						if(unique==false){
							JOptionPane.showMessageDialog(frmDefineScenario, testName+" is already defined!");
							proceed=false;
						}

						//Check iterations
						try{
							testIterations=Integer.parseInt(textIterations.getText());
						} catch(Exception e1){
							JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid number of iterations!");
							proceed=false;
						}
						if(proceed && testIterations<=0){
							JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid number of iterations!");
							proceed=false;
						}

						//Check cohort size
						try{
							testCohortSize=Integer.parseInt(textCohortSize.getText());
						}catch(Exception e1){
							if(myModel.simType==0){JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid cohort size!");}
							else if(myModel.simType==1){JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid # of simulations!");}
							proceed=false;
						}
						if(proceed && testCohortSize<=0){
							if(myModel.simType==0){JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid cohort size!");}
							else if(myModel.simType==1){JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid # of simulations!");}
							proceed=false;
						}


						//check seeds
						if(chckbxSeed1.isSelected()){
							try{
								testSeed1=Integer.parseInt(textSeed1.getText());
							} catch(Exception e1){
								JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid seed!");
								proceed=false;
							}
						}
						if(chckbxSeed2.isSelected()){
							try{
								testSeed2=Integer.parseInt(textSeed2.getText());
							} catch(Exception e1){
								JOptionPane.showMessageDialog(frmDefineScenario, "Please enter a valid seed!");
								proceed=false;
							}
						}

					}

					if(proceed==true){
						boolean sampleParams=chckbxSampleParameters.isSelected();

						//Evaluate updates
						String testExp=paneExpression.getText();
						proceed=evaluateUpdates();

						if(proceed==true){ //save
							scenario.name=testName;
							//uncertainty
							scenario.numIterations=testIterations;
							scenario.cohortSize=testCohortSize;
							scenario.crn1=chckbxSeed1.isSelected();
							scenario.seed1=testSeed1;
							scenario.sampleParams=sampleParams;
							scenario.crn2=chckbxSeed2.isSelected();
							scenario.seed2=testSeed2;

							scenario.objectUpdates=testExp;
							scenario.notes=testNotes;

							if(scenarioNum==-1){schedule.scenarios.add(scenario);}
							schedule.updateScenarios();

							frmDefineScenario.dispose();
						}
					}
				}
			});
			btnSave.setBounds(232, 365, 90, 28);
			frmDefineScenario.getContentPane().add(btnSave);

			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmDefineScenario.dispose();
				}
			});
			btnCancel.setBounds(331, 365, 90, 28);
			frmDefineScenario.getContentPane().add(btnCancel);

			JLabel lblName = new JLabel("Scenario Name:");
			lblName.setBounds(6, 13, 107, 16);
			frmDefineScenario.getContentPane().add(lblName);

			textName = new JTextField();
			textName.setBounds(108, 7, 296, 28);
			frmDefineScenario.getContentPane().add(textName);
			textName.setColumns(10);

			JLabel lblNotes = new JLabel("Notes:");
			lblNotes.setBounds(416, 73, 55, 16);
			frmDefineScenario.getContentPane().add(lblNotes);

			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(457, 7, 207, 138);
			frmDefineScenario.getContentPane().add(scrollPane_1);

			textNotes = new JTextArea();
			scrollPane_1.setViewportView(textNotes);

			JPanel panel = new JPanel();
			panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panel.setBounds(6, 41, 398, 104);
			frmDefineScenario.getContentPane().add(panel);
			panel.setLayout(null);

			JLabel lblModelUncertainty = new JLabel("Model Uncertainty");
			lblModelUncertainty.setFont(new Font("SansSerif", Font.BOLD, 12));
			lblModelUncertainty.setBounds(6, 6, 107, 16);
			panel.add(lblModelUncertainty);

			JLabel lblIterations = new JLabel("# iterations:");
			lblIterations.setBounds(128, 6, 64, 16);
			panel.add(lblIterations);

			textIterations = new JTextField();
			textIterations.setBounds(196, 1, 55, 28);
			panel.add(textIterations);
			textIterations.setText("1");
			textIterations.setColumns(10);

			chckbxSampleParameters = new JCheckBox("Sample Parameters");
			chckbxSampleParameters.setBounds(192, 56, 143, 18);
			panel.add(chckbxSampleParameters);

			JLabel lblFirstorder = new JLabel("First-order");
			lblFirstorder.setFont(new Font("SansSerif", Font.ITALIC, 12));
			lblFirstorder.setBounds(6, 34, 90, 16);
			panel.add(lblFirstorder);

			JLabel lblSecondorder = new JLabel("Second-order");
			lblSecondorder.setFont(new Font("SansSerif", Font.ITALIC, 12));
			lblSecondorder.setBounds(192, 34, 90, 16);
			panel.add(lblSecondorder);

			chckbxSeed2 = new JCheckBox("Seed Parameter RNG");
			chckbxSeed2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if(chckbxSeed2.isSelected()){textSeed2.setEnabled(true);}
					else{textSeed2.setEnabled(false);}
				}
			});
			chckbxSeed2.setBounds(192, 77, 145, 18);
			panel.add(chckbxSeed2);

			textSeed2 = new JTextField();
			textSeed2.setEnabled(false);
			textSeed2.setBounds(338, 72, 54, 28);
			panel.add(textSeed2);
			textSeed2.setColumns(10);

			chckbxSeed1 = new JCheckBox("Seed RNG");
			chckbxSeed1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if(chckbxSeed1.isSelected()){textSeed1.setEnabled(true);}
					else{textSeed1.setEnabled(false);}
				}
			});
			chckbxSeed1.setBounds(16, 77, 90, 18);
			panel.add(chckbxSeed1);

			textSeed1 = new JTextField();
			textSeed1.setEnabled(false);
			textSeed1.setColumns(10);
			textSeed1.setBounds(102, 72, 54, 28);
			panel.add(textSeed1);

			JLabel lblCohortSize = new JLabel("Cohort size:");
			if(myModel.simType==1){lblCohortSize.setText("# simulations:");}
			lblCohortSize.setBounds(6, 52, 80, 16);
			panel.add(lblCohortSize);

			textCohortSize = new JTextField();
			textCohortSize.setBounds(85, 46, 74, 28);
			panel.add(textCohortSize);
			textCohortSize.setColumns(10);

			JPanel panel_1 = new JPanel();
			panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panel_1.setBounds(6, 151, 658, 208);
			frmDefineScenario.getContentPane().add(panel_1);
			panel_1.setLayout(null);

			JLabel lblExpression = new JLabel("Object Updates");
			lblExpression.setFont(new Font("SansSerif", Font.BOLD, 12));
			lblExpression.setBounds(6, 6, 101, 16);
			panel_1.add(lblExpression);

			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(6, 27, 637, 87);
			panel_1.add(scrollPane);

			paneExpression=new StyledTextPane(myModel);
			paneExpression.setFont(new Font("Consolas", Font.PLAIN,15));
			scrollPane.setViewportView(paneExpression);

			JButton btnEvaluate = new JButton("Evaluate");
			btnEvaluate.setBounds(6, 113, 90, 28);
			panel_1.add(btnEvaluate);

			JLabel lblValue = new JLabel("Expected Values:");
			lblValue.setBounds(6, 153, 101, 16);
			panel_1.add(lblValue);

			JScrollPane scrollPaneValue = new JScrollPane();
			scrollPaneValue.setBounds(110, 118, 533, 87);
			panel_1.add(scrollPaneValue);

			paneValue = new JTextPane();
			scrollPaneValue.setViewportView(paneValue);
			paneValue.setEditable(false);
			btnEvaluate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					evaluateUpdates();
				}
			});

			paneExpression.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==KeyEvent.VK_ENTER){
						evaluateUpdates();
					}
				}
			});

		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}

	private boolean evaluateUpdates(){
		boolean valid=true;
		String testExp=paneExpression.getText();
		if(testExp.isEmpty()){ //no updates
			paneValue.setText("[No updates]");
		}
		else{
			Scenario test=new Scenario();
			test.objectUpdates=testExp;

			try{
				test.parseUpdates(myModel);
			} catch(Exception e){
				valid=false;
				paneValue.setText(e.getMessage());
			}

			if(valid==true){
				int numUpdates=test.objectNames.length;
				paneValue.setText("");
				String ev="";
				for(int i=0; i<numUpdates; i++){
					ev+=test.objectNames[i]+": "+test.testVals[i].toString()+"\n";
				}
				paneValue.setText(ev);
			}
		}
		return(valid);

	}


	private void getScenario(){
		textName.setText(scenario.name);
		//uncertainty
		textIterations.setText(scenario.numIterations+"");
		textCohortSize.setText(scenario.cohortSize+"");
		chckbxSeed1.setSelected(scenario.crn1);
		if(chckbxSeed1.isSelected()){textSeed1.setText(scenario.seed1+"");}
		chckbxSampleParameters.setSelected(scenario.sampleParams);
		chckbxSeed2.setSelected(scenario.crn2);
		if(chckbxSeed2.isSelected()){textSeed2.setText(scenario.seed2+"");}
		//object updates
		paneExpression.setText(scenario.objectUpdates);
		paneExpression.restyle();
		textNotes.setText(scenario.notes);
	}
}
