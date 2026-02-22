package cncs.academy.ess.controller;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import cncs.academy.ess.security.JwtUtils;
import io.javalin.http.Context;
import org.casbin.jcasbin.main.Enforcer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AuthorizationMiddleware {

    private final UserRepository userRepository;
    private final Enforcer enforcer;

    public AuthorizationMiddleware(UserRepository userRepository) {
        this.userRepository = userRepository;

        try {
            String modelPath = resourceToTempFile("casbin/model.conf");
            String policyPath = resourceToTempFile("casbin/policy.csv");
            this.enforcer = new Enforcer(modelPath, policyPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Casbin model/policy from resources", e);
        }
    }

    private static String resourceToTempFile(String resourcePath) throws IOException {
        try (InputStream in = AuthorizationMiddleware.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) throw new FileNotFoundException("Resource not found: " + resourcePath);

            String fileName = Paths.get(resourcePath).getFileName().toString();
            Path temp = Files.createTempFile("casbin-", "-" + fileName);
            Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            temp.toFile().deleteOnExit();
            return temp.toString();
        }
    }

    public void handle(Context ctx) {

        // 🔓 Endpoints públicos
        if (isPublicEndpoint(ctx)) {
            return;
        }

        // 🔐 Autenticação (JWT)
        String auth = ctx.header("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            ctx.status(401).result("Unauthorized");
            return;
        }

        Integer userId;
        try {
            userId = JwtUtils.verifyToken(auth.substring(7));
        } catch (Exception e) {
            ctx.status(401).result("Unauthorized");
            return;
        }

        User user = userRepository.findById(userId);
        if (userId == null || user == null) {
            ctx.status(401).result("Unauthorized");
            return;
        }

        // Disponível para controllers
        ctx.attribute("userId", userId);

        // ✅ Subject para Casbin: usar o "utilizador" (ex.: username)
        // A role é resolvida por "g, <username>, role_xxx" no policy.csv
        String sub = user.getUsername();

        String obj = extractObject(ctx);
        String act = extractAction(ctx);

        // Se não reconhece endpoint => nega por defeito (seguro)
        if ("unknown".equals(obj) || "unknown".equals(act)) {
            ctx.status(403).result("Forbidden");
            return;
        }

        boolean allowed;
        try {
            allowed = enforcer.enforce(sub, obj, act);
        } catch (Exception e) {
            ctx.status(403).result("Forbidden");
            return;
        }

        if (!allowed) {
            ctx.status(403).result("Forbidden");
            return;
        }
    }

    // -------- Helpers --------

    private boolean isPublicEndpoint(Context ctx) {
        return (ctx.path().equals("/login") && ctx.method().name().equals("POST")) ||
                (ctx.path().equals("/user") && ctx.method().name().equals("POST"));
    }

    private String extractObject(Context ctx) {
        String path = ctx.path();

        if (path.startsWith("/todolist")) return "todolist";
        if (path.startsWith("/todo")) return "todoitem";   // ✅ alinhado com policy
        if (path.startsWith("/user")) return "user";

        return "unknown";
    }

    private String extractAction(Context ctx) {
        String method = ctx.method().name();
        String path = ctx.path();

        // ✅ endpoint de partilha: /todolist/{listId}/share (POST)
        if (path.startsWith("/todolist") && path.endsWith("/share") && "POST".equals(method)) {
            return "share";
        }

        switch (method) {
            case "GET": return "read";
            case "POST": return "create";
            case "DELETE": return "delete";
            default: return "unknown";
        }
    }
}
