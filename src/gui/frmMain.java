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

package gui;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import base.AmuaModel;
import filters.AmuaModelFilter;
import filters.CSVFilter;
import filters.GIFFilter;
import filters.JPEGFilter;
import filters.PNGFilter;
import main.Console;
import main.Constraint;
import main.ErrorLog;
import main.Parameter;
import main.ParameterSet;
import main.RecentFiles;
import main.StyledTextPane;
import main.Variable;
import markov.PanelMarkov;
import tree.PanelTree;

public class frmMain {

	static frmMain main;
	public JFrame frmMain;
	int curModelType=0;
	int prevModelType=0;
	ArrayList<Integer> modelTypes;
	AmuaModel curModel;
	ArrayList<AmuaModel> modelList;
	public RecentFiles recentFiles;

	JToolBar toolBar;
	JTabbedPane tabbedPaneCanvas;
	JScrollPane scrollPaneFx;
	public JTabbedPane tabbedPaneBottom, tabbedPaneRight;
	JScrollPane scrollPaneNotes;
	JScrollPane scrollPaneProperties;
	JPanel panelParamSets;
	public JCheckBox chckbxUseParamSets;
	Console console;
	//JTextArea console;
	JFileChooser fc=new JFileChooser();
	public String version="0.2.1";
	public main.Clipboard clipboard; //Clipboard

	//Menu items to enable once a model is opened
	JMenuItem mntmSave, mntmSaveAs, mntmDocument, mntmExport, mntmProperties;
	JMenu mnEdit, mnRun;
	JButton btnOCD, btnClearAnnotations, btnZoomOut, btnZoomIn, btnSnapshot;
	
	//Main menu items to be accessible to panel
	public JButton btnDecisionNode;
	public JButton btnMarkovChain;
	public JButton btnMarkovState;
	public JButton btnChanceNode;
	public JButton btnTerminalNode;
	public JButton btnStateTransition;
	public JButton btnChangeNodeType;
	public JButton btnShowCost;
	public JButton btnUpdateVariable;
	public JButton btnEqualY;
	public JButton btnCollapse;
	public JButton btnAlignLeft;
	public JButton btnAlignRight;
	public JButton btnCheckModel;
	public JButton btnRunModel;
	public JMenuItem mntmUndo;
	public JMenuItem mntmRedo;
	public JMenuItem mntmCut;
	public JMenuItem mntmCopy;
	public JMenuItem mntmPaste;
	public JMenuItem mntmRunModel;
	public JMenuItem mntmCalibrateModel;
	JButton btnAddVariable;
	JSlider sliderZoom;
	
	public JButton btnFx; //build expression button
	public boolean updateCurFx=false; //if Update Operators are allowed
	StyledTextPane curFxPane;
	
	public DefaultTableModel modelParameters, modelVariables, modelTables, modelConstraints, modelParamSets;
	private JTable tableParameters, tableVariables, tableTables, tableConstraints, tableParamSets;

	ErrorLog errorLog;

