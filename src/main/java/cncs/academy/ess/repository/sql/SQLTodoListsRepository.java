package cncs.academy.ess.repository.sql;

import cncs.academy.ess.model.TodoList;
import cncs.academy.ess.repository.TodoListsRepository;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLTodoListsRepository implements TodoListsRepository {

    private final BasicDataSource dataSource;

    public SQLTodoListsRepository(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public TodoList findById(int listId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt =
                    connection.prepareStatement("SELECT * FROM lists WHERE id = ?");
            stmt.setInt(1, listId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToTodoList(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find list by id", e);
        }
        return null;
    }

    @Override
    public List<TodoList> findAll() {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt =
                    connection.prepareStatement("SELECT * FROM lists");

            ResultSet rs = stmt.executeQuery();
            List<TodoList> lists = new ArrayList<>();

            while (rs.next()) {
                lists.add(mapResultSetToTodoList(rs));
            }
            return lists;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all lists", e);
        }
    }

    @Override
    public List<TodoList> findAllByUserId(int userId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt =
                    connection.prepareStatement("SELECT * FROM lists WHERE owner_id = ?");
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            List<TodoList> lists = new ArrayList<>();

            while (rs.next()) {
                lists.add(mapResultSetToTodoList(rs));
            }
            return lists;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find lists by user", e);
        }
    }

    @Override
    public int save(TodoList list) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO lists (name, owner_id) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, list.getName());
            stmt.setInt(2, list.getOwnerId());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save list", e);
        }
    }

    @Override
    public void update(TodoList list) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt =
                    connection.prepareStatement("UPDATE lists SET name = ? WHERE id = ?");

            stmt.setString(1, list.getName());
            stmt.setInt(2, list.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update list", e);
        }
    }

    @Override
    public boolean deleteById(int listId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt =
                    connection.prepareStatement("DELETE FROM lists WHERE id = ?");
            stmt.setInt(1, listId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete list", e);
        }
    }

    private TodoList mapResultSetToTodoList(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        int ownerId = rs.getInt("owner_id");
        return new TodoList(id, name, ownerId);
    }
}
