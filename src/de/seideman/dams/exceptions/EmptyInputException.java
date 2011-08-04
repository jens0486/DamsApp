package de.seideman.dams.exceptions;

public class EmptyInputException extends Exception {
	
	public EmptyInputException(){
		
	}
	
	public EmptyInputException(String _message){
		super(_message);
	}
	
}
