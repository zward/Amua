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

package main;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.EventQueue;
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
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import filters.GIFFilter;
import filters.JPEGFilter;
import filters.PNGFilter;
import filters.TREXFilter;
import filters.TreeFilter;
import java.awt.event.InputEvent;
import javax.swing.Icon;

public class frmMain {

	private JFrame frmMain;
	PanelTree panelTree;
	JTabbedPane tabbedPaneBottom;
	JTextArea console;
	JFileChooser fc=new JFileChooser();
	String version="0.0.1";
	//Main menu items to be accessible to panel
	JButton btnDecisionNode, btnChanceNode, btnTerminalNode, btnChangeNodeType, btnShowCost, btnEqualY, btnCollapse;
	JMenuItem mntmUndo, mntmRedo;
	JMenuItem mntmCut, mntmCopy, mntmPaste;
	JSlider sliderZoom;
	DefaultTableModel modelVariables;
	private JTable tableVariables;
	ErrorLog errorLog;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			// If Nimbus is not available, you can set the GUI to another look and feel.
			e.printStackTrace();
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frmMain window = new frmMain();
					window.frmMain.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public frmMain() {
		initialize();
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

		errorLog=new ErrorLog();
		
		fc.addChoosableFileFilter(new TreeFilter());
		fc.setAcceptAllFileFilterUsed(false);
		
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
				boolean proceed=true;
				if(panelTree.unsavedChanges){
					int choice=JOptionPane.showConfirmDialog(frmMain, "There are unsaved changes that will be lost.  Do you want to save now?");
					if(choice==JOptionPane.YES_OPTION){saveModel();}
					else if(choice==JOptionPane.CANCEL_OPTION || choice==JOptionPane.CLOSED_OPTION){proceed=false;}
				}

				if(proceed){
					setZoomVal(100);
					panelTree.clearAll();
					panelTree.tree=new DecisionTree(true);
					panelTree.tree.nodes.get(0).setPanel(panelTree);
					panelTree.treeStackUndo=new Stack<DecisionTree>();
					panelTree.treeStackRedo=new Stack<DecisionTree>();
					panelTree.actionStackUndo=new Stack<String>();
					panelTree.actionStackRedo=new Stack<String>();
					mntmUndo.setEnabled(false);
					mntmUndo.setText("Undo");
					mntmRedo.setEnabled(false);
					mntmRedo.setText("Redo");
					modelVariables.setRowCount(0);
					panelTree.varHelper=new VarHelper(panelTree.tree.variables,panelTree.tree.dimSymbols,errorLog);
					panelTree.paneFormula.varHelper=panelTree.varHelper;
					panelTree.paneFormula.setText("");
					panelTree.paneFormula.setEditable(false);
					panelTree.textAreaNotes.setText("");
					panelTree.textAreaNotes.setEditable(false);
					console.setText("");
					
					panelTree.repaint();
					frmMain.setTitle("Amua");
					panelTree.name=null;
					panelTree.filepath=null;
				}
			}
		});
		mnNew.add(mntmDecisionTree);

		JMenuItem mntmOpenModel = new JMenuItem("Open Model...");
		mntmOpenModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					boolean proceed=true;
					if(panelTree.unsavedChanges){
						int choice=JOptionPane.showConfirmDialog(frmMain, "There are unsaved changes that will be lost.  Do you want to save now?");
						if(choice==JOptionPane.YES_OPTION){saveModel();}
						else if(choice==JOptionPane.CANCEL_OPTION || choice==JOptionPane.CLOSED_OPTION){proceed=false;}
					}

					if(proceed){
						fc.setDialogTitle("Open Model");
						fc.setApproveButtonText("Open");

						int returnVal = fc.showOpenDialog(frmMain);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							panelTree.name=file.getName();
							panelTree.filepath=file.getAbsolutePath();
							panelTree.openTree();
							console.setText("");

							frmMain.setTitle("Amua - "+panelTree.name);
						}
					}
				}catch(Exception e){
					console.append("Error: "+e.getMessage());
					errorLog.recordError(e);
				}
			}
		});
		mnModel.add(mntmOpenModel);

		JSeparator separator_2 = new JSeparator();
		mnModel.add(separator_2);

		JMenuItem mntmSave = new JMenuItem("Save");
		KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		mntmSave.setAccelerator(ctrlS);
		mntmSave.setIcon(new ImageIcon(frmMain.class.getResource("/com/sun/java/swing/plaf/windows/icons/FloppyDrive.gif")));
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveModel();
			}
		});
		mnModel.add(mntmSave);

		JMenuItem mntmSaveAs = new JMenuItem("Save As...");
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					JFileChooser fc=new JFileChooser(panelTree.filepath);
					fc.setAcceptAllFileFilterUsed(false);
					fc.setFileFilter(new TreeFilter());
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
							panelTree.name=file.getName();
							path=path.replaceAll(".amdt", "");
							panelTree.filepath=path+".amdt";
							panelTree.saveTree(console);
							frmMain.setTitle("Amua - "+panelTree.name);
						}
						else{ //Image
							BufferedImage bi = new BufferedImage(panelTree.getWidth(), panelTree.getHeight(), BufferedImage.TYPE_INT_RGB);
							Graphics2D g = bi.createGraphics();
							panelTree.print(g);
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
		
		JMenuItem mntmExport = new JMenuItem("Export...");
		mntmExport.setIcon(new ImageIcon(frmMain.class.getResource("/images/export.png")));
		mntmExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmExport window=new frmExport(panelTree);
				window.frmExport.setVisible(true);
			}
		});
		
		JSeparator separator_6 = new JSeparator();
		mnModel.add(separator_6);
		
		JMenuItem mntmImport = new JMenuItem("Import...");
		mntmImport.setIcon(new ImageIcon(frmMain.class.getResource("/images/import.png")));
		mntmImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc=new JFileChooser();
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(new TREXFilter());
				fc.setDialogTitle("Import Model");
				fc.setApproveButtonText("Import");

				int returnVal = fc.showOpenDialog(frmMain);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					JProgressBar progress=new JProgressBar();
					progress.setString("Importing...");
					progress.setVisible(true);
					progress.setIndeterminate(true);
					File file = fc.getSelectedFile();
					String path=file.getAbsolutePath();
					setZoomVal(100);
					modelVariables.setRowCount(0);
					panelTree.setVisible(false);
					new ImportTree(panelTree,path);
					frmMain.setTitle("Amua");
					panelTree.name=null;
					panelTree.filepath=null;
					panelTree.rescale(panelTree.tree.scale);
					panelTree.ocd("OCD");
					panelTree.setVisible(true);
					panelTree.treeStackUndo=new Stack<DecisionTree>();
					panelTree.treeStackRedo=new Stack<DecisionTree>();
					panelTree.actionStackUndo=new Stack<String>();
					panelTree.actionStackRedo=new Stack<String>();
					panelTree.paneFormula.setText("");
					panelTree.paneFormula.setEditable(false);
					panelTree.textAreaNotes.setText("");
					panelTree.textAreaNotes.setEditable(false);
					console.setText("");
					mntmUndo.setEnabled(false);
					mntmUndo.setText("Undo");
					mntmRedo.setEnabled(false);
					mntmRedo.setText("Redo");
					progress.setVisible(false);
					JOptionPane.showMessageDialog(panelTree, "Imported!");
				}
			}
		});
		mnModel.add(mntmImport);
		mnModel.add(mntmExport);
		
		JSeparator separator_5 = new JSeparator();
		mnModel.add(separator_5);
		
		JMenuItem mntmProperties = new JMenuItem("Properties");
		mntmProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmProperties window=new frmProperties();
				window.setModel(panelTree);
				window.frmProperties.setVisible(true);
			}
		});
		mnModel.add(mntmProperties);
		
		JSeparator separator_7 = new JSeparator();
		mnModel.add(separator_7);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		mnModel.add(mntmExit);

		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);

		mntmUndo = new JMenuItem("Undo");
		mntmUndo.setIcon(new ImageIcon(frmMain.class.getResource("/com/sun/javafx/scene/web/skin/Undo_16x16_JFX.png")));
		mntmUndo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelTree.undoAction();
			}
		});
		mntmUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmUndo.setEnabled(false);
		mnEdit.add(mntmUndo);

		mntmRedo = new JMenuItem("Redo");
		mntmRedo.setIcon(new ImageIcon(frmMain.class.getResource("/com/sun/javafx/scene/web/skin/Redo_16x16_JFX.png")));
		mntmRedo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelTree.redoAction();
			}
		});
		mntmRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmRedo.setEnabled(false);
		mnEdit.add(mntmRedo);

		JSeparator separator_3 = new JSeparator();
		mnEdit.add(separator_3);

		mntmCut = new JMenuItem("Cut");
		mntmCut.setIcon(new ImageIcon(frmMain.class.getResource("/com/sun/javafx/scene/control/skin/modena/HTMLEditor-Cut.png")));
		mntmCut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelTree.cutSubtree();
			}
		});
		mntmCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmCut.setEnabled(false);
		mnEdit.add(mntmCut);

		mntmCopy = new JMenuItem("Copy");
		mntmCopy.setIcon(new ImageIcon(frmMain.class.getResource("/com/sun/javafx/scene/web/skin/Copy_16x16_JFX.png")));
		mntmCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelTree.copySubtree();
			}
		});
		mntmCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmCopy.setEnabled(false);
		mnEdit.add(mntmCopy);

		mntmPaste = new JMenuItem("Paste");
		mntmPaste.setIcon(new ImageIcon(frmMain.class.getResource("/com/sun/javafx/scene/web/skin/Paste_16x16_JFX.png")));
		mntmPaste.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelTree.pasteSubtree();
			}
		});
		mntmPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmPaste.setEnabled(false);
		mnEdit.add(mntmPaste);
		
		JMenuItem mntmFindreplace = new JMenuItem("Find/Replace");
		mntmFindreplace.setIcon(new ImageIcon(frmMain.class.getResource("/images/find.png")));
		mntmFindreplace.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmFindReplace window=new frmFindReplace(panelTree);
				window.frmFindReplace.setVisible(true);
			}
		});
		
		JSeparator separator_9 = new JSeparator();
		mnEdit.add(separator_9);
		mntmFindreplace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
		mnEdit.add(mntmFindreplace);
		
		JMenu mnRun = new JMenu("Run");
		menuBar.add(mnRun);
		
		JMenuItem mntmRunTree = new JMenuItem("Run Tree");
		mntmRunTree.setIcon(new ImageIcon(frmMain.class.getResource("/images/runTree_16.png")));
		mntmRunTree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(panelTree.checkTree(console,true)){
					console.setText("Running tree... ");
					panelTree.tree.showEV=true;
					panelTree.tree.runTree(panelTree.tree.nodes.get(0),true); //Send root
					console.append("done!\n");
					//Display output on console
					panelTree.tree.displayResults(console);
					
				}
			}
		});
		mnRun.add(mntmRunTree);
		
		JSeparator separator_10 = new JSeparator();
		mnRun.add(separator_10);
		
		JMenu mnSensitivityAnalysis = new JMenu("Sensitivity Analysis");
		mnRun.add(mnSensitivityAnalysis);
		
		JMenuItem mntmOneway = new JMenuItem("One-way");
		mntmOneway.setIcon(new ImageIcon(frmMain.class.getResource("/images/oneWay.png")));
		mntmOneway.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmSensOneWay window=new frmSensOneWay(panelTree);
				window.frmSensOneWay.setVisible(true);
			}
		});
		mnSensitivityAnalysis.add(mntmOneway);
		
		JMenuItem mntmTornadoDiagram = new JMenuItem("Tornado Diagram");
		mntmTornadoDiagram.setIcon(new ImageIcon(frmMain.class.getResource("/images/tornado.png")));
		mntmTornadoDiagram.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmTornadoDiagram window=new frmTornadoDiagram(panelTree);
				window.frmTornadoDiagram.setVisible(true);
			}
		});
		mnSensitivityAnalysis.add(mntmTornadoDiagram);
		
		JMenuItem mntmThresholdAnalysis = new JMenuItem("Threshold Analysis");
		mntmThresholdAnalysis.setIcon(new ImageIcon(frmMain.class.getResource("/images/threshold.png")));
		mntmThresholdAnalysis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmThreshOneWay window=new frmThreshOneWay(panelTree);
				window.frmThreshOneWay.setVisible(true);
			}
		});
		mnSensitivityAnalysis.add(mntmThresholdAnalysis);
		
		JMenuItem mntmProbabilisticpsa = new JMenuItem("Probabilistic (PSA)");
		mntmProbabilisticpsa.setIcon(new ImageIcon(frmMain.class.getResource("/images/psa.png")));
		mntmProbabilisticpsa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmPSA window=new frmPSA(panelTree);
				window.frmPSA.setVisible(true);
			}
		});
		
		JMenuItem mntmTwoway = new JMenuItem("Two-way");
		mntmTwoway.setIcon(new ImageIcon(frmMain.class.getResource("/images/twoWay.png")));
		mntmTwoway.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmSensTwoWay window=new frmSensTwoWay(panelTree);
				window.frmSensTwoWay.setVisible(true);
			}
		});
		mnSensitivityAnalysis.add(mntmTwoway);
		mnSensitivityAnalysis.add(mntmProbabilisticpsa);
		
		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);
		
		JMenuItem mntmPlotFunction = new JMenuItem("Plot Function");
		mntmPlotFunction.setIcon(new ImageIcon(frmMain.class.getResource("/images/plotFx.png")));
		mntmPlotFunction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmPlotFx window=new frmPlotFx(panelTree.varHelper);
				window.frmPlotFx.setVisible(true);
			}
		});
		mnTools.add(mntmPlotFunction);
		
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

		JToolBar toolBar = new JToolBar();
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
		
		JTabbedPane paneProperties=new JTabbedPane(JTabbedPane.TOP);
		JSplitPane splitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,splitPaneLeft,paneProperties);
		
		JPanel panelVars = new JPanel();
		paneProperties.addTab("Variables", null, panelVars, null);
		GridBagLayout gbl_panelVars = new GridBagLayout();
		gbl_panelVars.columnWidths = new int[]{0, 0};
		gbl_panelVars.rowHeights = new int[]{0, 0, 0};
		gbl_panelVars.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelVars.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panelVars.setLayout(gbl_panelVars);
		
		JToolBar toolBar_1 = new JToolBar();
		toolBar_1.setRollover(true);
		toolBar_1.setFloatable(false);
		GridBagConstraints gbc_toolBar_1 = new GridBagConstraints();
		gbc_toolBar_1.insets = new Insets(0, 0, 5, 0);
		gbc_toolBar_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_toolBar_1.gridx = 0;
		gbc_toolBar_1.gridy = 0;
		panelVars.add(toolBar_1, gbc_toolBar_1);
		
		JButton btnAddVariable = new JButton();
		btnAddVariable.setToolTipText("Add Variable");
		URL imageURL = frmMain.class.getResource("/images/add.png");
		btnAddVariable.setIcon(new ImageIcon(imageURL,"Add Variable"));
		btnAddVariable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmDefineVariable window=new frmDefineVariable(panelTree,-1);
				window.frmDefineVariable.setVisible(true);
			}
		});
		toolBar_1.add(btnAddVariable);
		
		JButton btnDeleteVariable = new JButton();
		btnDeleteVariable.setToolTipText("Delete Variable");
		imageURL = frmMain.class.getResource("/images/delete.png");
		btnDeleteVariable.setIcon(new ImageIcon(imageURL,"Delete Variable"));
		btnDeleteVariable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selected=tableVariables.getSelectedRow();
				if(selected!=-1){
					panelTree.deleteVariable(selected);
					panelTree.rescale(panelTree.tree.scale); //Re-validates textfields
				}
			}
		});
		
		JButton btnEdit = new JButton();
		btnEdit.setToolTipText("Edit Variable");
		imageURL = frmMain.class.getResource("/images/edit.png");
		btnEdit.setIcon(new ImageIcon(imageURL,"Edit Variable"));
		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selected=tableVariables.getSelectedRow();
				if(selected!=-1){
					frmDefineVariable window=new frmDefineVariable(panelTree,selected);
					window.frmDefineVariable.setVisible(true);
				}
			}
		});
		toolBar_1.add(btnEdit);
		toolBar_1.add(btnDeleteVariable);
		
		JButton btnShowuse = new JButton();
		btnShowuse.setToolTipText("Highlight Use");
		btnShowuse.setIcon(new ImageIcon(frmMain.class.getResource("/com/sun/javafx/scene/web/skin/FontBackgroundColor_16x16_JFX.png")));
		btnShowuse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selected=tableVariables.getSelectedRow();
				if(selected!=-1){panelTree.highlightVariable(selected);}
			}
		});
		toolBar_1.add(btnShowuse);
		
		JScrollPane scrollPaneVariables = new JScrollPane();
		GridBagConstraints gbc_scrollPaneVariables = new GridBagConstraints();
		gbc_scrollPaneVariables.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneVariables.gridx = 0;
		gbc_scrollPaneVariables.gridy = 1;
		panelVars.add(scrollPaneVariables, gbc_scrollPaneVariables);
		
		modelVariables=new DefaultTableModel(new Object[][] {},	new String[] {"Name", "Expression"})
			{boolean[] columnEditables = new boolean[] {false, false};
				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
			};
					
		tableVariables = new JTable();
		tableVariables.setRowSelectionAllowed(false);
		tableVariables.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==10){ //Enter
					int selected=tableVariables.getSelectedRow();
					if(selected!=-1){
						frmDefineVariable window=new frmDefineVariable(panelTree,selected);
						window.frmDefineVariable.setVisible(true);
					}
				}
			}
		});
		tableVariables.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2){
					int selected=tableVariables.getSelectedRow();
					if(selected!=-1){
						frmDefineVariable window=new frmDefineVariable(panelTree,selected);
						window.frmDefineVariable.setVisible(true);
					}
				}
			}
		});
		tableVariables.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
		    @Override
		    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		        TreeVariable curVar=panelTree.tree.variables.get(row);
		        if(curVar.valid){
		        	setForeground(table.getForeground());
		        }
		        else{
		        	setForeground(Color.RED);
		        }
		        return this;
		    }   
		});
		
		tableVariables.setShowVerticalLines(true);
		tableVariables.setModel(modelVariables);
		scrollPaneVariables.setViewportView(tableVariables);
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.9);
		frmMain.getContentPane().add(splitPane, gbc_splitPaneLeft);
		
	
		JScrollPane scrollPaneCanvas = new JScrollPane();
		splitPaneLeft.setLeftComponent(scrollPaneCanvas);

		//Decision tree

		//New decision tree
		panelTree=new PanelTree(this,errorLog);
		scrollPaneCanvas.setViewportView(panelTree);

		tabbedPaneBottom = new JTabbedPane(JTabbedPane.TOP);
		splitPaneLeft.setRightComponent(tabbedPaneBottom);

		JScrollPane scrollPaneConsole = new JScrollPane();
		JLabel lblConsole=new JLabel("Console");
		Icon iconConsole=new ImageIcon(frmMain.class.getResource("/images/console.png"));
		lblConsole.setIcon(iconConsole);
		lblConsole.setHorizontalTextPosition(SwingConstants.RIGHT);
		tabbedPaneBottom.addTab("Console", null, scrollPaneConsole, null);
		tabbedPaneBottom.setTabComponentAt(0, lblConsole);
		
		console = new JTextArea();
		console.setEditable(false);
		scrollPaneConsole.setViewportView(console);
		
		JScrollPane scrollPaneFormula = new JScrollPane();
		JLabel lblFormula=new JLabel("Formula");
		Icon iconFormula=new ImageIcon(frmMain.class.getResource("/images/formula.png"));
		lblFormula.setIcon(iconFormula);
		lblFormula.setHorizontalTextPosition(SwingConstants.RIGHT);
		tabbedPaneBottom.addTab("Formula", null, scrollPaneFormula, null);
		tabbedPaneBottom.setTabComponentAt(1, lblFormula);
		scrollPaneFormula.setViewportView(panelTree.paneFormula);
		
		JScrollPane scrollPaneNotes = new JScrollPane();
		JLabel lblNotes=new JLabel("Notes");
		Icon iconNotes=new ImageIcon(frmMain.class.getResource("/images/notes.png"));
		lblNotes.setIcon(iconNotes);
		lblNotes.setHorizontalTextPosition(SwingConstants.RIGHT);
		tabbedPaneBottom.addTab("Notes", (Icon) null, scrollPaneNotes, null);
		tabbedPaneBottom.setTabComponentAt(2, lblNotes);
		scrollPaneNotes.setViewportView(panelTree.textAreaNotes);
		
		btnDecisionNode=new JButton();
		btnDecisionNode.setEnabled(false);
		btnDecisionNode.setToolTipText("Decision Node");
		imageURL = frmMain.class.getResource("/images/decisionNode.png");
		btnDecisionNode.setIcon(new ImageIcon(imageURL,"Decision Node"));
		toolBar.add(btnDecisionNode);
		btnDecisionNode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panelTree.addNode(0);
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
				panelTree.addNode(1);
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
				panelTree.addNode(2);
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
				panelTree.changeNodeType();
			}
		});
		
		btnShowCost = new JButton();
		btnShowCost.setEnabled(false);
		btnShowCost.setToolTipText("Add/Remove Cost");
		imageURL = frmMain.class.getResource("/images/cost.png");
		btnShowCost.setIcon(new ImageIcon(imageURL,"Add/Remove Cost"));
		btnShowCost.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelTree.addRemoveCost();
			}
		});
		toolBar.add(btnShowCost);
		

		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator);

		JButton btnOCD = new JButton("OCD");
		btnOCD.setFont(new Font("SansSerif", Font.PLAIN, 12));
		btnOCD.setToolTipText("Arrange Nodes");
		btnOCD.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panelTree.ocd("OCD");
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
				panelTree.equalY();
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
				panelTree.collapseBranch();
			}
		});
		toolBar.add(btnCollapse);
		
		final JButton btnAlignLeft = new JButton();
		final JButton btnAlignRight = new JButton();
		
		btnAlignLeft.setToolTipText("Align Left");
		imageURL = frmMain.class.getResource("/images/alignLeftSelected.png");
		btnAlignLeft.setIcon(new ImageIcon(imageURL,"Align Left"));
		btnAlignLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelTree.tree.alignRight=false;
				panelTree.ocd("Align Left");
				URL imageURL = frmMain.class.getResource("/images/alignLeftSelected.png");
				btnAlignLeft.setIcon(new ImageIcon(imageURL,"Align Left"));
				imageURL = frmMain.class.getResource("/images/alignRight.png");
				btnAlignRight.setIcon(new ImageIcon(imageURL,"Align Right"));
			}
		});
		toolBar.add(btnAlignLeft);
		
		btnAlignRight.setToolTipText("Align Right");
		imageURL = frmMain.class.getResource("/images/alignRight.png");
		btnAlignRight.setIcon(new ImageIcon(imageURL,"Align Right"));
		btnAlignRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelTree.tree.alignRight=true;
				panelTree.ocd("Align Right");
				URL imageURL = frmMain.class.getResource("/images/alignRightSelected.png");
				btnAlignRight.setIcon(new ImageIcon(imageURL,"Align Right"));
				imageURL = frmMain.class.getResource("/images/alignLeft.png");
				btnAlignLeft.setIcon(new ImageIcon(imageURL,"Align Left"));
			}
		});
		toolBar.add(btnAlignRight);

		JSeparator separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator_1);

		JButton btnCheckTree=new JButton();
		btnCheckTree.setToolTipText("Check Tree");
		imageURL = frmMain.class.getResource("/images/checkTree.png");
		btnCheckTree.setIcon(new ImageIcon(imageURL,"Check Tree"));
		toolBar.add(btnCheckTree);
		btnCheckTree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panelTree.checkTree(console,true);
			}
		});

		JButton btnRunTree=new JButton();
		btnRunTree.setToolTipText("Run Tree");
		imageURL = frmMain.class.getResource("/images/runTree.png");
		btnRunTree.setIcon(new ImageIcon(imageURL,"Run Tree"));
		toolBar.add(btnRunTree);
		btnRunTree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(panelTree.checkTree(console,true)){
					console.setText("Running tree... ");
					panelTree.tree.showEV=true;
					panelTree.tree.runTree(panelTree.tree.nodes.get(0),true); //Send root
					console.append("done!\n");
					//Display output on console
					panelTree.tree.displayResults(console);
				}
			}
		});

		JButton btnClearAnnotations=new JButton();
		btnClearAnnotations.setToolTipText("Clear");
		imageURL = frmMain.class.getResource("/images/clearEV.png");
		btnClearAnnotations.setIcon(new ImageIcon(imageURL,"Clear"));
		toolBar.add(btnClearAnnotations);
		btnClearAnnotations.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panelTree.clearAnnotations();
				console.setText("");
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
				panelTree.rescale(zoom);
			}
		});
		
		JButton btnZoomOut = new JButton(" \u2013 ");
		btnZoomOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Zoom out, increments of 10%
				int curScale=panelTree.tree.scale;
				//Round up to nearest 10%
				int rem=curScale%10;
				if(rem!=0){curScale+=(10-rem);}
				int newScale=Math.max(curScale-10,20); //Floor of 20
				setZoomVal(newScale);
			}
		});
		btnZoomOut.setToolTipText("Zoom Out");
		btnZoomOut.setFont(new Font("Tahoma", Font.PLAIN, 16));
		toolBar.add(btnZoomOut);
		sliderZoom.setToolTipText("Zoom");
		toolBar.add(sliderZoom);
		
		JButton btnZoomIn = new JButton(" + ");
		btnZoomIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Zoom in, increments of 10%
				int curScale=panelTree.tree.scale;
				//Round down to nearest 10%
				int rem=curScale%10;
				curScale-=rem;
				int newScale=Math.min(curScale+10,500); //Ceiling of 500
				setZoomVal(newScale);
			}
		});
		btnZoomIn.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnZoomIn.setToolTipText("Zoom In");
		toolBar.add(btnZoomIn);
		
		
		toolBar.add(lblZoom);
		
		JSeparator separator_8 = new JSeparator();
		separator_8.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator_8);
		
		JButton btnSnapshot=new JButton();
		btnSnapshot.setToolTipText("Screenshot");
		btnSnapshot.setIcon(new ImageIcon(imageURL,"Screenshot"));
		toolBar.add(btnSnapshot);
		
		btnSnapshot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				BufferedImage bi = new BufferedImage(panelTree.getWidth(), panelTree.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D g = bi.createGraphics();
				panelTree.print(g);
			    
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			    ImageTransferable selection = new ImageTransferable(bi);
			    clipboard.setContents(selection, null);
			}
		});
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
			if(panelTree.filepath==null){
				fc.setDialogTitle("Save Model");
				fc.setApproveButtonText("Save");

				int returnVal = fc.showSaveDialog(frmMain);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					panelTree.name=file.getName();
					String path=file.getAbsolutePath();
					path=path.replaceAll(".amdt", "");
					panelTree.filepath=path+".amdt";
					panelTree.saveTree(console);
					frmMain.setTitle("Amua - "+panelTree.name);
				}
			}
			else{
				panelTree.saveTree(console);
			}

		}catch(Exception e1){
			e1.printStackTrace();
			errorLog.recordError(e1);
		}
	}
	
	public void addVariable(TreeVariable var){
		modelVariables.addRow(new Object[] {var.name,var.expression});
	}
	
	private void exit(){
		boolean proceed=true;
		if(panelTree.unsavedChanges){
			int choice=JOptionPane.showConfirmDialog(frmMain, "There are unsaved changes that will be lost.  Do you want to save now?");
			if(choice==JOptionPane.YES_OPTION){saveModel();}
			else if(choice==JOptionPane.CANCEL_OPTION || choice==JOptionPane.CLOSED_OPTION){proceed=false;}
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
