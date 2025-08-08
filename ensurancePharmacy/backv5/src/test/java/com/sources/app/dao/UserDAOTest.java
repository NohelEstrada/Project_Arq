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
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/user.json");
        assertNotNull(is);
        User user = mapper.readValue(is, User.class);
        assertNotNull(user);
        assertEquals("John Doe", user.getName());
    }

    @Test
    public void testCreateAndLoginUserFromJson() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/user.json");
        assertNotNull(is);
        User userFromJson = mapper.readValue(is, User.class);


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


        User logged = userDAO.login(userFromJson.getEmail(), userFromJson.getPassword());
        assertNotNull(logged);
        assertEquals(userFromJson.getName(), logged.getName());
    }
}