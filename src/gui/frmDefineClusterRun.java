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
			frmDefineClusterRun.setTitle("Amua - Define Cluster Run");
			frmDefineClusterRun.setResizable(false);
			frmDefineClusterRun.setBounds(100, 100, 679, 371);
			frmDefineClusterRun.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmDefineClusterRun.getContentPane().setLayout(null);

			JButton btnSave = new JButton("Save");
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
					fc.setDialogTitle("Save Cluster Run Inputs");
					fc.setApproveButtonText("Save");
					fc.resetChoosableFileFilters();
					fc.addChoosableFileFilter(new XMLFilter());
					fc.setAcceptAllFileFilterUsed(false);
					
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
			btnSave.setBounds(255, 289, 90, 28);
			frmDefineClusterRun.getContentPane().add(btnSave);

			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmDefineClusterRun.dispose();
				}
			});
			btnCancel.setBounds(370, 289, 90, 28);
			frmDefineClusterRun.getContentPane().add(btnCancel);
			
			if(myModel.parameterSets!=null) {
				chckbxSampleParameterSets.setEnabled(true);
			}
								
			JLabel lblRun = new JLabel("Run Type:");
			lblRun.setBounds(6, 6, 74, 16);
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
			
			chckbxSeedIteration = new JCheckBox("Use iteration as RNG seed");
			chckbxSeedIteration.setSelected(true);
			chckbxSeedIteration.setBounds(275, 29, 185, 18);
			frmDefineClusterRun.getContentPane().add(chckbxSeedIteration);
			
			JPanel panel = new JPanel();
			panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			panel.setBounds(275, 64, 203, 85);
			frmDefineClusterRun.getContentPane().add(panel);
			panel.setLayout(null);
			
			
			JLabel lblPsa = new JLabel("PSA:");
			lblPsa.setBounds(6, 6, 54, 16);
			panel.add(lblPsa);
			lblPsa.setFont(new Font("SansSerif", Font.BOLD, 12));
			
			chckbxPSASeed = new JCheckBox("Seed");
			chckbxPSASeed.setBounds(6, 32, 68, 18);
			panel.add(chckbxPSASeed);
			
			textPSASeed = new JTextField();
			textPSASeed.setBounds(72, 27, 54, 28);
			panel.add(textPSASeed);
			textPSASeed.setEnabled(false);
			textPSASeed.setColumns(10);
			
			chckbxSampleParameterSets = new JCheckBox("Sample parameter sets");
			chckbxSampleParameterSets.setBounds(6, 62, 159, 18);
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
			
		
			
	
		} catch (Exception ex){
			ex.printStackTrace();
			myModel.errorLog.recordError(ex);
		}
	}
}
