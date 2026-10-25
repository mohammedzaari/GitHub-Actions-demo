package org.mql.automation_tests.models.exceptions;

public class InvalidCredentialsException extends RuntimeException {
	public InvalidCredentialsException() {
		super("Identifiants incorrects");
	}	
}
