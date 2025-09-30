package lang;

import java.util.Locale;
import java.util.ResourceBundle;

public class Language {
	
	public Locale locale;
	public ResourceBundle base;
	public ResourceBundle message;
	public ResourceBundle analysis;
	public ResourceBundle math;
	public ResourceBundle dist;
	
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
	}
	
	public void setLocale(Locale newLocale) {
		locale = newLocale;
		Locale.setDefault(newLocale);
		base = ResourceBundle.getBundle("lang.base", locale);
		message = ResourceBundle.getBundle("lang.message", locale);
		analysis = ResourceBundle.getBundle("lang.analysis", locale);
		math = ResourceBundle.getBundle("lang.math", locale);
		dist = ResourceBundle.getBundle("lang.dist", locale);
	}
	
	
}