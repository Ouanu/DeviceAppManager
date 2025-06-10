package org.ouanu.manager.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

@Service
public class EnhancedJwtService {
    private final KeyPairRotationService keyPairRotationService;

    public EnhancedJwtService(KeyPairRotationService keyPairRotationService) {
        this.keyPairRotationService = keyPairRotationService;
    }

    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        KeyPairRotationService.KeyPairHolder keyPairHolder = keyPairRotationService.getCurrentKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPairHolder.getKeyPair().getPrivate();

        return JWT.create()
                .withSubject(userPrincipal.getUsername())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600 * 1000))
                .withClaim("kid", keyPairHolder.getPublicKeyStr().hashCode())
                .sign(Algorithm.RSA256(null, privateKey));
    }

    public DecodedJWT verifyToken(String token) throws JWTVerificationException {
        try {
            KeyPairRotationService.KeyPairHolder currentHolder = keyPairRotationService.getCurrentKeyPair();
            RSAPublicKey currentPublicKey = (RSAPublicKey) currentHolder.getKeyPair().getPublic();

            return JWT.require(Algorithm.RSA256(currentPublicKey, null))
                    .build()
                    .verify(token);
        } catch (JWTVerificationException e) {
            // 如果失败，尝试用历史密钥验证
            for (KeyPairRotationService.KeyPairHolder holder : keyPairRotationService.getPreviousKeyPairs()) {
                try {
                    return JWT.require(Algorithm.RSA256((RSAPublicKey) holder.getKeyPair().getPublic(), null))
                            .build()
                            .verify(token);
                } catch (JWTVerificationException ignored) {
                    // 继续尝试下一个密钥
                    System.out.println("Try the next key again.");
                }
            }
            throw e;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            return jwt.getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}
