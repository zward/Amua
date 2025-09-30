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

import javax.swing.JFrame;
import java.awt.Dialog.ModalityType;

import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextArea;

import lang.Language;
import main.ErrorLog;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;

/**
 *
 */
public class frmErrorLog {
	
	public JDialog frmErrorLog;
	String version;
	JTextArea txtLog;
	ErrorLog log;
	Language language;
	
	/**
	 *  Default Constructor
	 */
	public frmErrorLog(ErrorLog log, Language language) {
		this.log=log;
		this.language=language;
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmErrorLog = new JDialog();
			frmErrorLog.setIconImage(Toolkit.getDefaultToolkit().getImage(frmErrorLog.class.getResource("/images/errorLog_128.png")));
			frmErrorLog.setModalityType(ModalityType.APPLICATION_MODAL);
			frmErrorLog.setTitle("Amua - "+language.base.getString("menu.error_log")); //Error Log
			frmErrorLog.setResizable(false);
			frmErrorLog.setBounds(100, 100, 800, 600);
			frmErrorLog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmErrorLog.getContentPane().setLayout(null);
									
			JButton btnOk = new JButton(language.base.getString("button.ok")); //OK
			btnOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmErrorLog.dispose();
				}
			});
			btnOk.setBounds(698, 531, 90, 28);
			frmErrorLog.getContentPane().add(btnOk);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(6, 6, 782, 513);
			frmErrorLog.getContentPane().add(scrollPane_1);
			
			txtLog = new JTextArea();
			txtLog.setWrapStyleWord(true);
			txtLog.setLineWrap(true);
			txtLog.setEditable(false);
			scrollPane_1.setViewportView(txtLog);
			
			//Display log
			txtLog.append(language.message.getString("info.version")+": "+log.version+"\n\n"); //Version
			int numErrors=log.errors.size();
			txtLog.append(language.message.getString("errors")+" ("+numErrors+"):\n"); //Errors
			for(int i=0; i<numErrors; i++){
				txtLog.append(log.errors.get(i)+"\n");
			}
			txtLog.append("\n\n");
			txtLog.append("---------------------------------------------------------------------------------------------------\n");
			txtLog.append(language.base.getString("system.system_properties")+"\n"); //System properties
			for(int i=0; i<log.systemInfo.length; i++){
				txtLog.append(log.systemInfo[i][0]+": "+log.systemInfo[i][1]+"\n");
			}
			txtLog.append(language.base.getString("system.screen_resolution")+": "+Toolkit.getDefaultToolkit().getScreenResolution()+"\n"); //Screen Resolution
			txtLog.append(language.base.getString("system.screen_size")+": "+Toolkit.getDefaultToolkit().getScreenSize().toString()+"\n"); //Screen Size
			txtLog.append("Locale: "+language.locale.toString());
			
			txtLog.setCaretPosition(0);
			
			
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
}
