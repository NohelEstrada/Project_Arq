package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.User;

public class UserDAOTest {

    private UserDAO userDAO = new UserDAO();
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testUserFromJson() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/user.json")) {
            assertNotNull(is, "No se pudo cargar el recurso user.json desde el classpath");
            User user = mapper.readValue(is, User.class);
            assertNotNull(user);
            assertEquals("John Doe", user.getName());
        }
    }

    @Test
    public void testCreateAndLoginUserFromJson() throws Exception {
        User userFromJson;
        try (InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/user.json")) {
            assertNotNull(is, "No se pudo cargar el recurso user.json desde el classpath");
            userFromJson = mapper.readValue(is, User.class);
        }

        // Evitar colisiones entre ejecuciones/entornos usando un email único
        String uniqueEmail = "john+" + System.currentTimeMillis() + "@example.com";
        userFromJson.setEmail(uniqueEmail);

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

        User logged = userDAO.login(created.getEmail(), userFromJson.getPassword());
        assertNotNull(logged, "El login devolvió null para email=" + created.getEmail());
        assertEquals(userFromJson.getName(), logged.getName());
    }
}
