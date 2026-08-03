package com.neoframe.neoframe_backend.modules.video.core.services;

import com.neoframe.neoframe_backend.modules.auth.core.domain.Plan;
import com.neoframe.neoframe_backend.modules.auth.core.domain.User;
import com.neoframe.neoframe_backend.modules.auth.core.ports.out.UserRepositoryPort;
import com.neoframe.neoframe_backend.modules.video.core.domain.VideoJob;
import com.neoframe.neoframe_backend.modules.video.core.domain.VideoStatus;
import com.neoframe.neoframe_backend.modules.video.core.ports.out.VideoJobRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoJobServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private VideoJobRepositoryPort videoJobRepository;

    @InjectMocks
    private VideoJobService videoJobService;

    @Mock
    private User mockUser;

    private final String EMAIL = "creator@neoframe.com";

    @BeforeEach
    void setUp() {
        lenient().when(mockUser.getId()).thenReturn(UUID.randomUUID());
        lenient().when(mockUser.getPlan()).thenReturn(mock(Plan.class));
    }

    @Test
    @DisplayName("Should create a video job successfully when the user is within the plan limit")
    void createJob_Success() {
        // Arrange
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(mockUser.getProcessingVideosCount()).thenReturn(1);
        when(mockUser.getPlanLimit()).thenReturn(3); // Allows up to 3 videos

        when(videoJobRepository.save(any(VideoJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        VideoJob jobCreated = videoJobService.createJob(
                EMAIL,
                "http://script.url",
                "http://music.url",
                "http://intro.url",
                "http://transition.url"
        );

        // Assert
        assertNotNull(jobCreated);
        assertEquals(VideoStatus.PENDING, jobCreated.getStatus());

        // Verify if the user's processing count was incremented and both user and job were saved
        verify(mockUser, times(1)).incrementProcessingCount();
        verify(userRepository, times(1)).save(mockUser);
        verify(videoJobRepository, times(1)).save(any(VideoJob.class));
    }

    @Test
    @DisplayName("Should throw an exception when the user is not found")
    void createJob_ThrowsException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            videoJobService.createJob(EMAIL, "script", "music", "intro", "transition");
        });

        assertEquals("Usuário não encontrado.", exception.getMessage());
        verify(videoJobRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw an exception when the user reaches or exceeds the simultaneous video limit of their plan")
    void createJob_ThrowsException_WhenPlanLimitReached() {
        // Arrange
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(mockUser.getProcessingVideosCount()).thenReturn(3);
        when(mockUser.getPlanLimit()).thenReturn(3); // Limit reached (3 >= 3)

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            videoJobService.createJob(EMAIL, "script", "music", "intro", "transition");
        });

        assertEquals("Você atingiu o limite de vídeos processando simultaneamente para o seu plano.", exception.getMessage());

        // Ensures the user wasn't modified and the job wasn't created
        verify(mockUser, never()).incrementProcessingCount();
        verify(userRepository, never()).save(any());
        verify(videoJobRepository, never()).save(any());
    }
}