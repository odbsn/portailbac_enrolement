package com.officedubac.project.services.impl;

import com.officedubac.project.services.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTServiceImpl implements JWTService {
    private static final Logger logger = LoggerFactory.getLogger(JWTServiceImpl.class);
    // Access token : 15 minutes
    private static final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 15;  // 15 min
    // Refresh token valide pendant : 8 heures (temps de travail normal)
    private static final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 60 * 8;  // 8 heures
    public String generateToken(UserDetails userDetails) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        return Jwts.builder().setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Map<String, Object> extraClaims, UserDetails userDetails) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        return Jwts.builder().setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserName(String token) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private static final Key signinKey;

    // Initialisation statique de la clé
    static
    {
        signinKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        // Log pour afficher l'objet de la clé
        //logger.info("Generated Secret Key: " + signinKey.toString());
        // Convertir la clé en Base64 pour l'afficher dans les logs
        String encodedKey = Base64.getEncoder().encodeToString(signinKey.getEncoded());
        //logger.info("Generated Secret Key (Base64): " + encodedKey);
    }

    // Méthode pour obtenir la clé
    public Key getSigninKey() {
        return signinKey;  // Retourner la clé pré-générée
    }

    private Claims extractAllClaims(String token) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        return Jwts.parserBuilder().setSigningKey(getSigninKey()).build().parseClaimsJws(token).getBody();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        final String login = extractUserName(token);
        return (login.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
