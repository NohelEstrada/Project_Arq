package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Policy;

public class PolicyDAOTest {
    private PolicyDAO policyDAO = new PolicyDAO();
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testPolicyFromJsonFile() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/policy.json");
        assertNotNull(is);
        Policy policyFromJson = mapper.readValue(is, Policy.class);
        assertNotNull(policyFromJson);
        assertEquals(15.0, policyFromJson.getPercentage());
        assertEquals("Y", String.valueOf(policyFromJson.getEnabled()));
    }

    @Test
    public void testCreatePolicyFromJson() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/policy.json");
        assertNotNull(is);
        Policy policyFromJson = mapper.readValue(is, Policy.class);
        Policy created = policyDAO.create(policyFromJson.getPercentage(), policyFromJson.getEnabled());
        assertNotNull(created);
        assertNotNull(created.getIdPolicy());
        assertEquals(policyFromJson.getPercentage(), created.getPercentage());
        assertEquals(policyFromJson.getEnabled(), created.getEnabled());
    }

    @Test
    public void testGetAllGetByIdAndUpdate() throws Exception {
        java.util.List<Policy> before = policyDAO.getAll();
        int base = before == null ? 0 : before.size();

        // Crear desde JSON para usar valores consistentes
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/policy.json");
        assertNotNull(is);
        Policy fromJson = mapper.readValue(is, Policy.class);
        Policy created = policyDAO.create(fromJson.getPercentage(), fromJson.getEnabled());
        assertNotNull(created);

        java.util.List<Policy> after = policyDAO.getAll();
        assertNotNull(after);
        assertTrue(after.size() >= base + 1);

        Policy byId = policyDAO.getById(created.getIdPolicy());
        assertNotNull(byId);
        assertEquals(fromJson.getPercentage(), byId.getPercentage());
        assertEquals(fromJson.getEnabled(), byId.getEnabled());

        // Update percentage and enabled
        byId.setPercentage(20.0);
        byId.setEnabled('N');
        Policy updated = policyDAO.update(byId);
        assertNotNull(updated);
        assertEquals(20.0, updated.getPercentage());
        assertEquals('N', updated.getEnabled());

        Policy reloaded = policyDAO.getById(created.getIdPolicy());
        assertNotNull(reloaded);
        assertEquals(20.0, reloaded.getPercentage());
        assertEquals('N', reloaded.getEnabled());
    }

    @Test
    public void testGetByIdNegativeReturnsNull() {
        assertNull(policyDAO.getById(-1L));
    }
}