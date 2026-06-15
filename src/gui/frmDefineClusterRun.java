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
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import base.AmuaModel;
import cluster.ClusterInputs;
import filters.XMLFilter;

import javax.swing.JCheckBox;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.awt.Toolkit;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

/**
 *
 */
public class frmDefineClusterRun {

	/**
	 * JFrame for form
	 */
	public JDialog frmDefineClusterRun;
	AmuaModel myModel;
	
	JList<String> listRuns;
	
	
	JCheckBox chckbxSeedIteration;
	
	//PSA
	JCheckBox chckbxPSASeed, chckbxSampleParameterSets;
	private JTextField textPSASeed;
	

	public frmDefineClusterRun(AmuaModel myModel){
		this.myModel=myModel;
		//myModel.getStrategies();
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmDefineClusterRun = new JDialog();
			frmDefineClusterRun.setIconImage(Toolkit.getDefaultToolkit().getImage(frmDefineClusterRun.class.getResource("/images/cluster_128.png")));
			frmDefineClusterRun.setModalityType(ModalityType.APPLICATION_MODAL);
			frmDefineClusterRun.setTitle("Amua - "+myModel.language.base.getString("title.define_cluster_run")); //Define Cluster Run
			frmDefineClusterRun.setFont(myModel.language.font);
			frmDefineClusterRun.setResizable(false);
			frmDefineClusterRun.setBounds(100, 100, 679, 371);
			frmDefineClusterRun.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmDefineClusterRun.getContentPane().setLayout(null);

			JButton btnSave = new JButton(myModel.language.base.getString("menu.save")); //Save
			btnSave.setFont(myModel.language.font);
			btnSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ClusterInputs inputs=new ClusterInputs();
					
					String operation=(String) listRuns.getSelectedValue();
					inputs.operation=operation;
					
					inputs.seedIterationRNG=chckbxSeedIteration.isSelected();
					
					if(operation.contains("PSA")) {
						inputs.seedParamRNG=chckbxPSASeed.isSelected();
						if(inputs.seedParamRNG) {
							inputs.paramSeed=Integer.parseInt(textPSASeed.getText());
						}
						inputs.sampleParamSets=chckbxSampleParameterSets.isSelected();
					}
					
					//save to file
					JFileChooser fc= new JFileChooser(myModel.filepath);
					fc.setDialogTitle(myModel.language.base.getString("title.save_cluster_run_inputs")); //Save Cluster Run Inputs
					fc.setApproveButtonText(myModel.language.base.getString("menu.save"));
					fc.resetChoosableFileFilters();
					fc.addChoosableFileFilter(new XMLFilter(myModel.language));
					fc.setAcceptAllFileFilterUsed(false);
					myModel.language.setFontRecursively(fc); //set font
					
					int returnVal = fc.showSaveDialog(frmDefineClusterRun);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						String outpath=file.getAbsolutePath();
						outpath=outpath.replace(".xml", "");
						outpath=outpath+".xml";
						
						try {
							JAXBContext context = JAXBContext.newInstance(ClusterInputs.class);
							Marshaller m = context.createMarshaller();
							m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
							m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //should be default, but make UTF explicit

							FileOutputStream fileStream = new FileOutputStream(new File(outpath));
							OutputStreamWriter writer = new OutputStreamWriter(fileStream, "UTF-8"); //ensure writer is UTF-8
							m.marshal(inputs, writer);
							writer.close();

							frmDefineClusterRun.dispose();
							
						}catch(Exception e1){
							e1.printStackTrace();
							myModel.errorLog.recordError(e1);
						}

					}
									
				}
			});
			btnSave.setBounds(195, 289, 150, 28);
			frmDefineClusterRun.getContentPane().add(btnSave);

			JButton btnCancel = new JButton(myModel.language.base.getString("button.cancel")); //Cancel
			btnCancel.setFont(myModel.language.font);
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmDefineClusterRun.dispose();
				}
			});
			btnCancel.setBounds(376, 289, 150, 28);
			frmDefineClusterRun.getContentPane().add(btnCancel);
											
			JLabel lblRun = new JLabel(myModel.language.analysis.getString("gen.run_type")+":"); //Run Type
			lblRun.setFont(myModel.language.font);
			lblRun.setBounds(6, 6, 232, 16);
			frmDefineClusterRun.getContentPane().add(lblRun);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(6, 25, 232, 259);
			frmDefineClusterRun.getContentPane().add(scrollPane);
			
			listRuns = new JList<String>();
			listRuns.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					
					//update input fields...
					
				}
			});
			listRuns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listRuns.setModel(new AbstractListModel() {
				String[] values = new String[] {"PSA", "Scenarios"};
				public int getSize() {
					return values.length;
				}
				public Object getElementAt(int index) {
					return values[index];
				}
			});
			listRuns.setSelectedIndex(0);
			scrollPane.setViewportView(listRuns);
			
			chckbxSeedIteration = new JCheckBox(myModel.language.analysis.getString("sim.iteration_as_seed")); //Use iteration as RNG seed
			chckbxSeedIteration.setFont(myModel.language.font);
			chckbxSeedIteration.setSelected(true);
			chckbxSeedIteration.setBounds(275, 29, 338, 18);
			frmDefineClusterRun.getContentPane().add(chckbxSeedIteration);
			
			JPanel panel = new JPanel();
			panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panel.setBounds(275, 64, 325, 85);
			frmDefineClusterRun.getContentPane().add(panel);
			panel.setLayout(null);
			
			
			JLabel lblPsa = new JLabel(myModel.language.analysis.getString("gen.psa")+":"); //PSA
			lblPsa.setBounds(6, 6, 301, 16);
			panel.add(lblPsa);
			lblPsa.setFont(myModel.language.font.deriveFont(Font.BOLD, 12f));
			
			chckbxPSASeed = new JCheckBox(myModel.language.analysis.getString("sim.seed")); //Seed
			chckbxPSASeed.setFont(myModel.language.font);
			chckbxPSASeed.setBounds(6, 32, 87, 18);
			panel.add(chckbxPSASeed);
			
			textPSASeed = new JTextField();
			textPSASeed.setBounds(96, 27, 54, 28);
			panel.add(textPSASeed);
			textPSASeed.setEnabled(false);
			textPSASeed.setColumns(10);
			
			chckbxSampleParameterSets = new JCheckBox(myModel.language.analysis.getString("sim.sample_parameter_sets")); //Sample parameter sets
			chckbxSampleParameterSets.setFont(myModel.language.font);
			chckbxSampleParameterSets.setBounds(6, 62, 301, 18);
			panel.add(chckbxSampleParameterSets);
			chckbxSampleParameterSets.setEnabled(false);
			chckbxPSASeed.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(chckbxPSASeed.isSelected()) {
						textPSASeed.setEnabled(true);
					}
					else {
						textPSASeed.setEnabled(false);
					}
				}
			});
			
			if(myModel.parameterSets!=null) {
				chckbxSampleParameterSets.setEnabled(true);
			}
			
	
		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}
}
