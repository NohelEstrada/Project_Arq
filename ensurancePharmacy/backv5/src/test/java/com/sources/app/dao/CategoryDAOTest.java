package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Category;

public class CategoryDAOTest {
    private CategoryDAO dao = new CategoryDAO();
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testCategoryFromJson() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/category.json");
        assertNotNull(is);
        Category cat = mapper.readValue(is, Category.class);
        assertNotNull(cat);
        assertEquals("Electronics", cat.getName());
    }

    @Test
    public void testCreateCategoryFromJson() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/category.json");
        assertNotNull(is);
        Category cat = mapper.readValue(is, Category.class);
        Category created = dao.create(cat.getName());
        assertNotNull(created);
        assertNotNull(created.getIdCategory());
        assertEquals(cat.getName(), created.getName());
    }
}