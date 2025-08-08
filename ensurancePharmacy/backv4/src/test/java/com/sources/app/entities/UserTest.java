package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.Date;

class UserTest {

    private User user;
    private final Long id = 1L;
    private final String name = "Test User";
    private final Long cui = 1234567890123L;
    private final String phone = "555-1234";
    private final String email = "test@example.com";
    private final String address = "123 Main St";
    private final Date birthDate = new Date();
    private final String role = "USER";
    private final Policy policy = mock(Policy.class); // Mock Policy dependency
    private final Integer enabled = 1;
    private final String password = "password123";
    private final Boolean paidService = true;
    private final Date expirationDate = new Date(System.currentTimeMillis() + 86400000); // Tomorrow
    private Date createdAt;

    @BeforeEach
    void setUp() {
        // Use the full constructor for setup to ensure all fields can be tested
        user = new User(name, cui, phone, email, address, birthDate, role, policy, enabled, password, paidService, expirationDate);
        user.setIdUser(id); // ID is usually set by persistence layer, set it manually for testing
        createdAt = user.getCreatedAt(); // Capture the auto-generated creation date
    }

    @Test
    void noArgsConstructor_InitializesDefaults() {
        User defaultUser = new User();
        assertNull(defaultUser.getIdUser());
        assertNull(defaultUser.getName());
        assertNull(defaultUser.getCui());
        // ... check other fields are null or default
        assertNull(defaultUser.getPaidService()); // Check specific default
        assertNotNull(defaultUser.getCreatedAt()); // Check createdAt is initialized
    }

    @Test
    void fullArgsConstructor_SetsAllFields() {
        assertEquals(id, user.getIdUser()); // Check manually set ID
        assertEquals(name, user.getName());
        assertEquals(cui, user.getCui());
        assertEquals(phone, user.getPhone());
        assertEquals(email, user.getEmail());
        assertEquals(address, user.getAddress());
        assertEquals(birthDate, user.getBirthDate());
        assertEquals(role, user.getRole());
        assertEquals(policy, user.getPolicy());
        assertEquals(enabled, user.getEnabled());
        assertEquals(password, user.getPassword());
        assertEquals(paidService, user.getPaidService());
        assertEquals(expirationDate, user.getExpirationDate());
        assertEquals(createdAt, user.getCreatedAt()); // Check captured creation date
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        User testUser = new User();
        Date newDate = new Date();
        Policy newPolicy = mock(Policy.class);

        testUser.setIdUser(2L);
        assertEquals(2L, testUser.getIdUser());

        testUser.setName("New Name");
        assertEquals("New Name", testUser.getName());

        testUser.setCui(987654321L);
        assertEquals(987654321L, testUser.getCui());
        
        testUser.setPhone("987-6543");
        assertEquals("987-6543", testUser.getPhone());

        testUser.setEmail("new@example.com");
        assertEquals("new@example.com", testUser.getEmail());

        testUser.setAddress("456 Side St");
        assertEquals("456 Side St", testUser.getAddress());

        testUser.setBirthDate(newDate);
        assertEquals(newDate, testUser.getBirthDate());

        testUser.setRole("ADMIN");
        assertEquals("ADMIN", testUser.getRole());

        testUser.setPolicy(newPolicy);
        assertEquals(newPolicy, testUser.getPolicy());

        testUser.setEnabled(0);
        assertEquals(0, testUser.getEnabled());

        testUser.setPassword("newpass");
        assertEquals("newpass", testUser.getPassword());
        
        testUser.setPaidService(false);
        assertEquals(false, testUser.getPaidService());

        testUser.setExpirationDate(newDate);
        assertEquals(newDate, testUser.getExpirationDate());
        
        testUser.setCreatedAt(newDate);
        assertEquals(newDate, testUser.getCreatedAt());
    }
    
    // Add tests for equals() and hashCode() if they are implemented in User.java
    // Example:
    // @Test
    // void equals_And_HashCode_Contracts() {
    //     // Test reflexivity, symmetry, transitivity, consistency, non-null
    // }
} 