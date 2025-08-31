package com.qrcode.QR_Code_Apps.repository;

import com.qrcode.QR_Code_Apps.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByEmail() {
        // Given
        User user = new User();
        user.setFirstName("Sagar");
        user.setLastName("Jate");
        user.setEmail("sagar@gmail.com");
        user.setPassword("12345");
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("sagar@gmail.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Sagar", found.get().getFirstName());
        assertEquals("Jate", found.get().getLastName());
        assertEquals("sagar@gmail.com", found.get().getEmail());
    }

    @Test
    void testExistsByEmail() {
        // Given
        User user = new User();
        user.setFirstName("Ganesh");
        user.setLastName("Jate");
        user.setEmail("ganesh@gmail.com");
        user.setPassword("12345");
        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByEmail("ganesh@gmail.com");
        boolean notExists = userRepository.existsByEmail("nonexistent@gmail.com");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }
}
