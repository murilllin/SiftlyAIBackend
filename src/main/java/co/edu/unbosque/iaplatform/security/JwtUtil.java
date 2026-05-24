package co.edu.unbosque.iaplatform.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilidad para manejo de tokens JWT.
 * Genera, valida y extrae información de tokens de acceso y refresh.
 *
 * @author Daniel Murillo
 * @version 1.0
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un token de acceso JWT.
     *
     * @param email     Email del usuario (subject)
     * @param rol       Rol del usuario (ADMIN/USUARIO)
     * @param usuarioId ID del usuario
     * @return Token JWT firmado
     */
    public String generarToken(String email, String rol, long usuarioId) {
        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .claim("usuarioId", usuarioId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getKey())
                .compact();
    }

    /**
     * Genera un token de refresh JWT con mayor duración.
     *
     * @param email Email del usuario
     * @return Refresh token JWT firmado
     */
    public String generarRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(getKey())
                .compact();
    }

    /**
     * Extrae todos los claims del token.
     *
     * @param token Token JWT
     * @return Claims del token
     * @throws JwtException Si el token es inválido
     */
    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extrae el email del token.
     *
     * @param token Token JWT
     * @return Email del usuario
     */
    public String extraerEmail(String token) {
        return extraerClaims(token).getSubject();
    }

    /**
     * Extrae el rol del token.
     *
     * @param token Token JWT
     * @return Rol del usuario
     */
    public String extraerRol(String token) {
        return extraerClaims(token).get("rol", String.class);
    }

    /**
     * Extrae el ID del usuario del token.
     *
     * @param token Token JWT
     * @return ID del usuario
     */
    public Long extraerUsuarioId(String token) {
        return extraerClaims(token).get("usuarioId", Long.class);
    }

    /**
     * Verifica si el token es válido (firma correcta).
     *
     * @param token Token JWT
     * @return true si el token es válido
     */
    public boolean esValido(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Verifica si el token ha expirado.
     *
     * @param token Token JWT
     * @return true si el token está expirado
     */
    public boolean estaExpirado(String token) {
        try {
            return extraerClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}