
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    plan VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL
    );


CREATE TABLE IF NOT EXISTS video_jobs (
                                          id UUID PRIMARY KEY,
                                          user_id UUID NOT NULL,
                                          script TEXT NOT NULL,
                                          status VARCHAR(50) NOT NULL,
    video_url VARCHAR(512),
    background_music_url VARCHAR(512),
    intro_video_url VARCHAR(512),
    topic_transition_url VARCHAR(512),
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    CONSTRAINT fk_video_jobs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Índices de performance para buscas rápidas de filas por usuário
CREATE INDEX IF NOT EXISTS idx_video_jobs_user_status ON video_jobs(user_id, status);