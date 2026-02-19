package com.crn.lgdms.integration.repository;

import com.crn.lgdms.fixtures.TestDataFactory;
import com.crn.lgdms.modules.users.domain.entity.User;
import com.crn.lgdms.modules.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        User user = TestDataFactory.createTestUser();

        User saved = userRepository.save(user);

        assertNotNull(saved.getId());

        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(user.getUsername(), found.get().getUsername());
    }

    @Test
    void shouldFindByUsername() {
        User user = TestDataFactory.createTestUser();
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername(user.getUsername());
        assertTrue(found.isPresent());
    }

    @Test
    void shouldCheckUsernameExists() {
        User user = TestDataFactory.createTestUser();
        userRepository.save(user);

        boolean exists = userRepository.existsByUsername(user.getUsername());
        assertTrue(exists);

        exists = userRepository.existsByUsername("nonexistent");
        assertFalse(exists);
    }
}
