package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.PrescriptionMedicine;
import com.sources.app.entities.Prescription;
import com.sources.app.entities.Medicine;

public class PrescriptionMedicineDAOTest {
    private PrescriptionMedicineDAO dao = new PrescriptionMedicineDAO();
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testPrescriptionMedicineFromJson() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/prescriptionMedicine.json");
        assertNotNull(is);
        PrescriptionMedicine pm = mapper.readValue(is, PrescriptionMedicine.class);
        assertNotNull(pm);
        assertEquals(2.5, pm.getDosis());
        assertEquals(3.0, pm.getFrecuencia());
        assertEquals(10.0, pm.getDuracion());
    }

    @Test
    public void testCreatePrescriptionMedicine() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/prescriptionMedicine.json");
        assertNotNull(is);
        PrescriptionMedicine pmJson = mapper.readValue(is, PrescriptionMedicine.class);
        Prescription prescription = new Prescription();
        Medicine medicine = new Medicine();
        PrescriptionMedicine created = dao.create(prescription, medicine, pmJson.getDosis(), pmJson.getFrecuencia(), pmJson.getDuracion());
        assertNotNull(created);
        assertNotNull(created.getPrescription());
        assertNotNull(created.getMedicine());
        assertEquals(pmJson.getDosis(), created.getDosis());
        assertEquals(pmJson.getFrecuencia(), created.getFrecuencia());
        assertEquals(pmJson.getDuracion(), created.getDuracion());
    }

    @Test
    public void testUpdateAndGetAll() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/prescriptionMedicine.json");
        assertNotNull(is);
        PrescriptionMedicine pmJson = mapper.readValue(is, PrescriptionMedicine.class);
        Prescription prescription = new Prescription();
        Medicine medicine = new Medicine();
        PrescriptionMedicine created = dao.create(prescription, medicine, pmJson.getDosis(), pmJson.getFrecuencia(), pmJson.getDuracion());
        assertNotNull(created);
        // update a no-op change by setting same values; exercises DAO.update path
        created.setDuracion(created.getDuracion());
        PrescriptionMedicine updated = dao.update(created);
        assertNotNull(updated);
        // getAll should return non-empty list
        assertTrue(dao.getAll() != null && !dao.getAll().isEmpty());
    }
}