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
}