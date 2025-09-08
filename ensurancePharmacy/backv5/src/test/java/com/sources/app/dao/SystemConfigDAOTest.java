package com.sources.app.dao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.sources.app.entities.SystemConfig;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SystemConfigDAOTest {

    @Test
    public void testSystemConfigDAOInstantiation() {
        // TODO: implement tests for SystemConfigDAO
        SystemConfigDAO instance = new SystemConfigDAO();
        assertNotNull(instance);
    }

    @Test
    public void testSaveOrUpdateCreateAndGet() {
        SystemConfigDAO dao = new SystemConfigDAO();
        String key = "SITE_NAME";
        SystemConfig created = dao.saveOrUpdate(key, "Ensurance", "Nombre de sitio");
        assertNotNull(created);
        assertNotNull(created.getIdConfig());
        assertEquals(key, created.getConfigKey());
        assertEquals("Ensurance", created.getConfigValue());
        assertEquals("Nombre de sitio", created.getDescription());
        assertNotNull(created.getLastUpdated());

        SystemConfig fetched = dao.getByKey(key);
        assertNotNull(fetched);
        assertEquals(created.getIdConfig(), fetched.getIdConfig());
    }

    @Test
    public void testSaveOrUpdateUpdateValueOnlyAndDescription() throws Exception {
        SystemConfigDAO dao = new SystemConfigDAO();
        String key = "MAX_USERS_" + System.currentTimeMillis(); // Make key unique
        
        // Create initial config
        SystemConfig created = dao.saveOrUpdate(key, "100", "Máximo de usuarios");
        assertNotNull(created);
        String originalDesc = created.getDescription();
        assertEquals("Máximo de usuarios", originalDesc);

        // Wait to ensure different timestamps
        TimeUnit.MILLISECONDS.sleep(10);
        
        // Update only value (description unchanged when null)
        SystemConfig updatedValueOnly = dao.saveOrUpdate(key, "150", null);
        assertNotNull(updatedValueOnly);
        assertEquals("150", updatedValueOnly.getConfigValue());
        assertEquals(originalDesc, updatedValueOnly.getDescription());

        // Wait to ensure different timestamps
        TimeUnit.MILLISECONDS.sleep(10);
        
        // Update both value and description
        SystemConfig updatedBoth = dao.saveOrUpdate(key, "200", "Nuevo límite");
        assertNotNull(updatedBoth);
        assertEquals("200", updatedBoth.getConfigValue());
        assertEquals("Nuevo límite", updatedBoth.getDescription());
        
        // Clean up - delete the test config
        if (updatedBoth.getIdConfig() != null) {
            dao.delete(updatedBoth.getIdConfig());
        }
    }

    @Test
    public void testGetAllOrdering() {
        SystemConfigDAO dao = new SystemConfigDAO();
        dao.saveOrUpdate("A_KEY", "1", "primero");
        dao.saveOrUpdate("B_KEY", "2", "segundo");
        dao.saveOrUpdate("C_KEY", "3", "tercero");

        List<SystemConfig> all = dao.getAll();
        assertNotNull(all);
        assertTrue(all.size() >= 3);
        // Should be ordered by configKey ascending
        for (int i = 1; i < all.size(); i++) {
            String prev = all.get(i - 1).getConfigKey();
            String curr = all.get(i).getConfigKey();
            assertTrue(prev.compareTo(curr) <= 0);
        }
    }

    @Test
    public void testGetConfigValueHelpers() {
        SystemConfigDAO dao = new SystemConfigDAO();

        // String helper
        dao.saveOrUpdate("SITE_THEME", "dark", "tema");
        assertEquals("dark", dao.getConfigValue("SITE_THEME", "light"));
        assertEquals("fallback", dao.getConfigValue("UNKNOWN_KEY", "fallback"));

        // Double helper
        dao.saveOrUpdate("THRESHOLD", "12.5", "umbral");
        assertEquals(12.5, dao.getConfigValueAsDouble("THRESHOLD", 0.0));

        dao.saveOrUpdate("THRESHOLD_INVALID", "abc", "no num");
        assertEquals(7.0, dao.getConfigValueAsDouble("THRESHOLD_INVALID", 7.0));

        assertEquals(3.14, dao.getConfigValueAsDouble("MISSING_DOUBLE", 3.14));
    }

    @Test
    public void testDelete() {
        SystemConfigDAO dao = new SystemConfigDAO();
        SystemConfig cfg = dao.saveOrUpdate("TMP_KEY", "x", "tmp");
        assertNotNull(cfg);
        Long id = cfg.getIdConfig();
        assertTrue(dao.delete(id));
        assertNull(dao.getByKey("TMP_KEY"));
        // deleting again should return false
        assertFalse(dao.delete(id));
    }

    @Test
    public void testInitializeDefaultConfigs() {
        SystemConfigDAO dao = new SystemConfigDAO();
        dao.initializeDefaultConfigs();
        SystemConfig min = dao.getByKey("MIN_PRESCRIPTION_AMOUNT");
        assertNotNull(min);
        assertEquals("MIN_PRESCRIPTION_AMOUNT", min.getConfigKey());
        assertNotNull(min.getConfigValue());
    }
}
