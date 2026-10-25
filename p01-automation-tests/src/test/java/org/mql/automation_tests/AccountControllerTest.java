package org.mql.automation_tests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;


import org.junit.jupiter.api.Test;
import org.mql.automation_tests.controllers.AccountController;
import org.mql.automation_tests.models.User;
import org.mql.automation_tests.models.exceptions.EmailAlreadyUsedException;
import org.mql.automation_tests.models.exceptions.InvalidCredentialsException;
import org.mql.automation_tests.models.exceptions.InvalidDataException;
import org.mql.automation_tests.models.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.InvalidCriteriaException;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
public class AccountControllerTest {



    @Autowired MockMvc mvc;
    @MockitoBean AccountService accountService;

    
    private static final String BODY = """
        {"email": "alice@test.com", "hashedPassword": "Secret123"}
        """;

    @Test
    void creerCompte_retourne201() throws Exception {
        when(accountService.createAccount("alice@test.com", "Secret123"))
                .thenReturn(new User("alice@test.com", "hash"));

        mvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content(BODY))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.email").value("alice@test.com"))
           .andExpect(jsonPath("$.hashedPassword").doesNotExist()); // jamais renvoyé
    }

    @Test
    void creerCompte_emailExistant_retourne409() throws Exception {
        when(accountService.createAccount(any(), any())).thenThrow(new EmailAlreadyUsedException());

        mvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content(BODY))
           .andExpect(status().isConflict())
           .andExpect(jsonPath("$.erreur").value("Email déjà utilisé"));
    }

    @Test
    void creerCompte_donneesInvalides_retourne400() throws Exception {
        when(accountService.createAccount(any(), any()))
                .thenThrow(new InvalidDataException("Email invalide"));

        mvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content(BODY))
           .andExpect(status().isBadRequest());
    }

    @Test
    void connexion_valide_retourne200() throws Exception {
        when(accountService.connecte("alice@test.com", "Secret123"))
                .thenReturn(new User("alice@test.com", "hash"));

        mvc.perform(post("/api/connexion").contentType(MediaType.APPLICATION_JSON).content(BODY))
           .andExpect(status().isOk());
    }

    @Test
    void connexion_identifiantsIncorrects_retourne401() throws Exception {
        when(accountService.connecte(any(), any())).thenThrow(new InvalidCredentialsException());

        mvc.perform(post("/api/connexion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BODY))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.erreur").value("Identifiants incorrects"));
    }
}