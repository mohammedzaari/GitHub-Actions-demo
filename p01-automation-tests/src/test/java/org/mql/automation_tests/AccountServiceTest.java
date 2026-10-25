package org.mql.automation_tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mql.automation_tests.models.User;
import org.mql.automation_tests.models.exceptions.EmailAlreadyUsedException;
import org.mql.automation_tests.models.exceptions.InvalidCredentialsException;
import org.mql.automation_tests.models.exceptions.InvalidDataException;
import org.mql.automation_tests.models.repositories.UserRepository;
import org.mql.automation_tests.models.services.AccountService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {


    @Mock UserRepository userRepository;
    PasswordEncoder encoder = new BCryptPasswordEncoder(4); // coût réduit : tests rapides
    AccountService accountService;

    @BeforeEach
    void init() { accountService = new AccountService(userRepository, encoder); }

    // ---------- Création de compte ----------

    @Test // S1 + S5 + S9
    void creation_valide_normaliseEmail_et_hacheLeMotDePasse() {
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        User u = accountService.createAccount("  Alice@Test.com ", "Secret123");

        assertThat(u.email()).isEqualTo("alice@test.com");
        assertThat(u.hashedPassword()).isNotEqualTo("Secret123");
        assertThat(encoder.matches("Secret123", u.hashedPassword())).isTrue();
    }

    @Test // S2
    void creation_emailDejaUtilise_estRefusee() {
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

        assertThatThrownBy(() -> accountService.createAccount("alice@test.com", "Secret123"))
                .isInstanceOf(EmailAlreadyUsedException.class);
        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest // S3
    @NullSource
    @ValueSource(strings = {"", "abc", "abc1234"}) // 7 caractères : limite basse
    void creation_motDePasseTropCourt_estRefusee(String mdp) {
        assertThatThrownBy(() -> accountService.createAccount("alice@test.com", mdp))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("trop court");
        verify(userRepository, never()).save(any());
    }

    @Test // S3 : cas limite exact
    void creation_motDePasseDeHuitCaracteres_estAccepte() {
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThat(accountService.createAccount("alice@test.com", "abcd1234")).isNotNull();
    }

    @ParameterizedTest // S4
    @ValueSource(strings = {"", "abc", "alice@", "@test.com", "alice test@x.com"})
    void creation_emailInvalide_estRefusee(String email) {
        assertThatThrownBy(() -> accountService.createAccount(email, "Secret123"))
                .isInstanceOf(InvalidDataException.class)
                .hasMessage("Email invalide");
        verifyNoInteractions(userRepository);
    }

    // ---------- Connexion ----------

    private User alice() {
        return new User("alice@test.com", encoder.encode("Secret123"));
    }

    @Test // S6
    void connexion_valide_retourneLUtilisateur() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(alice()));

        assertThat(accountService.connecte("alice@test.com", "Secret123").email())
                .isEqualTo("alice@test.com");
    }

    @Test // S9
    void connexion_emailEnMajuscules_fonctionne() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(alice()));

        assertThat(accountService.connecte(" ALICE@Test.com ", "Secret123")).isNotNull();
    }

    @Test // S7
    void connexion_mauvaisMotDePasse_estRefusee() {
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(alice()));

        assertThatThrownBy(() -> accountService.connecte("alice@test.com", "Mauvais123"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test // S8 : même message pour un email inconnu
    void connexion_emailInconnu_donneLeMemeMessageQueMauvaisMotDePasse() {
        when(userRepository.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.connecte("inconnu@test.com", "Secret123"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Identifiants incorrects");
    }
}