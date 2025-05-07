package fr.jessee.classicAPI.util;

import fr.jessee.classicAPI.ClassicAPI;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class JsonWebToken {
    private static final String SECRET_KEY_STRING = ClassicAPI.getYamlConfig().jwt.secret;
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));

    public static String generateToken(String username, long tokenExpiration) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claim("sub", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(tokenExpiration, ChronoUnit.MILLIS)))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static boolean isSignatureValid(String token) {
        try {
            Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Claims getClaimsFromToken(String token) {
        return Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();
    }

    public static boolean isTokenValid(String token) {
        try {
            Claims claims = getClaimsFromToken(token);

            return isSignatureValid(token) &&
                    !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }
}
