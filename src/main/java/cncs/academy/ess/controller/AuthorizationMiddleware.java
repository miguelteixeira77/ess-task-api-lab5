package cncs.academy.ess.controller;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import cncs.academy.ess.security.JwtUtils;
import io.javalin.http.Context;
import org.casbin.jcasbin.main.Enforcer;

public class AuthorizationMiddleware {

    private final UserRepository userRepository;
    private final Enforcer enforcer;

    public AuthorizationMiddleware(UserRepository userRepository) {
        this.userRepository = userRepository;

        // Recomendado: manter em resources/casbin/
        this.enforcer = new Enforcer(
                "src/main/resources/casbin/model.conf",
                "src/main/resources/casbin/policy.csv"
        );
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
