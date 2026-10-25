package org.mql.automation_tests.models.services;

import java.util.regex.Pattern;

import org.mql.automation_tests.models.User;
import org.mql.automation_tests.models.exceptions.EmailAlreadyUsedException;
import org.mql.automation_tests.models.exceptions.InvalidCredentialsException;
import org.mql.automation_tests.models.exceptions.InvalidDataException;
import org.mql.automation_tests.models.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
	private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MDP_MIN = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public AccountService(UserRepository repo, PasswordEncoder encoder) {
        this.userRepository = repo;
        this.encoder = encoder;
    }

    public User createAccount(String email, String mdp) {
        String e = normalize(email);
        if (!EMAIL.matcher(e).matches())
            throw new InvalidDataException("Email invalide");
        if (mdp == null || mdp.length() < MDP_MIN)
            throw new InvalidDataException("Mot de passe trop court (8 caractères minimum)");
        if (userRepository.existsByEmail(e))
            throw new EmailAlreadyUsedException();
        return userRepository.save(new User(e, encoder.encode(mdp)));
    }

    public User connecte(String email, String mdp) {
        if (mdp == null) throw new InvalidCredentialsException();
        User user = userRepository.findByEmail(normalize(email))
                .orElseThrow(InvalidCredentialsException::new);
        if (!encoder.matches(mdp, user.hashedPassword()))
            throw new InvalidCredentialsException();
        return user;
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}