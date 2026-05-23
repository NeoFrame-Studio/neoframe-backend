package com.neoframe.neoframe_backend.modules.auth.infrastructure.email;

import com.neoframe.neoframe_backend.modules.auth.core.ports.out.EmailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockEmailSenderAdapter implements EmailSenderPort {

    private static final Logger logger = LoggerFactory.getLogger(MockEmailSenderAdapter.class);

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        // Link apontando para a sua futura tela de reset no React
        String resetLink = "http://localhost:5173/reset-password?token=" + resetToken;

        logger.info("\n======================================================");
        logger.info("📧 MOCK EMAIL SENDER - MENSAGEM SIMULADA");
        logger.info("Para: {}", toEmail);
        logger.info("Assunto: Recuperação de Senha - NeoFrame");
        logger.info("Corpo: Você solicitou a recuperação de sua senha.");
        logger.info("Para redefinir, clique no link abaixo:");
        logger.info(resetLink);
        logger.info("======================================================\n");
    }
}
