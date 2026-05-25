-- Adiciona a coluna plan para gerenciar as tiers (Starter, Pro, SaaS)
ALTER TABLE users ADD COLUMN plan VARCHAR(50) DEFAULT 'STARTER';