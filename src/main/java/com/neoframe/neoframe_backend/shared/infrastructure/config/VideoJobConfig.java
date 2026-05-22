package com.neoframe.neoframe_backend.shared.infrastructure.config;

import com.neoframe.neoframe_backend.core.ports.in.VideoJobUseCase;
import com.neoframe.neoframe_backend.core.ports.out.UserRepositoryPort;
import com.neoframe.neoframe_backend.core.ports.out.VideoJobRepositoryPort;
import com.neoframe.neoframe_backend.core.services.VideoJobService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VideoJobConfig {

    @Bean
    public VideoJobUseCase videoJobUseCase(
            UserRepositoryPort userRepository,
            VideoJobRepositoryPort videoJobRepository) {

        return new VideoJobService(userRepository, videoJobRepository);
    }
}