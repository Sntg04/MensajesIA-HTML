package com.ia.mensajes.agentemensajesia.util; // O el paquete que hayas elegido

import com.ia.mensajes.agentemensajesia.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest; // Para extraer el token de la cabecera

import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit; // Para el tiempo de expiración

public class JwtUtil {

    // ¡IMPORTANTE! Esta clave secreta DEBE ser compleja, larga y guardada de forma segura.
    // NO la dejes hardcodeada así en producción. Considera variables de entorno o un servicio de secretos.
    // Para este ejemplo, generaremos una clave segura HMAC-SHA256.
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // Tiempo de validez del token (ej. 1 hora)
    private static final long TOKEN_VALIDITY_MS = TimeUnit.HOURS.toMillis(1); // 1 hora en milisegundos
    // private static final long TOKEN_VALIDITY_MS = TimeUnit.MINUTES.toMillis(5); // Para pruebas más cortas

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_AUTHORIZATION = "Authorization";

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
                .setSubject(usuario.getUsername()) // El "asunto" del token, usualmente el username o ID
                .claim("userId", usuario.getId())    // Reclamación personalizada: ID del usuario
                .claim("role", usuario.getRol())     // Reclamación personalizada: Rol del usuario
                .setIssuedAt(now)                    // Fecha de emisión
                .setExpiration(validity)             // Fecha de expiración
                .signWith(SECRET_KEY)                // Firma el token con la clave secreta y el algoritmo por defecto (HS256 aquí)
                .compact();
    }

    /**
     * Valida un token JWT y extrae sus reclamaciones (claims).
     * @param token El token JWT (sin el prefijo "Bearer ").
     * @return Jws<Claims> si el token es válido, null en caso contrario o si hay un error.
     */
    public static Jws<Claims> validateTokenAndGetClaims(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY) // La misma clave secreta usada para generar
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            System.err.println("Token JWT expirado: " + e.getMessage());
            return null;
        } catch (JwtException | IllegalArgumentException e) {
            // Incluye MalformedJwtException, SignatureException, UnsupportedJwtException
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
     * @return El token JWT (sin el prefijo "Bearer ") o null si no se encuentra o el formato es incorrecto.
     */
    public static String extractTokenFromRequest(HttpServletRequest request) { // <--- ¡CORREGIDO AQUÍ!
        String bearerToken = request.getHeader(HEADER_AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}