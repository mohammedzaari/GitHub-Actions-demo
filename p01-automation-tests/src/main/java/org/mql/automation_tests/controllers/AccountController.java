package org.mql.automation_tests.controllers;

import org.mql.automation_tests.models.AccountRequest;
import org.mql.automation_tests.models.AccountResponse;
import org.mql.automation_tests.models.services.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AccountController {
	private final AccountService accountService;
    public AccountController(AccountService service) { this.accountService = service; }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@RequestBody AccountRequest request) {
        return new AccountResponse(accountService.createAccount(request.email(), request.hashedPassword()).email());
    }
    @PostMapping("/connexion")
    public AccountResponse connecte(@RequestBody AccountRequest request) {
        return new AccountResponse(accountService.connecte(request.email(), request.hashedPassword()).email());
    }
	
}
