package org.mql.automation_tests.models.repositories;

import java.util.Optional;

import org.mql.automation_tests.models.User;

public interface  UserRepository {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    User save(User u);
}
