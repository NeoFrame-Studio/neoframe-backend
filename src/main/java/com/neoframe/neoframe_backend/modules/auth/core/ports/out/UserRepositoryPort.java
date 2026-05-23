package com.neoframe.neoframe_backend.modules.auth.core.ports.out;

import com.neoframe.neoframe_backend.modules.auth.core.domain.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);
}