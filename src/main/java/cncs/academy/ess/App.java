package cncs.academy.ess;

import cncs.academy.ess.controller.AuthorizationMiddleware;
import cncs.academy.ess.controller.TodoController;
import cncs.academy.ess.controller.TodoListController;
import cncs.academy.ess.controller.UserController;
import cncs.academy.ess.repository.memory.InMemoryTodoListsRepository;
import cncs.academy.ess.repository.memory.InMemoryTodoRepository;
import cncs.academy.ess.repository.memory.InMemoryUserRepository;
import cncs.academy.ess.service.TodoListsService;
import cncs.academy.ess.service.TodoUserService;
import cncs.academy.ess.service.TodoService;
import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;

import java.security.NoSuchAlgorithmException;

public class App {

    public static void main(String[] args) throws NoSuchAlgorithmException {

        // 🔐 HTTPS configuration (cert.pem + key.pem dentro do container)
        SslPlugin sslPlugin = new SslPlugin(conf -> {
            conf.pemFromPath("cert.pem", "key.pem");
        });

        Javalin app = Javalin.create(config -> {

            // Enable HTTPS
            config.registerPlugin(sslPlugin);

            // Enable CORS
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> it.anyHost());
            });

        }).start(); // Sem porta explícita -> usa 80 e 443

        // =========================
        // Repositórios em memória
        // =========================

        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        InMemoryTodoListsRepository listsRepository = new InMemoryTodoListsRepository();
        InMemoryTodoRepository todoRepository = new InMemoryTodoRepository();

        // Services
        TodoUserService userService = new TodoUserService(userRepository);
        TodoListsService toDoListService = new TodoListsService(listsRepository);
        TodoService todoService = new TodoService(todoRepository, listsRepository);

        // Controllers
        UserController userController = new UserController(userService);
        TodoListController todoListController = new TodoListController(toDoListService);
        TodoController todoController = new TodoController(todoService, toDoListService);

        // Authorization middleware
        AuthorizationMiddleware authMiddleware = new AuthorizationMiddleware(userRepository);

        // CORS headers
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "*");
        });

        // Authorization middleware
        app.before(authMiddleware::handle);

        // =========================
        // Routes
        // =========================

        // User management
        app.post("/user", userController::createUser);
        app.get("/user/{userId}", userController::getUser);
        app.delete("/user/{userId}", userController::deleteUser);
        app.post("/login", userController::loginUser);
        app.post("/user/{userId}/upload", userController::addProfilePicture);

        // Todo lists
        app.post("/todolist", todoListController::createTodoList);
        app.get("/todolist", todoListController::getAllTodoLists);
        app.get("/todolist/{listId}", todoListController::getTodoList);
        app.post("/todolist/{listId}/share", ctx -> ctx.result("shared"));

        // Todo items
        app.post("/todo/item", todoController::createTodoItem);
        app.get("/todo/{listId}/tasks", todoController::getAllTodoItems);
        app.get("/todo/{listId}/tasks/{taskId}", todoController::getTodoItem);
        app.delete("/todo/{listId}/tasks/{taskId}", todoController::deleteTodoItem);

        // Dummy data
        fillDummyData(userService, toDoListService, todoService);
    }

    private static void fillDummyData(
            TodoUserService userService,
            TodoListsService toDoListService,
            TodoService todoService) throws NoSuchAlgorithmException {

        userService.addUser("user1", "password1");
        userService.addUser("user2", "password2");

        toDoListService.createTodoListItem("Shopping list", 1);
        toDoListService.createTodoListItem("Other", 1);

        todoService.createTodoItem("Bread", 1);
        todoService.createTodoItem("Milk", 1);
        todoService.createTodoItem("Eggs", 1);
        todoService.createTodoItem("Cheese", 1);
        todoService.createTodoItem("Butter", 1);
    }
}