package org.ouanu.manager.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class KeyPairRotationService {

    private final AtomicReference<KeyPairHolder> currentKeyPair = new AtomicReference<>();
    private final Deque<KeyPairHolder> previousKeyPairs = new ConcurrentLinkedDeque<>();
    private static final int MAX_HISTORY = 3;

    @PostConstruct
    public void init() {
        rotateKeyPair();
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduledKeyRotation() {
        rotateKeyPair();
    }

    public synchronized void rotateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair newKeyPair = keyPairGenerator.generateKeyPair();

            String publicKey = Base64.getEncoder().encodeToString(newKeyPair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(newKeyPair.getPrivate().getEncoded());

            KeyPairHolder oldHolder = currentKeyPair.get();
            if (oldHolder != null) {
                previousKeyPairs.addFirst(oldHolder);
                while (previousKeyPairs.size() > MAX_HISTORY) {
                    previousKeyPairs.removeLast();
                }
            }

            KeyPairHolder newHolder = new KeyPairHolder(newKeyPair, publicKey, privateKey);

            currentKeyPair.set(newHolder);

            System.out.println("The key has been changed, new public key: " + publicKey.substring(0, 20) + "...");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Create keypair failed",e);
        }
    }

    public KeyPairHolder getCurrentKeyPair() {
        return currentKeyPair.get();
    }

    public List<KeyPairHolder> getPreviousKeyPairs() {
        return new ArrayList<>(previousKeyPairs);
    }

    @Getter
    public static class KeyPairHolder {
        private final KeyPair keyPair;
        private final String publicKeyStr;
        private final String privateKeyStr;
        private final long creationTime = System.currentTimeMillis();

        public KeyPairHolder(KeyPair keyPair, String publicKeyStr, String privateKeyStr) {
            this.keyPair = keyPair;
            this.publicKeyStr = publicKeyStr;
            this.privateKeyStr = privateKeyStr;
        }

    }
}
