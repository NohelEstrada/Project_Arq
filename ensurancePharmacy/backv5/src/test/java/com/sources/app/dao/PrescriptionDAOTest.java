package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Prescription;
import com.sources.app.entities.Hospital;
import com.sources.app.entities.User;

public class PrescriptionDAOTest {
    private PrescriptionDAO dao = new PrescriptionDAO();
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testPrescriptionFromJsonFile() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/prescription.json");
        assertNotNull(is);
        Prescription p = mapper.readValue(is, Prescription.class);
        assertNotNull(p);
        assertEquals("Y", String.valueOf(p.getApproved()));
    }

    @Test
    public void testCreatePrescriptionFromJson() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/prescription.json");
        assertNotNull(is);
        Prescription pFromJson = mapper.readValue(is, Prescription.class);
        Hospital h = new Hospital();
        User u = new User();
        Prescription created = dao.create(h, u, pFromJson.getApproved());
        assertNotNull(created);
        assertNotNull(created.getIdPrescription());
        assertEquals(pFromJson.getApproved(), created.getApproved());
    }
}