package lang;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class Language {
	
	public Locale locale;
	public ResourceBundle base;
	public ResourceBundle message;
	public ResourceBundle analysis;
	public ResourceBundle math;
	public ResourceBundle dist;
	
	public Font font;
	public Font fontCode;
	
	boolean warnVersion;
	
	/**
	 * Constructor
	 */
	public Language(){
		locale = Locale.getDefault();
		base = ResourceBundle.getBundle("lang.base", locale);
		message = ResourceBundle.getBundle("lang.message", locale);
		analysis = ResourceBundle.getBundle("lang.analysis", locale);
		math = ResourceBundle.getBundle("lang.math", locale);
		dist = ResourceBundle.getBundle("lang.dist", locale);
		warnVersion=true;
		
		//get font
		font=new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12); //default font
		fontCode=new java.awt.Font("Consolas", java.awt.Font.PLAIN, 12); //default font
		if(locale.getLanguage().equals("km")) { //Khmer
			font=getKhmerFont();
			fontCode=getKhmerFont();
		}
		else if(locale.getLanguage().equals("zh")) { //Chinese
			font=getChineseFont();
			fontCode=getChineseFont();
		}
	}
	
	public void setLocale(Locale newLocale) {
		locale = newLocale;
		Locale.setDefault(newLocale);
		base = ResourceBundle.getBundle("lang.base", locale);
		message = ResourceBundle.getBundle("lang.message", locale);
		analysis = ResourceBundle.getBundle("lang.analysis", locale);
		math = ResourceBundle.getBundle("lang.math", locale);
		dist = ResourceBundle.getBundle("lang.dist", locale);
		
		//get font
		font=new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12); //default font
		fontCode=new java.awt.Font("Consolas", java.awt.Font.PLAIN, 12); //default font
		if(locale.getLanguage().equals("km")) { //Khmer
			font=getKhmerFont();
			fontCode=getKhmerFont();
		}
	}
	
	public Font getKhmerFont()  {
		try {
			boolean ok=checkJavaVersion();
			
			if(ok) {
				// Load from classpath
				InputStream is = Language.class.getResourceAsStream("/fonts/NotoSansKhmer-Regular.ttf");
				if (is == null) {
					throw new IOException("NotoSansKhmer-Regular.ttf not found on classpath");
				}
				Font curFont = Font.createFont(Font.TRUETYPE_FONT, is);
				is.close();

				// Register with graphics environment so it's available system-wide
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				ge.registerFont(curFont);

				return curFont.deriveFont(12f);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return(font);
		
    }
	
	public Font getChineseFont()  {
		try {
			boolean ok=checkJavaVersion();
			
			if(ok) {
				// Load from classpath
				InputStream is = Language.class.getResourceAsStream("/fonts/NotoSansSC-Regular.ttf");
				if (is == null) {
					throw new IOException("NotoSansSC-Regular.ttf not found on classpath");
				}
				Font curFont = Font.createFont(Font.TRUETYPE_FONT, is);
				is.close();

				// Register with graphics environment so it's available system-wide
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				ge.registerFont(curFont);

				return curFont.deriveFont(12f);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return(font);
		
    }
	
	private boolean checkJavaVersion() {
		boolean ok=true;
		if(warnVersion==true) {
			warnVersion=false; //only check once
			String version = System.getProperty("java.version");
			
			int major;
			if (version.startsWith("1.")) { // e.g., "1.8.0_361" -> 8
				major = Integer.parseInt(version.substring(2, 3));
			} else { // e.g., "11.0.18" or "21"
				int dot = version.indexOf('.');
				major = (dot > 0) ? Integer.parseInt(version.substring(0, dot))
						: Integer.parseInt(version);
			}

			if (major < 11) {
				ok=false;
				JOptionPane.showMessageDialog(
						null,
						"This language font requires Java 11 or later.\n" +
								"Your current version is " + version + ".\n" +
								"Please install a newer Java runtime.",
								"Unsupported Java Version",
								JOptionPane.ERROR_MESSAGE
						);
			}
		}
		return(ok);
	}
	
	public void setFontRecursively(Component c) {
	    c.setFont(font);
	    if (c instanceof Container) {
	        for (Component child : ((Container) c).getComponents()) {
	            setFontRecursively(child);
	        }
	    }
	}
	
	public void installMenuFontUpdater(JPopupMenu popup) {
	    popup.addPopupMenuListener(new PopupMenuListener() {
	        @Override
	        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	            setFontRecursively(popup);
	            // Also attach to any submenus we discover
	            for (Component c : popup.getComponents()) {
	                if (c instanceof JMenu) {
	                	JMenu menu = (JMenu) c;
	                    JPopupMenu sub = menu.getPopupMenu();
	                    installMenuFontUpdater(sub);
	                }
	            }
	        }
	        @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
	        @Override public void popupMenuCanceled(PopupMenuEvent e) {}
	    });
	}
	
	public void setChartPropertiesFont(JPopupMenu popup, int index) {
		JMenuItem prop=(JMenuItem) popup.getComponent(index);
		prop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
			        @Override
			        public void run() {
			            for (Window w : Window.getWindows()) {
			                if (w instanceof JDialog) {
			                    JDialog dlg = (JDialog) w;
			                    setFontRecursively(dlg.getContentPane());
			                    dlg.pack();
			                }
			            }
			        }
			    });
				
			}
		});
	}
}