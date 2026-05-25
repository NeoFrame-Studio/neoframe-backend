-- Migration para atualizar a tabela de video_jobs com os novos campos do plano Starter
ALTER TABLE video_jobs ADD COLUMN script TEXT;
ALTER TABLE video_jobs ADD COLUMN background_music_url VARCHAR(255);
ALTER TABLE video_jobs ADD COLUMN intro_video_url VARCHAR(255);
ALTER TABLE video_jobs ADD COLUMN topic_transition_url VARCHAR(255);
ALTER TABLE video_jobs ADD COLUMN video_url VARCHAR(255);
ALTER TABLE video_jobs ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE video_jobs ADD COLUMN completed_at TIMESTAMP;