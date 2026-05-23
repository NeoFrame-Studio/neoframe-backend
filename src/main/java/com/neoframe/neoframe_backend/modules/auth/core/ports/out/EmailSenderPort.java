package com.neoframe.neoframe_backend.modules.auth.core.ports.out;

public interface EmailSenderPort {
    void sendPasswordResetEmail(String toEmail, String resetToken);
}