package com.ia.mensajes.agentemensajesia.util;

import com.ia.mensajes.agentemensajesia.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.spec.SecretKeySpec; // ¡NUEVA IMPORTACIÓN!
import java.nio.charset.StandardCharsets; // ¡NUEVA IMPORTACIÓN!
import java.security.Key;
import java.util.Base64; // ¡NUEVA IMPORTACIÓN!
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class JwtUtil {

    // --- CAMBIO PRINCIPAL: La clave secreta ahora se carga desde el entorno ---
    private static final Key SECRET_KEY = getSecretKeyFromEnv();
    
    // Tiempo de validez del token (1 hora)
    private static final long TOKEN_VALIDITY_MS = TimeUnit.HOURS.toMillis(1);

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    
    /**
     * Método de utilidad para cargar la clave secreta desde una variable de entorno.
     * Esto es crucial para la seguridad en producción.
     * @return La clave de firma (Key).
     */
    private static Key getSecretKeyFromEnv() {
        // Lee la variable de entorno 'JWT_SECRET' que configurarás en Render.
        String secretString = System.getenv("JWT_SECRET");

        // Condición de fallback para desarrollo local o si la variable no está configurada.
        if (secretString == null || secretString.trim().isEmpty()) {
            System.err.println("ADVERTENCIA: La variable de entorno JWT_SECRET no está configurada. Usando una clave de desarrollo temporal. NO USAR EN PRODUCCIÓN.");
            // Genera una clave temporal solo para que la app no falle al iniciar localmente.
            return Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }

        // Decodifica la clave (que debería estar en Base64) para crear la Key real.
        byte[] keyBytes = Base64.getDecoder().decode(secretString.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    /**
     * Genera un token JWT para un usuario.
     * @param usuario El objeto Usuario para quien se genera el token.
     * @return El token JWT como un String.
     */
    public static String generateToken(Usuario usuario) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date validity = new Date(nowMillis + TOKEN_VALIDITY_MS);

        return Jwts.builder()
                .setSubject(usuario.getUsername())
                .claim("userId", usuario.getId())
                .claim("role", usuario.getRol())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SECRET_KEY) // Firma con la clave cargada del entorno
                .compact();
    }

    /**
     * Valida un token JWT y extrae sus reclamaciones (claims).
     * @param token El token JWT (sin el prefijo "Bearer ").
     * @return Jws<Claims> si el token es válido, null en caso contrario.
     */
    public static Jws<Claims> validateTokenAndGetClaims(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY) // Valida con la misma clave del entorno
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            System.err.println("Token JWT expirado: " + e.getMessage());
            return null;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Token JWT inválido: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Extrae el username (subject) de un token JWT válido.
     * @param token El token JWT (sin el prefijo "Bearer ").
     * @return El username o null si el token es inválido.
     */
    public static String getUsernameFromToken(String token) {
        Jws<Claims> claimsJws = validateTokenAndGetClaims(token);
        if (claimsJws != null) {
            return claimsJws.getBody().getSubject();
        }
        return null;
    }

    /**
     * Extrae el rol de un token JWT válido.
     * @param token El token JWT (sin el prefijo "Bearer ").
     * @return El rol o null si el token es inválido o la claim no existe.
     */
    public static String getRoleFromToken(String token) {
        Jws<Claims> claimsJws = validateTokenAndGetClaims(token);
        if (claimsJws != null) {
            return claimsJws.getBody().get("role", String.class);
        }
        return null;
    }
    
    /**
     * Extrae el token JWT de la cabecera Authorization de una solicitud HTTP.
     * @param request La HttpServletRequest.
     * @return El token JWT (sin el prefijo "Bearer ") o null si no se encuentra.
     */
    public static String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
