package com.cMall.feedShop.user.infrastructure.service;

import com.cMall.feedShop.user.domain.service.PasswordEncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BCryptPasswordEncryptionService implements PasswordEncryptionService {
    private final PasswordEncoder passwordEncoder;

    @Override
    public String encrypt(String rawPassword) { // 메서드 이름도 명확하게
        if (rawPassword == null || rawPassword.isEmpty()) {
            return rawPassword;
        }
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}