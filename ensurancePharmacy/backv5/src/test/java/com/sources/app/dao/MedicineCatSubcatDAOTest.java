package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.MedicineCatSubcat;
import com.sources.app.entities.Medicine;
import com.sources.app.entities.Category;
import com.sources.app.entities.Subcategory;

public class MedicineCatSubcatDAOTest {
    private MedicineCatSubcatDAO dao = new MedicineCatSubcatDAO();
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testMedicineCatSubcatFromJsonFile() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/medicineCatSubcat.json");
        assertNotNull(is);
        MedicineCatSubcat mcs = mapper.readValue(is, MedicineCatSubcat.class);
        assertNotNull(mcs);
        assertEquals("Test Medicine", mcs.getMedicine().getName());
        assertEquals("Test Category", mcs.getCategory().getName());
        assertEquals("Test Subcategory", mcs.getSubcategory().getName());
    }

    @Test
    public void testCreateMedicineCatSubcatFromJson() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/medicineCatSubcat.json");
        assertNotNull(is);
        MedicineCatSubcat mcsJson = mapper.readValue(is, MedicineCatSubcat.class);
        MedicineCatSubcat created = dao.create(mcsJson.getMedicine(), mcsJson.getCategory(), mcsJson.getSubcategory());
        assertNotNull(created);
        assertNotNull(created.getMedicine());
        assertNotNull(created.getCategory());
        assertNotNull(created.getSubcategory());
        assertEquals("Test Medicine", created.getMedicine().getName());
        assertEquals("Test Category", created.getCategory().getName());
        assertEquals("Test Subcategory", created.getSubcategory().getName());
    }
}