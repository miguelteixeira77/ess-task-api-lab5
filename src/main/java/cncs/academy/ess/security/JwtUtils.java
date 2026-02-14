package cncs.academy.ess.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

public class JwtUtils {

    private static final String SECRET = "very-secret-key-change-me";
    private static final long EXPIRATION_TIME = 60 * 60 * 1000; // 1h

    private static final Algorithm algorithm =
            Algorithm.HMAC256(SECRET);

    public static String generateToken(int userId, String username) {
        return JWT.create()
                .withIssuer("cncs.academy.ess")
                .withSubject(username)
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }

    public static Integer verifyToken(String token) {
        return JWT.require(algorithm)
                .build()
                .verify(token)
                .getClaim("userId")
                .asInt();
    }
}
