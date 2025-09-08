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
        // Persistir hospital y usuario antes de crear la prescripción
        HospitalDAO hospitalDAO = new HospitalDAO();
        UserDAO userDAO = new UserDAO();
        Hospital h = hospitalDAO.create("Hosp Test", "555-0000", "h@test.com", "Addr", '1');
        assertNotNull(h);
        User u = userDAO.create("User Test", "123", "555", "u@test.com", new java.util.Date(), "Addr", "pwd");
        assertNotNull(u);
        Prescription created = dao.create(h, u, pFromJson.getApproved());
        assertNotNull(created);
        assertNotNull(created.getIdPrescription());
        assertEquals(pFromJson.getApproved(), created.getApproved());
    }

    @Test
    public void testGetAllGetByIdAndUpdate() {
        java.util.List<Prescription> before = dao.getAll();
        int base = before == null ? 0 : before.size();

        HospitalDAO hospitalDAO = new HospitalDAO();
        UserDAO userDAO = new UserDAO();
        Hospital h = hospitalDAO.create("Hosp A", "555-1111", "a@h.com", "Addr A", '1');
        User u = userDAO.create("User A", "111", "555-1111", "a@u.com", new java.util.Date(), "Addr A", "pwd");
        Prescription created = dao.create(h, u, 'Y');
        assertNotNull(created);

        java.util.List<Prescription> after = dao.getAll();
        assertNotNull(after);
        assertTrue(after.size() >= base + 1);

        Prescription byId = dao.getById(created.getIdPrescription());
        assertNotNull(byId);
        assertEquals('Y', byId.getApproved());

        byId.setApproved('N');
        Prescription updated = dao.update(byId);
        assertNotNull(updated);
        assertEquals('N', updated.getApproved());

        Prescription reloaded = dao.getById(created.getIdPrescription());
        assertNotNull(reloaded);
        assertEquals('N', reloaded.getApproved());
    }

    @Test
    public void testGetByIdNegativeReturnsNull() {
        assertNull(dao.getById(-1L));
    }
}
