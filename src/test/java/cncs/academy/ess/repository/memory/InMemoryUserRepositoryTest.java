package cncs.academy.ess.repository.memory;

import cncs.academy.ess.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    @Test
    void saveAndFindById_ShouldReturnSavedUser() {
        User user = new User("jane", "password");
        int id = repository.save(user);

        User savedUser = repository.findById(id);
        assertSame(user, savedUser);
    }

    @Test
    void findById_WhenUserDoesNotExist_ShouldReturnNull() {
        assertNull(repository.findById(999));
    }

    @Test
    void findAll_ShouldReturnAllSavedUsers() {
        User u1 = new User("u1", "p1");
        User u2 = new User("u2", "p2");

        repository.save(u1);
        repository.save(u2);

        List<User> all = repository.findAll();
        assertEquals(2, all.size());
        assertTrue(all.contains(u1));
        assertTrue(all.contains(u2));
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        User u1 = new User("u1", "p1");
        int id = repository.save(u1);

        repository.deleteById(id);

        assertNull(repository.findById(id));
        assertNull(repository.findByUsername("u1"));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void findByUsername_ShouldReturnMatchingUser() {
        User u1 = new User("alice", "p1");
        User u2 = new User("bob", "p2");

        repository.save(u1);
        repository.save(u2);

        assertSame(u2, repository.findByUsername("bob"));
        assertNull(repository.findByUsername("charlie"));
    }

    @Test
    void save_WithExistingId_ShouldNotGenerateNewId() {
        User u1 = new User("fixed", "p");
        u1.setId(123);

        int id = repository.save(u1);

        assertEquals(123, id);
        assertSame(u1, repository.findById(123));
    }
}
