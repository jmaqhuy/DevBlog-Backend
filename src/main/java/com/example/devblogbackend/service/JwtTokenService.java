package com.example.devblogbackend.service;

import com.example.devblogbackend.enums.Role;
import com.example.devblogbackend.exception.AuthenticationException;
import com.example.devblogbackend.exception.TokenValidationException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.StringJoiner;

@Slf4j
@Service
public class JwtTokenService {
    
    @Value("${jwt.signerKey}")
    private String signerKey;

    public String generateToken(String uid, Set<Role> roles) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claimsSet = buildClaims(uid, roles);
        Payload payload = new Payload(claimsSet.toJSONObject());
        
        return signToken(header, payload, uid);
    }

    private JWTClaimsSet buildClaims(String uid, Set<Role> roles) {
        return new JWTClaimsSet.Builder()
                .subject(uid)
                .issuer("devblogbackend")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli()))
                .claim("scope", scopeBuilder(roles))
                .build();
    }

    public boolean validateToken(String token) {
        try {
            JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(token);

            boolean verified = signedJWT.verify(verifier);
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            
            return verified && expirationTime.after(new Date());
        } catch (JOSEException | ParseException e) {
            log.error("Token validation failed: {}", e.getMessage());
            throw new TokenValidationException("Invalid token format or signature", e);
        }
    }

    private String signToken(JWSHeader header, Payload payload, String uid) {
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token for {}: {}", uid, e.getMessage());
            throw new TokenValidationException("Token generation failed", e);
        }
    }

    public String validateAndGetUserId(String token) {
        try {
            JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(token);

            boolean verified = signedJWT.verify(verifier);
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            
            if (!verified || !expirationTime.after(new Date())) {
                throw new AuthenticationException("Invalid or expired token");
            }

            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (JOSEException | ParseException e) {
            log.error("Token validation failed: {}", e.getMessage());
            throw new TokenValidationException("Invalid token format or signature", e);
        }
    }

    private String scopeBuilder(Set<Role> roles) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        for (Role role : roles) {
            stringJoiner.add(role.toString());
        }
        return stringJoiner.toString();
    }
} 