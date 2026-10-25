package org.mql.automation_tests.models.exceptions;

public class EmailAlreadyUsedException extends RuntimeException{
	public EmailAlreadyUsedException() {
		super("Email déjà utilisé");
	}
}
