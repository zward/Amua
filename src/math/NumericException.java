package math;
public class NumericException extends Exception{
	String message;

	public NumericException(String message,String function) {
		this.message="Error in "+function+": "+message;
	}
	public String toString(){ 
		return(message);
	}
	public String getMessage(){
		return(message);
	}
}