	/**
	 * Create the application.
	 */
	public frmMain() {
		main=this;
		initialize();
		checkAnyModels();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmMain = new JFrame();
		frmMain.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		frmMain.setTitle("Amua");
		frmMain.setIconImage(Toolkit.getDefaultToolkit().getImage(frmMain.class.getResource("/images/logo_48.png")));
		
		frmMain.setBounds(100, 100, 800, 600);
		frmMain.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frmMain.setExtendedState(JFrame.MAXIMIZED_BOTH);

		errorLog=new ErrorLog(version);
		clipboard=new main.Clipboard();
		console=new Console(version);

		//Initialize model lists
		modelTypes=new ArrayList<Integer>();
		modelList=new ArrayList<AmuaModel>();
		recentFiles=new RecentFiles();

		JMenuBar menuBar = new JMenuBar();
		frmMain.setJMenuBar(menuBar);

		JMenu mnModel = new JMenu("Model");
		menuBar.add(mnModel);

		JMenu mnNew = new JMenu("New");
		mnModel.add(mnNew);

		JMenuItem mntmDecisionTree = new JMenuItem("Decision Tree");
		mntmDecisionTree.setIcon(new ImageIcon(frmMain.class.getResource("/images/modelTree_16.png")));
		mntmDecisionTree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AmuaModel newModel=new AmuaModel(0,main,errorLog);
				modelList.add(newModel);
				JScrollPane scrollPane=new JScrollPane();
				scrollPane.setViewportView(newModel.getPanel());
				modelTypes.add(0);
				//add tab
				addTab(0,scrollPane,"(New)");
				tabbedPaneCanvas.setSelectedIndex(tabbedPaneCanvas.getTabCount()-1);
				switchTabs();
				checkAnyModels();
			}
		});
		mnNew.add(mntmDecisionTree);

		JMenuItem mntmMarkovModel = new JMenuItem("Markov Model");
		mntmMarkovModel.setIcon(new ImageIcon(frmMain.class.getResource("/images/markovChain_16.png")));
		mntmMarkovModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AmuaModel newModel=new AmuaModel(1,main,errorLog);
				modelList.add(newModel);
				JScrollPane scrollPane=new JScrollPane();
				scrollPane.setViewportView(newModel.getPanel());
				modelTypes.add(1);
				//add tab
				addTab(1,scrollPane,"(New)");
				tabbedPaneCanvas.setSelectedIndex(tabbedPaneCanvas.getTabCount()-1);
				switchTabs();
				checkAnyModels();
			}
		});
		mnNew.add(mntmMarkovModel);

		JMenuItem mntmOpenModel = new JMenuItem("Open Model...");
		mntmOpenModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					fc.resetChoosableFileFilters();
					fc.addChoosableFileFilter(new AmuaModelFilter());
					fc.setAcceptAllFileFilterUsed(false);

					fc.setDialogTitle("Open Model");
					fc.setApproveButtonText("Open");

					int returnVal = fc.showOpenDialog(frmMain);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						openModel(file);
					}

				}catch(Exception e){
					console.print("Error: "+e.getMessage()); console.newLine();
					errorLog.recordError(e);
				}
			}
		});
		mnModel.add(mntmOpenModel);
		
		final JMenu mnOpenRecent = new JMenu("Open Recent");
		mnOpenRecent.addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent arg0) {
			}
			public void menuDeselected(MenuEvent arg0) {
			}
			public void menuSelected(MenuEvent arg0) {
				recentFiles.buildList(mnOpenRecent,main);
			}
		});
		mnModel.add(mnOpenRecent);
		
		JSeparator separator_2 = new JSeparator();
		mnModel.add(separator_2);

		mntmSave = new JMenuItem("Save");
		KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		mntmSave.setAccelerator(ctrlS);
		mntmSave.setIcon(new ImageIcon(frmMain.class.getResource("/images/save_16.png")));
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveModel();
			}
		});
		mnModel.add(mntmSave);

		mntmSaveAs = new JMenuItem("Save As...");
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fc=null;
					fc=new JFileChooser(curModel.filepath);
					fc.setFileFilter(new AmuaModelFilter());
					fc.setAcceptAllFileFilterUsed(false);

					fc.addChoosableFileFilter(new PNGFilter());
					fc.addChoosableFileFilter(new GIFFilter());
					fc.addChoosableFileFilter(new JPEGFilter());

					fc.setDialogTitle("Save Model");
					fc.setApproveButtonText("Save");

					int returnVal = fc.showSaveDialog(frmMain);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						String fileType=fc.getFileFilter().getDescription();
						File file = fc.getSelectedFile();
						String path=file.getAbsolutePath();
						if(fileType.contains("Amua")){
							String name=file.getName();
							curModel.name=name.replaceAll(".amua", "");
							setTabName(curModel.name,curModelType);
							path=path.replaceAll(".amua", "");
							curModel.filepath=path+".amua";
							curModel.saveModel();
							frmMain.setTitle("Amua - "+curModel.name);
						}
						else{ //Image
							BufferedImage bi=new BufferedImage(curModel.getPanel().getWidth(), curModel.getPanel().getHeight(), BufferedImage.TYPE_INT_RGB);
							Graphics2D g = bi.createGraphics();
							curModel.getPanel().print(g);

							String format="";
							String outFile=path;
							if(fileType.contains("PNG")){
								path=path.replaceAll(".png", "");
								format="png";
								outFile+=".png";
							}
							else if(fileType.contains("GIF")){
								path=path.replaceAll(".gif", "");
								format="gif";
								outFile+=".gif";
							}
							else if(fileType.contains("JPEG")){
								path=path.replaceAll(".jpeg", "");
								format="jpeg";
								outFile+=".jpeg";
							}
							ImageIO.write(bi, format, new File(outFile));
						}
					}

				}catch(Exception e1){
					e1.printStackTrace();
					errorLog.recordError(e1);
				}
			}
		});
		mnModel.add(mntmSaveAs);

		JSeparator separator_6 = new JSeparator();
		mnModel.add(separator_6);
		
		mntmDocument = new JMenuItem("Document...");
		mntmDocument.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//check model
				boolean valid=curModel.checkModel(console, true);
				if(valid==false){
					JOptionPane.showMessageDialog(frmMain, "Errors found!");
				}
				else{
					frmDocument window=new frmDocument(curModel);
					window.frmDocument.setVisible(true);
				}
			}
		});
		mntmDocument.setIcon(new ImageIcon(frmMain.class.getResource("/images/document.png")));
		mnModel.add(mntmDocument);
		
		
		
		mntmExport = new JMenuItem("Export...");
		mntmExport.setIcon(new ImageIcon(frmMain.class.getResource("/images/export.png")));
		mntmExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//check model
				boolean valid=curModel.checkModel(console, true);
				if(valid==false){
					JOptionPane.showMessageDialog(frmMain, "Errors found!");
				}
				else{
					frmExport window=new frmExport(curModel);
					window.frmExport.setVisible(true);
				}
			}
		});
		mnModel.add(mntmExport);
		

		JSeparator separator_5 = new JSeparator();
		mnModel.add(separator_5);

		mntmProperties = new JMenuItem("Properties");
		mntmProperties.setIcon(new ImageIcon(frmMain.class.getResource("/images/propertiesBase.png")));
		mntmProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmProperties window=new frmProperties(curModel);
				window.frmProperties.setVisible(true);
			}
		});
		mnModel.add(mntmProperties);

		JSeparator separator_7 = new JSeparator();
		mnModel.add(separator_7);

		JMenuItem mntmExit = new JMenuItem("Exit");
		//mntmExit.setIcon(new ImageIcon(frmMain.class.getResource("/javax/swing/plaf/metal/icons/ocean/close.gif")));
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		mnModel.add(mntmExit);

		mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);

		mntmUndo = new JMenuItem("Undo");
		mntmUndo.setIcon(new ImageIcon(frmMain.class.getResource("/images/undo_16.png")));
		mntmUndo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				curModel.undoAction();
			}
		});
		mntmUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmUndo.setEnabled(false);
		mnEdit.add(mntmUndo);

		mntmRedo = new JMenuItem("Redo");
		mntmRedo.setIcon(new ImageIcon(frmMain.class.getResource("/images/redo_16.png")));
		mntmRedo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				curModel.redoAction();
			}
		});
		mntmRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmRedo.setEnabled(false);
		mnEdit.add(mntmRedo);

		JSeparator separator_3 = new JSeparator();
		mnEdit.add(separator_3);

		mntmCut = new JMenuItem("Cut");
		mntmCut.setIcon(new ImageIcon(frmMain.class.getResource("/images/cut_16.png")));
		mntmCut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				curModel.cutSubtree();
			}
		});
		mntmCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmCut.setEnabled(false);
		mnEdit.add(mntmCut);

		mntmCopy = new JMenuItem("Copy");
		mntmCopy.setIcon(new ImageIcon(frmMain.class.getResource("/images/copy_16.png")));
		mntmCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				curModel.copySubtree();
			}
		});
		mntmCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmCopy.setEnabled(false);
		mnEdit.add(mntmCopy);

		mntmPaste = new JMenuItem("Paste");
		mntmPaste.setIcon(new ImageIcon(frmMain.class.getResource("/images/paste_16.png")));
		mntmPaste.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				curModel.pasteSubtree();
			}
		});
		mntmPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmPaste.setEnabled(false);
		mnEdit.add(mntmPaste);

		JMenuItem mntmFindreplace = new JMenuItem("Find/Replace");
		mntmFindreplace.setIcon(new ImageIcon(frmMain.class.getResource("/images/find.png")));
		mntmFindreplace.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmFindReplace window=new frmFindReplace(curModel);
				window.frmFindReplace.setVisible(true);
			}
		});

		JSeparator separator_9 = new JSeparator();
		mnEdit.add(separator_9);
		mntmFindreplace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		
		mnEdit.add(mntmFindreplace);

		mnRun = new JMenu("Run");
		menuBar.add(mnRun);

		mntmRunModel = new JMenuItem("Run Model");
		mntmRunModel.setIcon(new ImageIcon(frmMain.class.getResource("/images/runTree_16.png")));
		mntmRunModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread SimThread = new Thread(){ //Non-UI
					public void run(){
						runModel();
					}
				};
				SimThread.start();
			}
		});
		mnRun.add(mntmRunModel);

		JMenu mnSensitivityAnalysis = new JMenu("Sensitivity Analysis");
		mnRun.add(mnSensitivityAnalysis);

		JMenuItem mntmOneway = new JMenuItem("One-way");
		mntmOneway.setIcon(new ImageIcon(frmMain.class.getResource("/images/oneWay.png")));
		mntmOneway.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmSensOneWay window=new frmSensOneWay(curModel);
				window.frmSensOneWay.setVisible(true);
			}
		});
		mnSensitivityAnalysis.add(mntmOneway);

		JMenuItem mntmTornadoDiagram = new JMenuItem("Tornado Diagram");
		mntmTornadoDiagram.setIcon(new ImageIcon(frmMain.class.getResource("/images/tornado.png")));
		mntmTornadoDiagram.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmTornadoDiagram window=new frmTornadoDiagram(curModel);
				window.frmTornadoDiagram.setVisible(true);
			}
		});
		mnSensitivityAnalysis.add(mntmTornadoDiagram);

		JMenuItem mntmThresholdAnalysis = new JMenuItem("Threshold Analysis");
		mntmThresholdAnalysis.setIcon(new ImageIcon(frmMain.class.getResource("/images/threshold.png")));
		mntmThresholdAnalysis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmThreshOneWay window=new frmThreshOneWay(curModel);
				window.frmThreshOneWay.setVisible(true);
			}
		});
		mnSensitivityAnalysis.add(mntmThresholdAnalysis);

		JMenuItem mntmProbabilisticpsa = new JMenuItem("Probabilistic (PSA)");
		mntmProbabilisticpsa.setIcon(new ImageIcon(frmMain.class.getResource("/images/psa.png")));
		mntmProbabilisticpsa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmPSA window=new frmPSA(curModel);
				window.frmPSA.setVisible(true);
			}
		});

		JMenuItem mntmTwoway = new JMenuItem("Two-way");
		mntmTwoway.setIcon(new ImageIcon(frmMain.class.getResource("/images/twoWay.png")));
		mntmTwoway.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmSensTwoWay window=new frmSensTwoWay(curModel);
				window.frmSensTwoWay.setVisible(true);
			}
		});
		mnSensitivityAnalysis.add(mntmTwoway);
		mnSensitivityAnalysis.add(mntmProbabilisticpsa);
		
		JMenuItem mntmBatchRuns = new JMenuItem("Batch Runs");
		mntmBatchRuns.setIcon(new ImageIcon(frmMain.class.getResource("/images/runBatch.png")));
		mntmBatchRuns.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frmBatch window = new frmBatch(curModel);
				window.frmBatch.setVisible(true);
			}
		});
		mnRun.add(mntmBatchRuns);
		
		JMenuItem mntmScenarios = new JMenuItem("Scenarios");
		mntmScenarios.setIcon(new ImageIcon(frmMain.class.getResource("/images/scenario_16.png")));
		mntmScenarios.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmScenarios window = new frmScenarios(curModel);
				window.frmScenarios.setVisible(true);
			}
		});
		mnRun.add(mntmScenarios);

		JSeparator separator_10 = new JSeparator();
		mnRun.add(separator_10);

		mntmCalibrateModel = new JMenuItem("Calibrate Model");
		mntmCalibrateModel.setIcon(new ImageIcon(frmMain.class.getResource("/images/calibrate.png")));
		mntmCalibrateModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(curModel.checkModel(console,true)){
					frmCalibrate window=new frmCalibrate(curModel);
					window.frmCalibrate.setVisible(true);
				}
			}
		});
		mnRun.add(mntmCalibrateModel);
		mntmCalibrateModel.setEnabled(false);

		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);

		JMenuItem mntmPlotFunction = new JMenuItem("Plot Function");
		mntmPlotFunction.setIcon(new ImageIcon(frmMain.class.getResource("/images/plotFx.png")));
		mntmPlotFunction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmPlotFx window=new frmPlotFx(curModel);
				window.frmPlotFx.setVisible(true);
			}
		});
		mnTools.add(mntmPlotFunction);

		JMenuItem mntmPlotSurface = new JMenuItem("Plot Surface");
		mntmPlotSurface.setIcon(new ImageIcon(frmMain.class.getResource("/images/plotSurface.png")));
		mntmPlotSurface.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frmPlotSurface window=new frmPlotSurface(curModel);
				window.frmPlotSurface.setVisible(true);
			}
		});
		mnTools.add(mntmPlotSurface);
	
		JMenuItem mntmTestExpressions = new JMenuItem("Test Expressions");
		mntmTestExpressions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frmTestExpressions window=new frmTestExpressions(curModel);
				window.frmTestExpressions.setVisible(true);
			}
		});
		//mnTools.add(mntmTestExpressions);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmAboutAmua = new JMenuItem("About Amua");
		mntmAboutAmua.setIcon(new ImageIcon(frmMain.class.getResource("/images/logo_16.png")));
		mntmAboutAmua.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmAboutAmua window=new frmAboutAmua(version,errorLog);
				window.frmAboutAmua.setVisible(true);
			}
		});

		JMenuItem mntmHelpContents = new JMenuItem("Help Contents");
		mntmHelpContents.setIcon(new ImageIcon(frmMain.class.getResource("/images/help.png")));
		mntmHelpContents.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Go to URL: https://github.com/zward/Amua/wiki
				try {
					Desktop.getDesktop().browse(new URI("https://github.com/zward/Amua/wiki"));
				} catch (Exception e1) {
					e1.printStackTrace();
					errorLog.recordError(e1);
				}
			}
		});
		mnHelp.add(mntmHelpContents);

		JMenuItem mntmReportBugrequest = new JMenuItem("Report Bug/Request");
		mntmReportBugrequest.setIcon(new ImageIcon(frmMain.class.getResource("/images/bug.png")));
		mntmReportBugrequest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Go to URL: https://github.com/zward/Amua/issues
				try {
					Desktop.getDesktop().browse(new URI("https://github.com/zward/Amua/issues"));
				} catch (Exception e1) {
					e1.printStackTrace();
					errorLog.recordError(e1);
				}
			}
		});
		mnHelp.add(mntmReportBugrequest);

		JMenuItem mntmErrorLog = new JMenuItem("Error Log");
		mntmErrorLog.setIcon(new ImageIcon(frmMain.class.getResource("/images/errorLog.png")));

		mntmErrorLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmErrorLog window=new frmErrorLog(errorLog);
				window.frmErrorLog.setVisible(true);
			}
		});
		mnHelp.add(mntmErrorLog);

		JSeparator separator_11 = new JSeparator();
		mnHelp.add(separator_11);
		mnHelp.add(mntmAboutAmua);


		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 394, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		frmMain.getContentPane().setLayout(gridBagLayout);

		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		GridBagConstraints gbc_toolBar = new GridBagConstraints();
		gbc_toolBar.gridwidth = 2;
		gbc_toolBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_toolBar.insets = new Insets(0, 0, 5, 0);
		gbc_toolBar.gridx = 0;
		gbc_toolBar.gridy = 0;
		frmMain.getContentPane().add(toolBar, gbc_toolBar);

		JSplitPane splitPaneLeft = new JSplitPane();
		splitPaneLeft.setResizeWeight(0.9);
		splitPaneLeft.setOrientation(JSplitPane.VERTICAL_SPLIT);
		GridBagConstraints gbc_splitPaneLeft = new GridBagConstraints();
		gbc_splitPaneLeft.gridwidth = 2;
		gbc_splitPaneLeft.gridheight = 2;
		gbc_splitPaneLeft.insets = new Insets(0, 0, 5, 0);
		gbc_splitPaneLeft.fill = GridBagConstraints.BOTH;
		gbc_splitPaneLeft.gridx = 0;
		gbc_splitPaneLeft.gridy = 1;

		JPanel panelRight=new JPanel();
		JSplitPane splitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,splitPaneLeft,panelRight);
		GridBagLayout gbl_panelRight = new GridBagLayout();
		gbl_panelRight.columnWidths = new int[]{0, 0};
		gbl_panelRight.rowHeights = new int[]{0, 0, 0};
		gbl_panelRight.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelRight.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panelRight.setLayout(gbl_panelRight);

		JToolBar toolBar_1 = new JToolBar();
		toolBar_1.setRollover(true);
		toolBar_1.setFloatable(false);
		GridBagConstraints gbc_toolBar_1 = new GridBagConstraints();
		gbc_toolBar_1.insets = new Insets(0, 0, 5, 0);
		gbc_toolBar_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_toolBar_1.gridx = 0;
		gbc_toolBar_1.gridy = 0;
		panelRight.add(toolBar_1, gbc_toolBar_1);

		tabbedPaneRight = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 1;
		panelRight.add(tabbedPaneRight, gbc_tabbedPane);

		btnAddVariable = new JButton();
		btnAddVariable.setEnabled(false);
		btnAddVariable.setToolTipText("Add");
		URL imageURL = frmMain.class.getResource("/images/add.png");
		btnAddVariable.setIcon(new ImageIcon(imageURL,"Add"));
		btnAddVariable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tabbedPaneRight.getSelectedIndex()==0){ //Parameters
					frmDefineParameter window=new frmDefineParameter(curModel,-1);
					window.frmDefineParameter.setVisible(true);
				}
				else if(tabbedPaneRight.getSelectedIndex()==1){ //Variables
					frmDefineVariable window=new frmDefineVariable(curModel,-1);
					window.frmDefineVariable.setVisible(true);
				}
				else if(tabbedPaneRight.getSelectedIndex()==2){ //Tables
					frmDefineTable window=new frmDefineTable(curModel,-1,fc,errorLog);
					window.frmDefineTable.setVisible(true);
				}
				else if(tabbedPaneRight.getSelectedIndex()==3){ //Constraints
					frmDefineConstraint window=new frmDefineConstraint(curModel,-1);
					window.frmDefineConstraint.setVisible(true);
				}
			}
		});
		toolBar_1.add(btnAddVariable);

		final JButton btnDeleteVariable = new JButton();
		btnDeleteVariable.setToolTipText("Delete");
		imageURL = frmMain.class.getResource("/images/delete.png");
		btnDeleteVariable.setIcon(new ImageIcon(imageURL,"Delete"));
		btnDeleteVariable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tabbedPaneRight.getSelectedIndex()==0){ //Parameters
					int selectedRow=tableParameters.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableParameters.convertRowIndexToModel(selectedRow);
						curModel.deleteParameter(dataRow);
						curModel.rescale(curModel.scale); //Re-validates textfields
					}
				}
				else if(tabbedPaneRight.getSelectedIndex()==1){ //Variables
					int selectedRow=tableVariables.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableVariables.convertRowIndexToModel(selectedRow);
						curModel.deleteVariable(dataRow);
						curModel.rescale(curModel.scale); //Re-validates textfields
					}
				}
				else if(tabbedPaneRight.getSelectedIndex()==2){ //Tables
					int selectedRow=tableTables.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableTables.convertRowIndexToModel(selectedRow);
						curModel.deleteTable(dataRow);
						curModel.rescale(curModel.scale); //Re-validates textfields
					}
				}
				else if(tabbedPaneRight.getSelectedIndex()==3){ //Constraint
					int selectedRow=tableConstraints.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableConstraints.convertRowIndexToModel(selectedRow);
						curModel.deleteConstraint(dataRow);
					}
				}
			}
		});

		final JButton btnEdit = new JButton();
		btnEdit.setToolTipText("Edit");
		imageURL = frmMain.class.getResource("/images/edit_16.png");
		btnEdit.setIcon(new ImageIcon(imageURL,"Edit"));
		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tabbedPaneRight.getSelectedIndex()==0){ //Parameters
					int selectedRow=tableParameters.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableParameters.convertRowIndexToModel(selectedRow);
						frmDefineParameter window=new frmDefineParameter(curModel,dataRow);
						window.frmDefineParameter.setVisible(true);
					}
				}
				else if(tabbedPaneRight.getSelectedIndex()==1){ //Variables
					int selectedRow=tableVariables.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableVariables.convertRowIndexToModel(selectedRow);
						frmDefineVariable window=new frmDefineVariable(curModel,dataRow);
						window.frmDefineVariable.setVisible(true);
					}
				}
				else if(tabbedPaneRight.getSelectedIndex()==2){ //Tables
					int selectedRow=tableTables.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableTables.convertRowIndexToModel(selectedRow);
						frmDefineTable window=new frmDefineTable(curModel,dataRow,fc,errorLog);
						window.frmDefineTable.setVisible(true);
					}
				}
				else if(tabbedPaneRight.getSelectedIndex()==3){ //Constraints
					int selectedRow=tableConstraints.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableConstraints.convertRowIndexToModel(selectedRow);
						frmDefineConstraint window=new frmDefineConstraint(curModel,dataRow);
						window.frmDefineConstraint.setVisible(true);
					}
				}
			}
		});
		toolBar_1.add(btnEdit);
		toolBar_1.add(btnDeleteVariable);

		JButton btnShowuse = new JButton();
		btnShowuse.setToolTipText("Highlight Use");
		btnShowuse.setIcon(new ImageIcon(frmMain.class.getResource("/images/highlight_16.png")));
		btnShowuse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tabbedPaneRight.getSelectedIndex()==0){ //Parameters
					int selectedRow=tableParameters.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableParameters.convertColumnIndexToModel(selectedRow);
						curModel.highlightParameter(dataRow);
					}
				}
				else if(tabbedPaneRight.getSelectedIndex()==1){ //Variables
					int selectedRow=tableVariables.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableVariables.convertRowIndexToModel(selectedRow);
						curModel.highlightVariable(dataRow);
					}
				}
				else if(tabbedPaneRight.getSelectedIndex()==2){ //Tables
					int selectedRow=tableTables.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableTables.convertRowIndexToModel(selectedRow);
						curModel.highlightTable(dataRow);
					}
				}
			}
		});
		toolBar_1.add(btnShowuse);

		tabbedPaneRight.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if(tabbedPaneRight.getSelectedIndex()==0){ //Parameters
					btnAddVariable.setToolTipText("Add Parameter");
					btnDeleteVariable.setToolTipText("Delete Parameter");
					btnEdit.setToolTipText("Edit Parameter");
				}
				else if(tabbedPaneRight.getSelectedIndex()==1){ //Variables
					btnAddVariable.setToolTipText("Add Variable");
					btnDeleteVariable.setToolTipText("Delete Variable");
					btnEdit.setToolTipText("Edit Variable");
				}
				else if(tabbedPaneRight.getSelectedIndex()==2){ //Tables
					btnAddVariable.setToolTipText("Add Table");
					btnDeleteVariable.setToolTipText("Delete Table");
					btnEdit.setToolTipText("Edit Table");
				}
				else if(tabbedPaneRight.getSelectedIndex()==3){ //Constraints
					btnAddVariable.setToolTipText("Add Constraint");
					btnDeleteVariable.setToolTipText("Delete Constraint");
					btnEdit.setToolTipText("Edit Constraint");
				}
			}
		});

		//Parameters
		JPanel panelParams = new JPanel();
		panelParams.setLayout(new BoxLayout(panelParams, BoxLayout.X_AXIS));
		JLabel lblParam=new JLabel("Parameters");
		Icon iconParameter=new ImageIcon(frmMain.class.getResource("/images/parameters2.png"));
		lblParam.setIcon(iconParameter);
		lblParam.setHorizontalTextPosition(SwingConstants.RIGHT);
		tabbedPaneRight.addTab("Parameters", null, panelParams, null);
		tabbedPaneRight.setTabComponentAt(0, lblParam);
		
		JScrollPane scrollPaneParameters = new JScrollPane();
		GridBagConstraints gbc_scrollPaneParameters = new GridBagConstraints();
		gbc_scrollPaneParameters.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneParameters.gridx = 0;
		gbc_scrollPaneParameters.gridy = 1;
		panelParams.add(scrollPaneParameters);

		modelParameters=new DefaultTableModel(new Object[][] {},	new String[] {"Name", "Expression"})
		{boolean[] columnEditables = new boolean[] {false, false};
		public boolean isCellEditable(int row, int column) {
			return columnEditables[column];
		}
		};

		tableParameters = new JTable();
		tableParameters.setRowSelectionAllowed(false);
		tableParameters.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER){ //Enter
					int selectedRow=tableParameters.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableParameters.convertRowIndexToModel(selectedRow);
						frmDefineParameter window=new frmDefineParameter(curModel,dataRow);
						window.frmDefineParameter.setVisible(true);
					}
				}
				else if(e.getKeyCode()==KeyEvent.VK_DELETE){ //Delete
					int selectedRow=tableParameters.getSelectedRow();
					if(selectedRow!=-1){
						e.consume();
						int dataRow=tableParameters.convertRowIndexToModel(selectedRow);
						curModel.deleteParameter(dataRow);
						curModel.rescale(curModel.scale); //Re-validates textfields
					}
				}
			}
		});
		tableParameters.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2){
					int selectedRow=tableParameters.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableParameters.convertRowIndexToModel(selectedRow);
						frmDefineParameter window=new frmDefineParameter(curModel,dataRow);
						window.frmDefineParameter.setVisible(true);
					}
				}
			}
		});
		tableParameters.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
				int dataRow=tableParameters.convertRowIndexToModel(row);
				Parameter curParam=curModel.parameters.get(dataRow);
				if(curParam.valid){setForeground(table.getForeground());}
				else{setForeground(Color.RED);}
				return this;
			}   
		});
		tableParameters.setShowVerticalLines(true);
		tableParameters.setModel(modelParameters);
		tableParameters.getTableHeader().setReorderingAllowed(false);
		tableParameters.setAutoCreateRowSorter(true);
		scrollPaneParameters.setViewportView(tableParameters);

		//Variables
		JPanel panelVars = new JPanel();
		panelVars.setLayout(new BoxLayout(panelVars, BoxLayout.X_AXIS));
		JLabel lblVar=new JLabel("Variables");
		Icon iconVariable=new ImageIcon(frmMain.class.getResource("/images/variable.png"));
		lblVar.setIcon(iconVariable);
		lblVar.setHorizontalTextPosition(SwingConstants.RIGHT);
		tabbedPaneRight.addTab("Variables", null, panelVars, null);
		tabbedPaneRight.setTabComponentAt(1, lblVar);

		JScrollPane scrollPaneVariables = new JScrollPane();
		GridBagConstraints gbc_scrollPaneVariables = new GridBagConstraints();
		gbc_scrollPaneVariables.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneVariables.gridx = 0;
		gbc_scrollPaneVariables.gridy = 1;
		panelVars.add(scrollPaneVariables);

		modelVariables=new DefaultTableModel(new Object[][] {},	new String[] {"Name", "Expression"})
		{boolean[] columnEditables = new boolean[] {false, false};
		public boolean isCellEditable(int row, int column) {
			return columnEditables[column];
		}
		};

		tableVariables = new JTable();
		//tableVariables.setFont(new Font("Consolas", Font.PLAIN, 12));
		tableVariables.setRowSelectionAllowed(false);
		tableVariables.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER){ //Enter
					int selectedRow=tableVariables.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableVariables.convertRowIndexToModel(selectedRow);
						frmDefineVariable window=new frmDefineVariable(curModel,dataRow);
						window.frmDefineVariable.setVisible(true);
					}
				}
				else if(e.getKeyCode()==KeyEvent.VK_DELETE){ //Delete
					int selectedRow=tableVariables.getSelectedRow();
					if(selectedRow!=-1){
						e.consume();
						int dataRow=tableVariables.convertRowIndexToModel(selectedRow);
						curModel.deleteVariable(dataRow);
						curModel.rescale(curModel.scale); //Re-validates textfields
					}
				}
			}
		});
		tableVariables.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2){
					int selectedRow=tableVariables.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableVariables.convertRowIndexToModel(selectedRow);
						frmDefineVariable window=new frmDefineVariable(curModel,dataRow);
						window.frmDefineVariable.setVisible(true);
					}
				}
			}
		});
		tableVariables.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
				int dataRow=tableVariables.convertRowIndexToModel(row);
				Variable curVar=curModel.variables.get(dataRow);
				if(curVar.valid){setForeground(table.getForeground());}
				else{setForeground(Color.RED);}
				return this;
			}   
		});
		tableVariables.setShowVerticalLines(true);
		tableVariables.setModel(modelVariables);
		tableVariables.getTableHeader().setReorderingAllowed(false);
		tableVariables.setAutoCreateRowSorter(true);
		scrollPaneVariables.setViewportView(tableVariables);

		//Tables
		JPanel panelTables = new JPanel();
		panelTables.setLayout(new BoxLayout(panelTables, BoxLayout.X_AXIS));

		JLabel lblTables=new JLabel("Tables");
		Icon iconTables=new ImageIcon(frmMain.class.getResource("/images/table.png"));
		lblTables.setIcon(iconTables);
		lblTables.setHorizontalTextPosition(SwingConstants.RIGHT);
		tabbedPaneRight.addTab("Tables", null, panelTables, null);
		tabbedPaneRight.setTabComponentAt(2, lblTables);

		JScrollPane scrollPaneTables = new JScrollPane();
		panelTables.add(scrollPaneTables);

		modelTables=new DefaultTableModel(new Object[][] {},	new String[] {"Name","Type","Size"})
		{boolean[] columnEditables = new boolean[] {false, false, false};
		public boolean isCellEditable(int row, int column) {
			return columnEditables[column];
		}
		};

		tableTables = new JTable();
		tableTables.setShowVerticalLines(true);
		tableTables.setModel(modelTables);
		tableTables.setRowSelectionAllowed(false);
		tableTables.getTableHeader().setReorderingAllowed(false);
		tableTables.setAutoCreateRowSorter(true);
		tableTables.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER){ //Enter
					int selectedRow=tableTables.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableTables.convertRowIndexToModel(selectedRow);
						frmDefineTable window=new frmDefineTable(curModel,dataRow,fc,errorLog);
						window.frmDefineTable.setVisible(true);
					}
				}
				else if(e.getKeyCode()==KeyEvent.VK_DELETE){ //Delete
					int selectedRow=tableTables.getSelectedRow();
					if(selectedRow!=-1){
						e.consume();
						int dataRow=tableTables.convertRowIndexToModel(selectedRow);
						curModel.deleteTable(dataRow);
						curModel.rescale(curModel.scale); //Re-validates textfields
					}
				}
			}
		});
		tableTables.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2){
					int selectedRow=tableTables.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableTables.convertRowIndexToModel(selectedRow);
						frmDefineTable window=new frmDefineTable(curModel,dataRow,fc,errorLog);
						window.frmDefineTable.setVisible(true);
					}
				}
			}
		});
		scrollPaneTables.setViewportView(tableTables);

		//Constraints
		JPanel panelConst = new JPanel();
		panelConst.setLayout(new BoxLayout(panelConst, BoxLayout.X_AXIS));
		JLabel lblConst=new JLabel("Constraints");
		Icon iconConst=new ImageIcon(frmMain.class.getResource("/images/constraint.png"));
		lblConst.setIcon(iconConst);
		lblConst.setHorizontalTextPosition(SwingConstants.RIGHT);
		tabbedPaneRight.addTab("Constraints", null, panelConst, null);
		tabbedPaneRight.setTabComponentAt(3, lblConst);

		JScrollPane scrollPaneConst = new JScrollPane();
		GridBagConstraints gbc_scrollPaneConst = new GridBagConstraints();
		gbc_scrollPaneConst.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneConst.gridx = 0;
		gbc_scrollPaneConst.gridy = 1;
		panelConst.add(scrollPaneConst);

		modelConstraints=new DefaultTableModel(new Object[][] {},	new String[] {"Name", "Expression"})
		{boolean[] columnEditables = new boolean[] {false, false};
		public boolean isCellEditable(int row, int column) {
			return columnEditables[column];
		}
		};

		tableConstraints = new JTable();
		tableConstraints.setRowSelectionAllowed(false);
		tableConstraints.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER){ //Enter
					int selectedRow=tableConstraints.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableConstraints.convertRowIndexToModel(selectedRow);
						frmDefineConstraint window=new frmDefineConstraint(curModel,dataRow);
						window.frmDefineConstraint.setVisible(true);
					}
				}
				else if(e.getKeyCode()==KeyEvent.VK_DELETE){ //Delete
					int selectedRow=tableConstraints.getSelectedRow();
					if(selectedRow!=-1){
						e.consume();
						int dataRow=tableConstraints.convertRowIndexToModel(selectedRow);
						curModel.deleteConstraint(dataRow);
						curModel.rescale(curModel.scale); //Re-validates textfields
					}
				}
			}
		});
		tableConstraints.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2){
					int selectedRow=tableConstraints.getSelectedRow();
					if(selectedRow!=-1){
						int dataRow=tableConstraints.convertRowIndexToModel(selectedRow);
						frmDefineConstraint window=new frmDefineConstraint(curModel,dataRow);
						window.frmDefineConstraint.setVisible(true);
					}
				}
			}
		});
		tableConstraints.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
				int dataRow=tableConstraints.convertRowIndexToModel(row);
				Constraint curConst=curModel.constraints.get(dataRow);
				if(curConst.valid){setForeground(table.getForeground());}
				else{setForeground(Color.RED);}
				return this;
			}   
		});
		tableConstraints.setShowVerticalLines(true);
		tableConstraints.setModel(modelConstraints);
		tableConstraints.getTableHeader().setReorderingAllowed(false);
		tableConstraints.setAutoCreateRowSorter(true);
		scrollPaneConst.setViewportView(tableConstraints);
		
		//Parameter sets
		modelParamSets=new DefaultTableModel(new Object[][] {}, new String[] {"Set", "Score"}); 
		tableParamSets = new JTable();
		tableParamSets.setModel(modelParamSets);
		tableParamSets.setShowVerticalLines(true);
		tableParamSets.setEnabled(false);
		tableParamSets.setRowSelectionAllowed(false);
		tableParamSets.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableParamSets.getTableHeader().setReorderingAllowed(false);


		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.9);
		frmMain.getContentPane().add(splitPane, gbc_splitPaneLeft);


		JPanel panelLeft=new JPanel();
		splitPaneLeft.setLeftComponent(panelLeft);
		GridBagLayout gbl_panelLeft = new GridBagLayout();
		gbl_panelLeft.columnWidths = new int[]{0, 0, 0};
		gbl_panelLeft.rowHeights = new int[]{34, 0, 0};
		gbl_panelLeft.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panelLeft.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panelLeft.setLayout(gbl_panelLeft);

		/*JLabel lblFx = new JLabel("");
		lblFx.setHorizontalTextPosition(SwingConstants.LEADING);
		lblFx.setHorizontalAlignment(SwingConstants.LEADING);
		lblFx.setIcon(new ImageIcon(frmMain.class.getResource("/images/formula.png")));*/
		JToolBar toolbarFx=new JToolBar();
		toolbarFx.setFloatable(false);
		toolbarFx.setRollover(true);
		toolbarFx.setBorderPainted(false);
		
		btnFx=new JButton("");
		btnFx.setEnabled(false);
		btnFx.setFocusable(false);
		btnFx.setFocusPainted(false);
		btnFx.setIcon(new ImageIcon(frmMain.class.getResource("/images/formula.png")));
		btnFx.setToolTipText("Build Expression");
		btnFx.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				curFxPane.requestFocus();
				frmExpressionBuilder window=new frmExpressionBuilder(curModel,curFxPane,updateCurFx);
				window.frmExpressionBuilder.setVisible(true);
			}
		});
		toolbarFx.add(btnFx);
		
		GridBagConstraints gbc_lblFx = new GridBagConstraints();
		gbc_lblFx.anchor = GridBagConstraints.EAST;
		gbc_lblFx.gridx = 0;
		gbc_lblFx.gridy = 0;
		panelLeft.add(toolbarFx, gbc_lblFx);

		scrollPaneFx = new JScrollPane();
		GridBagConstraints gbc_scrollPaneFx = new GridBagConstraints();
		gbc_scrollPaneFx.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneFx.gridx = 1;
		gbc_scrollPaneFx.gridy = 0;
		panelLeft.add(scrollPaneFx, gbc_scrollPaneFx);

		tabbedPaneCanvas = new JTabbedPane(JTabbedPane.TOP);
		tabbedPaneCanvas.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				switchTabs();
			}
		});
		GridBagConstraints gbc_scrollPaneCanvas = new GridBagConstraints();
		gbc_scrollPaneCanvas.gridwidth = 2;
		gbc_scrollPaneCanvas.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneCanvas.gridx = 0;
		gbc_scrollPaneCanvas.gridy = 1;
		panelLeft.add(tabbedPaneCanvas,gbc_scrollPaneCanvas);

		tabbedPaneBottom = new JTabbedPane(JTabbedPane.TOP);
		splitPaneLeft.setRightComponent(tabbedPaneBottom);

		JScrollPane scrollPaneConsole = new JScrollPane();
		JLabel lblConsole=new JLabel("Console");
		Icon iconConsole=new ImageIcon(frmMain.class.getResource("/images/console.png"));
		lblConsole.setIcon(iconConsole);
		lblConsole.setHorizontalTextPosition(SwingConstants.RIGHT);
		tabbedPaneBottom.addTab("Console", null, scrollPaneConsole, null);
		tabbedPaneBottom.setTabComponentAt(0, lblConsole);

		//console = new JTextArea();
		//console.setEditable(false);
		//scrollPaneConsole.setViewportView(console);
		scrollPaneConsole.setViewportView(console.textConsole);

		scrollPaneNotes = new JScrollPane();
		JLabel lblNotes=new JLabel("Notes");
		Icon iconNotes=new ImageIcon(frmMain.class.getResource("/images/notes.png"));
		lblNotes.setIcon(iconNotes);
		lblNotes.setHorizontalTextPosition(SwingConstants.RIGHT);
		tabbedPaneBottom.addTab("Notes", (Icon) null, scrollPaneNotes, null);
		tabbedPaneBottom.setTabComponentAt(1, lblNotes);

		//Parameter Sets Panel
		panelParamSets = new JPanel();
		panelParamSets.setBounds(12, 13, 459, 217);
		GridBagLayout gbl_panel_paramSets = new GridBagLayout();
		gbl_panel_paramSets.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panel_paramSets.rowHeights = new int[]{0, 0, 0};
		gbl_panel_paramSets.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_paramSets.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panelParamSets.setLayout(gbl_panel_paramSets);

		chckbxUseParamSets = new JCheckBox("Use Parameter Sets");
		chckbxUseParamSets.setEnabled(false);
		chckbxUseParamSets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				curModel.simParamSets=chckbxUseParamSets.isSelected();
			}
		});
		GridBagConstraints gbc_chckbxUseParamSets= new GridBagConstraints();
		gbc_chckbxUseParamSets.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxUseParamSets.gridx = 0;
		gbc_chckbxUseParamSets.gridy = 0;
		panelParamSets.add(chckbxUseParamSets, gbc_chckbxUseParamSets);

		JButton btnExportParamSets = new JButton("Export...");
		btnExportParamSets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(curModel.parameterNames!=null){
					//Export parameter sets
					try {
						JFileChooser fc=null;
						fc=new JFileChooser(curModel.filepath);
						fc.setFileFilter(new CSVFilter());
						fc.setDialogTitle("Export Parameter Sets");
						fc.setApproveButtonText("Export");

						int returnVal = fc.showSaveDialog(frmMain);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String path=file.getAbsolutePath();
							path=path.replace(".csv", "");
							//Open file for writing
							FileWriter fstream = new FileWriter(path+".csv"); //Create new file
							BufferedWriter out = new BufferedWriter(fstream);
							int numParams=curModel.parameterNames.length;
							//Headers
							out.write("Set,Score");
							for(int i=0; i<numParams; i++){out.write(","+curModel.parameterNames[i]);}
							out.newLine();
							//Sets
							int numSets=curModel.parameterSets.length;
							for(int i=0; i<numSets; i++){
								ParameterSet curSet=curModel.parameterSets[i];
								out.write(curSet.id+","+curSet.score);
								for(int j=0; j<numParams; j++){out.write(","+curSet.values[j].toString());}
								out.newLine();
							}
							out.close();

							JOptionPane.showMessageDialog(frmMain, "Exported!");
						}

					}catch(Exception e1){
						e1.printStackTrace();
						errorLog.recordError(e1);
					}
				}
			}
		});
		GridBagConstraints gbc_btnExportParamSets = new GridBagConstraints();
		gbc_btnExportParamSets.insets = new Insets(0, 0, 5, 5);
		gbc_btnExportParamSets.gridx = 1;
		gbc_btnExportParamSets.gridy = 0;
		panelParamSets.add(btnExportParamSets, gbc_btnExportParamSets);

		JButton btnClearParamSets = new JButton("Clear All");
		btnClearParamSets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(curModel.parameterNames!=null){
					//remove parameter sets
					curModel.saveSnapshot("Clear Parameter Sets");
					curModel.parameterNames=null;
					curModel.parameterSets=null;
					modelParamSets.setRowCount(0);
					modelParamSets.setColumnCount(0);
				}
			}
		});
		GridBagConstraints gbc_btnClearParamSets = new GridBagConstraints();
		gbc_btnClearParamSets.insets = new Insets(0, 0, 5, 5);
		gbc_btnClearParamSets.gridx = 2;
		gbc_btnClearParamSets.gridy = 0;
		panelParamSets.add(btnClearParamSets, gbc_btnClearParamSets);

		JScrollPane scrollPaneParamSets = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		panelParamSets.add(scrollPaneParamSets, gbc_scrollPane);
		scrollPaneParamSets.setViewportView(tableParamSets);

		JLabel lblParamSets=new JLabel("Parameter Sets");
		Icon iconParamSets=new ImageIcon(frmMain.class.getResource("/images/parameterSets.png"));
		lblParamSets.setIcon(iconParamSets);
		lblParamSets.setHorizontalTextPosition(SwingConstants.RIGHT);
		tabbedPaneBottom.addTab("Parameter Sets", (Icon) null, panelParamSets, null);
		tabbedPaneBottom.setTabComponentAt(2, lblParamSets);
		//chckbxUseParamSets.setSelected(curModel.simParamSets);

		scrollPaneProperties = new JScrollPane();


		btnDecisionNode=new JButton();
		btnDecisionNode.setEnabled(false);
		btnDecisionNode.setToolTipText("Decision Node");
		imageURL = frmMain.class.getResource("/images/decisionNode.png");
		btnDecisionNode.setIcon(new ImageIcon(imageURL,"Decision Node"));
		toolBar.add(btnDecisionNode);
		btnDecisionNode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				curModel.addNode(0);
			}
		});

		btnMarkovChain=new JButton();
		btnMarkovChain.setEnabled(false);
		btnMarkovChain.setToolTipText("Markov Chain");
		imageURL=frmMain.class.getResource("/images/markovChain.png");
		btnMarkovChain.setIcon(new ImageIcon(imageURL,"Markov Chain"));
		btnMarkovChain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(curModelType==1){
					curModel.addNode(1);
				}
			}
		});

		btnMarkovState=new JButton();
		btnMarkovState.setEnabled(false);
		btnMarkovState.setToolTipText("Markov State");
		imageURL=frmMain.class.getResource("/images/markovState.png");
		btnMarkovState.setIcon(new ImageIcon(imageURL,"Markov State"));
		btnMarkovState.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(curModelType==1){
					curModel.addNode(2);
				}
			}
		});

		btnChanceNode=new JButton();
		btnChanceNode.setEnabled(false);
		btnChanceNode.setToolTipText("Chance Node");
		imageURL = frmMain.class.getResource("/images/chanceNode.png");
		btnChanceNode.setIcon(new ImageIcon(imageURL,"Chance Node"));
		toolBar.add(btnChanceNode);
		btnChanceNode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(curModelType==0){
					curModel.addNode(1);
				}
				else if(curModelType==1){
					curModel.addNode(3);
				}
			}
		});


		btnTerminalNode=new JButton();
		btnTerminalNode.setEnabled(false);
		btnTerminalNode.setToolTipText("Terminal Node");
		imageURL = frmMain.class.getResource("/images/terminalNode.png");
		btnTerminalNode.setIcon(new ImageIcon(imageURL,"Terminal Node"));
		toolBar.add(btnTerminalNode);
		btnTerminalNode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(curModelType==0){
					curModel.addNode(2);
				}
			}
		});

		btnStateTransition=new JButton();
		btnStateTransition.setEnabled(false);;
		btnStateTransition.setToolTipText("State Transition");
		imageURL = frmMain.class.getResource("/images/stateTransition.png");
		btnStateTransition.setIcon(new ImageIcon(imageURL,"State Transition"));
		btnStateTransition.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				if(curModelType==1){
					curModel.addNode(4);
				}
			}
		});

		btnChangeNodeType=new JButton();
		btnChangeNodeType.setEnabled(false);
		btnChangeNodeType.setToolTipText("Change Node Type");
		imageURL = frmMain.class.getResource("/images/changeType.png");
		btnChangeNodeType.setIcon(new ImageIcon(imageURL,"Change Node Type"));
		toolBar.add(btnChangeNodeType);
		btnChangeNodeType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				curModel.changeNodeType();
			}
		});

		btnUpdateVariable=new JButton();
		btnUpdateVariable.setEnabled(false);;
		btnUpdateVariable.setToolTipText("Add/Remove Variable Updates");
		imageURL = frmMain.class.getResource("/images/updateVariable.png");
		btnUpdateVariable.setIcon(new ImageIcon(imageURL,"Add/Remove Variable Updates"));
		btnUpdateVariable.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				curModel.addRemoveVarUpdates();
			}
		});
		toolBar.add(btnUpdateVariable);

		btnShowCost = new JButton();
		btnShowCost.setEnabled(false);
		btnShowCost.setToolTipText("Add/Remove Cost");
		imageURL = frmMain.class.getResource("/images/cost.png");
		btnShowCost.setIcon(new ImageIcon(imageURL,"Add/Remove Cost"));
		btnShowCost.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				curModel.addRemoveCost();
			}
		});
		toolBar.add(btnShowCost);

		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator);

		btnOCD = new JButton("OCD");
		btnOCD.setFont(new Font("SansSerif", Font.PLAIN, 12));
		btnOCD.setToolTipText("Optimize Current Display");
		btnOCD.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				curModel.ocd("OCD");
				frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		toolBar.add(btnOCD);

		btnEqualY = new JButton();
		btnEqualY.setEnabled(false);
		btnEqualY.setToolTipText("Vertical Spacing");
		imageURL = frmMain.class.getResource("/images/equalY.png");
		btnEqualY.setIcon(new ImageIcon(imageURL,"Vertical Spacing"));
		btnEqualY.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				curModel.equalY();
				frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		toolBar.add(btnEqualY);

		btnCollapse = new JButton();
		btnCollapse.setEnabled(false);
		btnCollapse.setToolTipText("Collapse/Expand Branch");
		imageURL = frmMain.class.getResource("/images/collapse.png");
		btnCollapse.setIcon(new ImageIcon(imageURL,"Collapse/Expand Branch"));
		btnCollapse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				curModel.collapseBranch();
			}
		});
		toolBar.add(btnCollapse);

		btnAlignLeft = new JButton();
		btnAlignRight = new JButton();

		btnAlignLeft.setToolTipText("Align Left");
		imageURL = frmMain.class.getResource("/images/alignLeftSelected.png");
		btnAlignLeft.setIcon(new ImageIcon(imageURL,"Align Left"));
		btnAlignLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				curModel.alignRight=false;
				curModel.ocd("Align Left");

				URL imageURL = frmMain.class.getResource("/images/alignLeftSelected.png");
				btnAlignLeft.setIcon(new ImageIcon(imageURL,"Align Left"));
				imageURL = frmMain.class.getResource("/images/alignRight.png");
				btnAlignRight.setIcon(new ImageIcon(imageURL,"Align Right"));
				frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		toolBar.add(btnAlignLeft);

		btnAlignRight.setToolTipText("Align Right");
		imageURL = frmMain.class.getResource("/images/alignRight.png");
		btnAlignRight.setIcon(new ImageIcon(imageURL,"Align Right"));
		btnAlignRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				curModel.alignRight=true;
				curModel.ocd("Align Right");

				URL imageURL = frmMain.class.getResource("/images/alignRightSelected.png");
				btnAlignRight.setIcon(new ImageIcon(imageURL,"Align Right"));
				imageURL = frmMain.class.getResource("/images/alignLeft.png");
				btnAlignLeft.setIcon(new ImageIcon(imageURL,"Align Left"));
				frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		toolBar.add(btnAlignRight);

		JSeparator separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator_1);

		btnCheckModel=new JButton();
		btnCheckModel.setToolTipText("Check Model");
		imageURL = frmMain.class.getResource("/images/checkTree.png");
		btnCheckModel.setIcon(new ImageIcon(imageURL,"Check Tree"));
		toolBar.add(btnCheckModel);
		btnCheckModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				curModel.checkModel(console,true);
				frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});

		btnRunModel=new JButton();
		btnRunModel.setToolTipText("Run Model");
		imageURL = frmMain.class.getResource("/images/runTree.png");
		btnRunModel.setIcon(new ImageIcon(imageURL,"Run Tree"));
		toolBar.add(btnRunModel);
		btnRunModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Thread SimThread = new Thread(){ //Non-UI
					public void run(){
						//long startTime=System.currentTimeMillis(); //time testing
						runModel();
						//long endTime=System.currentTimeMillis();
						//System.out.println(endTime-startTime);
					}
				};
				SimThread.start();
			}
		});

		btnClearAnnotations=new JButton();
		btnClearAnnotations.setToolTipText("Clear");
		imageURL = frmMain.class.getResource("/images/clearEV.png");
		btnClearAnnotations.setIcon(new ImageIcon(imageURL,"Clear"));
		toolBar.add(btnClearAnnotations);
		btnClearAnnotations.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				curModel.clearAnnotations();
				frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});

		JSeparator separator_4 = new JSeparator();
		separator_4.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator_4);
		imageURL = frmMain.class.getResource("/images/screenshot.png");

		final JLabel lblZoom = new JLabel("100% ");

		sliderZoom = new JSlider();
		sliderZoom.setPaintTicks(true);
		sliderZoom.setMaximum(480);
		sliderZoom.setMinorTickSpacing(240);
		sliderZoom.setValue(240);
		sliderZoom.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int val=sliderZoom.getValue();
				int zoom=100;
				if(val<240){zoom=(int) Math.round(20+val/3.0);}
				else{zoom=(int)Math.round(100+(5/3.0)*(val-240));}
				lblZoom.setText(zoom+"% ");
				curModel.rescale(zoom);
			}
		});

		btnZoomOut = new JButton(" \u2013 ");
		btnZoomOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Zoom out, increments of 10%
				int curScale=100;
				curScale=curModel.scale;
				//Round up to nearest 10%
				int rem=curScale%10;
				if(rem!=0){curScale+=(10-rem);}
				int newScale=Math.max(curScale-10,20); //Floor of 20
				frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				setZoomVal(newScale);
				frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		btnZoomOut.setToolTipText("Zoom Out");
		btnZoomOut.setFont(new Font("Tahoma", Font.PLAIN, 16));
		toolBar.add(btnZoomOut);
		sliderZoom.setToolTipText("Zoom");
		toolBar.add(sliderZoom);

		btnZoomIn = new JButton(" + ");
		btnZoomIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Zoom in, increments of 10%
				int curScale=100;
				curScale=curModel.scale;
				//Round down to nearest 10%
				int rem=curScale%10;
				curScale-=rem;
				int newScale=Math.min(curScale+10,500); //Ceiling of 500
				frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				setZoomVal(newScale);
				frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		btnZoomIn.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnZoomIn.setToolTipText("Zoom In");
		toolBar.add(btnZoomIn);
		toolBar.add(lblZoom);

		JSeparator separator_8 = new JSeparator();
		separator_8.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator_8);

		btnSnapshot=new JButton();
		btnSnapshot.setToolTipText("Screenshot");
		btnSnapshot.setIcon(new ImageIcon(imageURL,"Screenshot"));
		toolBar.add(btnSnapshot);

		btnSnapshot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				BufferedImage bi=new BufferedImage(curModel.getPanel().getWidth(), curModel.getPanel().getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D g = bi.createGraphics();
				curModel.getPanel().print(g);

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				ImageTransferable selection = new ImageTransferable(bi);
				clipboard.setContents(selection, null);
				frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});

	}

	private void addTab(int modelType, JScrollPane scrollPane, String name){
		tabbedPaneCanvas.addTab("", scrollPane);
		tabbedPaneCanvas.setSelectedIndex(tabbedPaneCanvas.getTabCount()-1);
		setTabName(name,modelType);
	}

	public void setTabName(String name, int modelType){
		int index=tabbedPaneCanvas.getSelectedIndex();
		final JPanel pnlTab = new JPanel(new GridBagLayout());
		pnlTab.setOpaque(false);
		JLabel lbl=new JLabel(name);
		Icon icon=null;
		if(modelType==0){ //Decision tree
			icon=new ImageIcon(frmMain.class.getResource("/images/modelTree_16.png"));
		}
		else if(modelType==1){ //Markov model
			icon=new ImageIcon(frmMain.class.getResource("/images/markovChain_16.png"));
		}
		lbl.setIcon(icon);
		lbl.setIconTextGap(5);
		lbl.setHorizontalTextPosition(SwingConstants.RIGHT);

		JButton btnClose = new JButton();
		btnClose.setIcon(new ImageIcon(frmMain.class.getResource("/images/close_7.png")));
		btnClose.setRolloverIcon(new ImageIcon(frmMain.class.getResource("/images/close_press_7.png")));
		btnClose.setOpaque(false);
		btnClose.setPreferredSize(new Dimension(20,10));
		btnClose.setContentAreaFilled(false);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;

		pnlTab.add(lbl, gbc);

		gbc.gridx++;
		gbc.weightx = 0;
		pnlTab.add(btnClose, gbc);


		btnClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int index=tabbedPaneCanvas.indexOfTabComponent(pnlTab);
				closeTab(index);
			}
		});

		tabbedPaneCanvas.setTabComponentAt(index, pnlTab);
	}

	private void switchTabs(){
		int index=tabbedPaneCanvas.getSelectedIndex();
		if(index!=-1){ //tabs exist
			curModelType=modelTypes.get(index);
			curModel=modelList.get(index);
			if(curModelType==0){ //Decision tree
				showTree(index);
			}
			else if(curModelType==1){ //Markov model
				showMarkov(index);
			}
			curModel.refreshParamTable();
			curModel.refreshVarTable();
			curModel.refreshTableTable();
			curModel.refreshConstTable();
			curModel.refreshParamSetsTable();
			curModel.refreshUndoButtons();
			curModel.refreshAlignment();

			btnAddVariable.setEnabled(true);
		}
		else{ //no tabs
			curModel=null;
			scrollPaneFx.setViewportView(null);
			scrollPaneNotes.setViewportView(null);
			frmMain.setTitle("Amua");
			modelParameters.setRowCount(0);
			modelVariables.setRowCount(0);
			modelTables.setRowCount(0);
			modelConstraints.setRowCount(0);
			modelParamSets.setRowCount(0);

			btnAddVariable.setEnabled(false);
		}
		console.switchModel(curModel);
	}

	private void showTree(int index){
		PanelTree curPanelTree=curModel.panelTree;
		scrollPaneFx.setViewportView(curPanelTree.paneFormula);
		curFxPane=curPanelTree.paneFormula;
		scrollPaneNotes.setViewportView(curPanelTree.textAreaNotes);
		scrollPaneProperties.setViewportView(null);

		frmMain.setTitle("Amua - "+curModel.name);
		if(prevModelType==1){ //switch model type
			toolBar.remove(btnMarkovChain);
			toolBar.remove(btnMarkovState);
			toolBar.remove(btnStateTransition);
			toolBar.add(btnTerminalNode,2);
			btnChangeNodeType.setIcon(new ImageIcon(frmMain.class.getResource("/images/changeType.png"),"Change Node Type"));
			toolBar.revalidate();

			tabbedPaneBottom.removeTabAt(3); //properties
		}

		curPanelTree.rescalePanel=false; //Turn off re-scaling
		setZoomVal(curModel.scale);
		curPanelTree.rescalePanel=true;

		prevModelType=0;
	}

	private void showMarkov(int index){
		PanelMarkov curPanelMarkov=curModel.panelMarkov;
		scrollPaneFx.setViewportView(curPanelMarkov.paneFormula);
		curFxPane=curPanelMarkov.paneFormula;
		scrollPaneNotes.setViewportView(curPanelMarkov.textAreaNotes);
		scrollPaneProperties.setViewportView(curPanelMarkov.tableProperties);

		frmMain.setTitle("Amua - "+curModel.name);
		if(prevModelType==0){ //switch model type
			toolBar.add(btnMarkovChain,1);
			toolBar.add(btnMarkovState,2);
			toolBar.remove(btnTerminalNode);
			toolBar.add(btnStateTransition,4);
			btnChangeNodeType.setIcon(new ImageIcon(frmMain.class.getResource("/images/changeTypeTrans.png"),"Change Node Type"));
			toolBar.revalidate();
			mntmCalibrateModel.setEnabled(true);

			JLabel lblProperties=new JLabel("Properties");
			Icon iconProperties=new ImageIcon(frmMain.class.getResource("/images/propertiesMarkov.png"));
			lblProperties.setIcon(iconProperties);
			lblProperties.setHorizontalTextPosition(SwingConstants.RIGHT);
			tabbedPaneBottom.addTab("Properties", (Icon) null, scrollPaneProperties, null);
			tabbedPaneBottom.setTabComponentAt(3, lblProperties);
		}

		curPanelMarkov.rescalePanel=false; //Turn off re-scaling
		setZoomVal(curModel.scale);
		curPanelMarkov.rescalePanel=true;

		prevModelType=1;
	}

	private void closeTab(int index){
		boolean proceed=true;
		if(curModel.unsavedChanges){
			int choice=JOptionPane.showConfirmDialog(frmMain, curModel.name+" has unsaved changes that will be lost.  Do you want to save now?");
			if(choice==JOptionPane.YES_OPTION){saveModel();}
			else if(choice==JOptionPane.CANCEL_OPTION || choice==JOptionPane.CLOSED_OPTION){
				proceed=false;
			}
		}
		if(proceed){
			//close tab
			modelTypes.remove(index);
			modelList.remove(index);
			tabbedPaneCanvas.removeTabAt(index);
			if(tabbedPaneCanvas.getTabCount()>0){ //switch to previous tab if it exists
				tabbedPaneCanvas.setSelectedIndex(0);
			}
			else{
				tabbedPaneCanvas.setSelectedIndex(-1);
			}
			switchTabs();
			checkAnyModels();
		}
	}

	private void checkAnyModels(){
		if(modelList.size()==0){ //no models open
			mntmSave.setEnabled(false);
			mntmSaveAs.setEnabled(false);
			mntmDocument.setEnabled(false);
			mntmExport.setEnabled(false);
			mntmProperties.setEnabled(false);
			mnEdit.setEnabled(false);
			mnRun.setEnabled(false);
			//toolbar non-reciprocal (will be re-enabled on node selection)
			btnChangeNodeType.setEnabled(false); 
			btnShowCost.setEnabled(false); 
			btnUpdateVariable.setEnabled(false); 
			//toolbar reciprocal
			btnOCD.setEnabled(false);
			btnAlignLeft.setEnabled(false);
			btnAlignRight.setEnabled(false);
			btnCheckModel.setEnabled(false);
			btnRunModel.setEnabled(false);
			btnClearAnnotations.setEnabled(false);
			sliderZoom.setEnabled(false);
			btnZoomOut.setEnabled(false);
			btnZoomIn.setEnabled(false);
			btnSnapshot.setEnabled(false);
		}
		else{
			mntmSave.setEnabled(true);
			mntmSaveAs.setEnabled(true);
			mntmDocument.setEnabled(true);
			mntmExport.setEnabled(true);
			mntmProperties.setEnabled(true);
			mnEdit.setEnabled(true);
			mnRun.setEnabled(true);
			//toolbar
			btnOCD.setEnabled(true);
			btnAlignLeft.setEnabled(true);
			btnAlignRight.setEnabled(true);
			btnCheckModel.setEnabled(true);
			btnRunModel.setEnabled(true);
			btnClearAnnotations.setEnabled(true);
			sliderZoom.setEnabled(true);
			btnZoomOut.setEnabled(true);
			btnZoomIn.setEnabled(true);
			btnSnapshot.setEnabled(true);
		}
	}
	
	public void setZoomVal(int scale){
		//Calculate slider value
		int val=240;
		if(scale<100){val=(int)Math.round((scale-20)*3);}
		else{val=(int)Math.round((scale+300)*(3/5.0));}
		sliderZoom.setValue(val);
	}

	public void saveModel(){
		try {
			if(curModel.filepath==null){
				fc.resetChoosableFileFilters();
				fc.addChoosableFileFilter(new AmuaModelFilter());
				fc.setAcceptAllFileFilterUsed(false);
				fc.setDialogTitle("Save Model");
				fc.setApproveButtonText("Save");

				int returnVal = fc.showSaveDialog(frmMain);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String name=file.getName();
					curModel.name=name.replaceAll(".amua", "");
					String path=file.getAbsolutePath();
					path=path.replaceAll(".amua", "");
					curModel.filepath=path+".amua";
					curModel.saveModel();
					frmMain.setTitle("Amua - "+curModel.name);
				}
			}
			else{
				curModel.saveModel();
			}
			setTabName(curModel.name,curModel.type);
		}catch(Exception e1){
			e1.printStackTrace();
			errorLog.recordError(e1);
		}
	}
	
	public void openModel(File file){
		try{
			String name=file.getName();
			String filepath=file.getAbsolutePath();

			//Check if model is already open
			boolean isOpen=false;
			int numModels=modelList.size();
			int m=0;
			while(isOpen==false && m<numModels){
				String openPath=modelList.get(m).filepath;
				if(openPath!=null && openPath.equals(filepath)){ //Open, go to tab
					isOpen=true;
					JOptionPane.showMessageDialog(frmMain, filepath+" is already open!");
					tabbedPaneCanvas.setSelectedIndex(m);
					switchTabs();
				}
				else{m++;}
			}
						
			//Not open, open it
			if(isOpen==false){
				frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				
				JAXBContext context = JAXBContext.newInstance(AmuaModel.class);
				Unmarshaller un = context.createUnmarshaller();
				AmuaModel newModel = (AmuaModel) un.unmarshal(new File(filepath));
				curModel=newModel;
				newModel.openModel(main,errorLog);
				newModel.name=name.replaceAll(".amua", "");
				newModel.filepath=filepath;
				int modelType=newModel.type;

				modelList.add(newModel);
				JScrollPane scrollPane=new JScrollPane();
				scrollPane.setViewportView(newModel.getPanel());
				modelTypes.add(modelType);
				//add tab
				addTab(modelType,scrollPane,newModel.name);
				tabbedPaneCanvas.setSelectedIndex(tabbedPaneCanvas.getTabCount()-1);
				switchTabs();

				recentFiles.updateList(filepath,newModel.type);

				checkAnyModels();
				
				frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}catch(Exception e){
			frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			console.print("Error: "+e.getMessage()); console.newLine();
			errorLog.recordError(e);
		}
	}

	private void runModel(){
		if(curModel.checkModel(console,true)){
			curModel.runModel(console,true);
		}
	}

	private void exit(){
		boolean proceed=true;
		int numPanels=modelList.size();
		for(int i=0; i<numPanels; i++){
			tabbedPaneCanvas.setSelectedIndex(i);
			switchTabs();
			boolean savePrompt=false;
			String name="";
			if(curModel.unsavedChanges){
				savePrompt=true;
				name=curModel.name;
			}

			if(savePrompt==true){
				int choice=JOptionPane.showConfirmDialog(frmMain, name+" has unsaved changes that will be lost.  Do you want to save now?");
				if(choice==JOptionPane.YES_OPTION){saveModel();}
				else if(choice==JOptionPane.CANCEL_OPTION || choice==JOptionPane.CLOSED_OPTION){
					proceed=false;
					i=numPanels; //end loop
				}
			}
		}

		if(proceed){
			System.exit(0);
		}

	}

	class ImageTransferable implements Transferable
	{
		/**
		 * Constructs the selection.
		 * @param image an image
		 */
		public ImageTransferable(Image image){theImage = image;}
		public DataFlavor[] getTransferDataFlavors(){return new DataFlavor[] { DataFlavor.imageFlavor };}
		public boolean isDataFlavorSupported(DataFlavor flavor){return flavor.equals(DataFlavor.imageFlavor);}
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException{
			if (flavor.equals(DataFlavor.imageFlavor)){return theImage;}
			else{throw new UnsupportedFlavorException(flavor);}
		}
		private Image theImage;
	}

}
