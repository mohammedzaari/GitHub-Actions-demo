package org.mql.automation_tests.controllers;

import java.util.Map;

import org.mql.automation_tests.models.exceptions.EmailAlreadyUsedException;
import org.mql.automation_tests.models.exceptions.InvalidCredentialsException;
import org.mql.automation_tests.models.exceptions.InvalidDataException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AccountExceptionHandler {

	 @ExceptionHandler(InvalidDataException.class)
	    ResponseEntity<Map<String, String>> invalide(RuntimeException e) {
	        return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
	    }
	    @ExceptionHandler(EmailAlreadyUsedException.class)
	    ResponseEntity<Map<String, String>> conflit(RuntimeException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erreur", e.getMessage()));
	    }
	    @ExceptionHandler(InvalidCredentialsException.class)
	    ResponseEntity<Map<String, String>> noAutorize(RuntimeException e) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erreur", e.getMessage()));
	    }
}
