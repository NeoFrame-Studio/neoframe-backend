package com.neoframe.neoframe_backend.core.ports.out;

import com.neoframe.neoframe_backend.core.domain.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    Optional<User> findById(UUID id);
    User save(User user);
}
