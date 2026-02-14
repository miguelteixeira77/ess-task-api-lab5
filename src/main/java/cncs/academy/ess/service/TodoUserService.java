package cncs.academy.ess.service;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import cncs.academy.ess.security.PasswordUtils;
import cncs.academy.ess.security.JwtUtils;


import java.security.NoSuchAlgorithmException;

public class TodoUserService {

    private final UserRepository repository;

    public TodoUserService(UserRepository userRepository) {
        this.repository = userRepository;
    }

    // 🔐 Create user (hash password)
    public User addUser(String username, String password)
            throws NoSuchAlgorithmException {

        try {
            String hashedPassword = PasswordUtils.hashPassword(password);
            User user = new User(username, hashedPassword);
            int id = repository.save(user);
            user.setId(id);
            return user;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    public User getUser(int id) {
        return repository.findById(id);
    }

    public void deleteUser(int id) {
        repository.deleteById(id);
    }

    // 🔑 Login with PBKDF2 verification
    public String login(String username, String password)
            throws NoSuchAlgorithmException {

        User user = repository.findByUsername(username);
        if (user == null) {
            return null;
        }

        try {
            boolean valid = PasswordUtils.verifyPassword(
                    password,
                    user.getPassword()
            );

            if (valid) {
                return createAuthToken(user); // unchanged (2.b)
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify password", e);
        }
    }

    // ⚠️ Token fraco — será corrigido no ponto 2.b
    private String createAuthToken(User user) {
        return "Bearer " + JwtUtils.generateToken(
                user.getId(),
                user.getUsername()
        );
    }
}
