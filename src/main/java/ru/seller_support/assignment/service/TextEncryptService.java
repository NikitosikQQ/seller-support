package ru.seller_support.assignment.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

@Service
public class TextEncryptService {

    @Value("${app.security.secret}")
    private String secret;

    @Value("${app.security.salt}")
    private String salt;

    private TextEncryptor textEncryptor;

    @PostConstruct
    public void init() {
        textEncryptor = Encryptors.text(secret, salt);
    }

    public String encrypt(String text) {
        return textEncryptor.encrypt(text);
    }

    public String decrypt(String text) {
        return textEncryptor.decrypt(text);
    }

}
