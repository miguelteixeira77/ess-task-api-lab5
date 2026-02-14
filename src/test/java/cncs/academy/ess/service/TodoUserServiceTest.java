package cncs.academy.ess.service;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import cncs.academy.ess.security.PasswordUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TodoUserServiceTest {

    @Test
    void login_shouldReturnValidJWTTokenWhenCredentialsMatch() throws Exception {
        // Arrange
        UserRepository repo = mock(UserRepository.class);
        TodoUserService service = new TodoUserService(repo);

        // IMPORTANTE:
        // Se no vosso Lab2 a password é PBKDF2 hash, aqui tens de criar o User
        // com o hash/salt compatível com a vossa implementação.
        // (Se tiverem um util de hashing, usem-no no teste.)
        User user = new User("useress5", PasswordUtils.hashPassword("cncs**2026"));
        when(repo.findByUsername("useress5")).thenReturn(user);

        // Act
        String token = service.login("useress5", "cncs**2026");

        // Assert (1) começa por "Bearer"
        assertNotNull(token);
        assertTrue(token.startsWith("Bearer "), "Token deve começar por 'Bearer '");

        // Assert (2) resto é JWT com claims esperadas
        String jwtString = token.substring("Bearer ".length()).trim();
        DecodedJWT jwt = JWT.decode(jwtString);

        // Estrutura pedida no Lab2: issuer, username, issuedAt, expiresAt
        assertNotNull(jwt.getIssuer(), "issuer (iss) não pode ser null");
        assertFalse(jwt.getIssuer().isBlank(), "issuer (iss) não pode ser vazio");

        assertEquals("useress5", jwt.getClaim("username").asString(),
                "claim 'username' deve corresponder ao utilizador autenticado");

        assertNotNull(jwt.getIssuedAt(), "issuedAt (iat) não pode ser null");
        assertNotNull(jwt.getExpiresAt(), "expiresAt (exp) não pode ser null");
        assertTrue(jwt.getExpiresAt().after(jwt.getIssuedAt()),
                "exp deve ser depois de iat");

        verify(repo).findByUsername("useress5");
        verifyNoMoreInteractions(repo);
    }
}
