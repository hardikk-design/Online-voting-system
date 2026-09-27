package com.voting.online_voting_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class VoteEncryptionService {

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public VoteEncryptionService(
            @Value("${vote.encryption.key}") String base64Key
    ) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);

        if (decodedKey.length != 32) {
            throw new IllegalArgumentException(
                    "Vote encryption key must be 32 bytes"
            );
        }

        this.secretKey = new SecretKeySpec(decodedKey, "AES");
    }

    public String encrypt(Long candidateId) {

        try {
            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(128, iv);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    gcmSpec
            );

            byte[] encrypted =
                    cipher.doFinal(
                            String.valueOf(candidateId)
                                    .getBytes(StandardCharsets.UTF_8)
                    );

            byte[] result =
                    new byte[iv.length + encrypted.length];

            System.arraycopy(
                    iv,
                    0,
                    result,
                    0,
                    iv.length
            );

            System.arraycopy(
                    encrypted,
                    0,
                    result,
                    iv.length,
                    encrypted.length
            );

            return Base64.getEncoder().encodeToString(result);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to encrypt vote",
                    e
            );
        }
    }

    public Long decrypt(String encryptedVote) {

        try {
            byte[] data =
                    Base64.getDecoder()
                            .decode(encryptedVote);

            byte[] iv = new byte[12];

            System.arraycopy(
                    data,
                    0,
                    iv,
                    0,
                    12
            );

            byte[] encrypted =
                    new byte[data.length - 12];

            System.arraycopy(
                    data,
                    12,
                    encrypted,
                    0,
                    encrypted.length
            );

            Cipher cipher =
                    Cipher.getInstance(
                            "AES/GCM/NoPadding"
                    );

            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(128, iv);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    gcmSpec
            );

            byte[] decrypted =
                    cipher.doFinal(encrypted);

            return Long.parseLong(
                    new String(
                            decrypted,
                            StandardCharsets.UTF_8
                    )
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to decrypt vote",
                    e
            );
        }
    }
}
