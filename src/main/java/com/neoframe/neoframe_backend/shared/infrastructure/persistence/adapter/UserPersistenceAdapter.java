package com.neoframe.neoframe_backend.shared.infrastructure.persistence.adapter;

import com.neoframe.neoframe_backend.core.domain.User;
import com.neoframe.neoframe_backend.core.ports.out.UserRepositoryPort;
import com.neoframe.neoframe_backend.shared.infrastructure.persistence.entity.UserEntity;
import com.neoframe.neoframe_backend.shared.infrastructure.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;

    public UserPersistenceAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    private User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getPlan(),
                entity.getCreatedAt()
        );
    }

    private UserEntity toEntity(User user) {
        return new UserEntity(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getPlan(),
                user.getCreatedAt()
        );
    }
}