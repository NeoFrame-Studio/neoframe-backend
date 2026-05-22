package com.neoframe.neoframe_backend.core.ports.out;

public interface EmailSenderPort {
    void sendPasswordResetEmail(String toEmail, String resetToken);
}