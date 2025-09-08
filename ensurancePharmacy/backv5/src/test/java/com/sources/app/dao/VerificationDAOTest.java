package com.sources.app.dao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.User;

public class VerificationDAOTest {

    @Test
    public void testVerificationDAOInstantiation() {
        // TODO: implement tests for VerificationDAO
        VerificationDAO instance = new VerificationDAO();
        assertNotNull(instance);
    }

    @Test
    public void testVerifyUserReturnsTrueWhenExternalUserExists() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        User userFromJson;
        try (InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/user.json")) {
            assertNotNull(is, "No se pudo cargar el recurso user.json desde el classpath");
            userFromJson = mapper.readValue(is, User.class);
        }

        // Ensure unique email to avoid collisions across runs
        String email = "ext+" + System.currentTimeMillis() + "@example.com";
        userFromJson.setEmail(email);

        UserDAO userDAO = new UserDAO();
        User created = userDAO.create(
                userFromJson.getName(),
                userFromJson.getCui(),
                userFromJson.getPhone(),
                userFromJson.getEmail(),
                userFromJson.getBirthDate(),
                userFromJson.getAddress(),
                userFromJson.getPassword()
        );
        assertNotNull(created);
        assertNotNull(created.getIdUser());

        // Set role to 'externo' and persist the change
        created.setRole("externo");
        assertNotNull(new UserDAO().update(created));

        VerificationDAO verificationDAO = new VerificationDAO();
        assertTrue(verificationDAO.verifyUser(email));
    }

    @Test
    public void testVerifyUserReturnsFalseWhenUserNotFound() {
        VerificationDAO verificationDAO = new VerificationDAO();
        String nonexistent = "nope+" + System.currentTimeMillis() + "@example.com";
        assertFalse(verificationDAO.verifyUser(nonexistent));
    }
}
