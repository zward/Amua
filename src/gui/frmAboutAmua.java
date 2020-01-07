/**
 * Amua - An open source modeling framework.
 * Copyright (C) 2017-2020 Zachary J. Ward
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

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextArea;

import main.ErrorLog;
import main.ScaledIcon;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import java.awt.Font;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.awt.Toolkit;

/**
 *
 */
public class frmAboutAmua {
	
	public JDialog frmAboutAmua;
	String version;
	ErrorLog errorLog;
	JTextArea txtrAmuaIsFree;
	
	/**
	 *  Default Constructor
	 */
	public frmAboutAmua(String version, ErrorLog errorLog) {
		this.version=version;
		this.errorLog=errorLog;
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmAboutAmua = new JDialog();
			frmAboutAmua.setIconImage(Toolkit.getDefaultToolkit().getImage(frmAboutAmua.class.getResource("/images/logo_128.png")));
			frmAboutAmua.setModalityType(ModalityType.APPLICATION_MODAL);
			frmAboutAmua.setTitle("About Amua");
			frmAboutAmua.setResizable(false);
			frmAboutAmua.setBounds(100, 100, 500, 400);
			frmAboutAmua.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmAboutAmua.getContentPane().setLayout(null);
									
			JButton btnOk = new JButton("OK");
			btnOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmAboutAmua.dispose();
				}
			});
			btnOk.setBounds(392, 334, 90, 28);
			frmAboutAmua.getContentPane().add(btnOk);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(6, 144, 476, 178);
			frmAboutAmua.getContentPane().add(scrollPane_1);
			
			txtrAmuaIsFree = new JTextArea();
			txtrAmuaIsFree.setWrapStyleWord(true);
			txtrAmuaIsFree.setText("Amua is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.\r\n\r\nAmua is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.\r\n\r\nYou should have received a copy of the GNU General Public License along with Amua.  If not, see <http://www.gnu.org/licenses/>.");
			txtrAmuaIsFree.setLineWrap(true);
			txtrAmuaIsFree.setEditable(false);
			scrollPane_1.setViewportView(txtrAmuaIsFree);
			
			JLabel lblLogo = new JLabel("New label");
			lblLogo.setIcon(new ScaledIcon("/images/logo",48,48,48,true));
			lblLogo.setBounds(84, 13, 48, 48);
			frmAboutAmua.getContentPane().add(lblLogo);
			
			JLabel lblAmua = new JLabel("Amua");
			lblAmua.setFont(new Font("SansSerif", Font.BOLD, 28));
			lblAmua.setBounds(162, 10, 132, 42);
			frmAboutAmua.getContentPane().add(lblAmua);
			
			JLabel lblVersion = new JLabel("Version "+version);
			lblVersion.setFont(new Font("SansSerif", Font.PLAIN, 14));
			lblVersion.setBounds(162, 54, 182, 16);
			frmAboutAmua.getContentPane().add(lblVersion);
			
			JLabel lblURL = new JLabel("<HTML><FONT color=\"#000099\"><U>https://github.com/zward/Amua</U></FONT></HTML>");
			lblURL.setFont(new Font("SansSerif", Font.PLAIN, 14));
			lblURL.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						Desktop.getDesktop().browse(new URI("https://github.com/zward/Amua"));
					} catch (Exception e1) {
						e1.printStackTrace();
						errorLog.recordError(e1);
					}
				}
			});
			lblURL.setBounds(162, 76, 203, 20);
			lblURL.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
			frmAboutAmua.getContentPane().add(lblURL);
			
			JLabel lblGnuGeneralPublic = new JLabel("GNU General Public License:");
			lblGnuGeneralPublic.setBounds(6, 126, 182, 16);
			frmAboutAmua.getContentPane().add(lblGnuGeneralPublic);
			
			JLabel lblNewLabel = new JLabel("\u00A9 2017-2020 Zachary J. Ward");
			lblNewLabel.setBounds(162, 98, 182, 16);
			frmAboutAmua.getContentPane().add(lblNewLabel);
			
			
		} catch (Exception ex){
			ex.printStackTrace();
			errorLog.recordError(ex);
		}
	}
}
