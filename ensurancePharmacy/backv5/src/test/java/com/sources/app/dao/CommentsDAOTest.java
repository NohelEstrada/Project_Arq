package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Comments;
import com.sources.app.entities.User;
import com.sources.app.entities.Medicine;

public class CommentsDAOTest {
    private CommentsDAO commentsDAO = new CommentsDAO();
    private UserDAO userDAO = new UserDAO();
    private MedicineDAO medicineDAO = new MedicineDAO();
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testCreateCommentsFromJsonWithDependencies() throws Exception {
        // Cargar y persistir el User desde su JSON
        InputStream userIs = getClass().getResourceAsStream("/com/sources/app/dao/user.json");
        assertNotNull(userIs);
        User userFromJson = mapper.readValue(userIs, User.class);
        User persistedUser = userDAO.create(
                userFromJson.getName(), userFromJson.getCui(), userFromJson.getPhone(),
                userFromJson.getEmail(), userFromJson.getBirthDate(), userFromJson.getAddress(),
                userFromJson.getPassword()
        );
        assertNotNull(persistedUser);

        // Cargar y persistir el Medicine desde su JSON
        InputStream medIs = getClass().getResourceAsStream("/com/sources/app/dao/medicine.json");
        assertNotNull(medIs);
        Medicine medFromJson = mapper.readValue(medIs, Medicine.class);
        Medicine persistedMedicine = medicineDAO.create(
                medFromJson.getName(), medFromJson.getActiveMedicament(), medFromJson.getDescription(),
                medFromJson.getImage(), medFromJson.getConcentration(), medFromJson.getPresentacion(),
                medFromJson.getStock(), medFromJson.getBrand(), medFromJson.getPrescription(),
                medFromJson.getPrice(), medFromJson.getSoldUnits()
        );
        assertNotNull(persistedMedicine);

        // Cargar el Comments desde su JSON
        InputStream comIs = getClass().getResourceAsStream("/com/sources/app/dao/comments.json");
        assertNotNull(comIs);
        Comments commentFromJson = mapper.readValue(comIs, Comments.class);

        // Crear el comentario usando los objetos persistidos
        Comments created = commentsDAO.create(persistedUser, null, commentFromJson.getCommentText(), persistedMedicine);
        assertNotNull(created);
        assertNotNull(created.getIdComments());
        assertEquals(commentFromJson.getCommentText(), created.getCommentText());
    }
}