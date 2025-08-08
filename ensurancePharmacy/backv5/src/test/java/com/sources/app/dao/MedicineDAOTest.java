package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Medicine;

public class MedicineDAOTest {
    private MedicineDAO medicineDAO = new MedicineDAO();
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testMedicineFromJsonFile() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/medicine.json");
        assertNotNull(is);
        Medicine medFromJson = mapper.readValue(is, Medicine.class);
        assertNotNull(medFromJson);
        assertEquals("Test Medicine", medFromJson.getName());
    }

    @Test
    public void testCreateMedicineFromJson() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/medicine.json");
        assertNotNull(is);
        Medicine medFromJson = mapper.readValue(is, Medicine.class);
        Medicine createdMed = medicineDAO.create(
                medFromJson.getName(),
                medFromJson.getActiveMedicament(),
                medFromJson.getDescription(),
                medFromJson.getImage(),
                medFromJson.getConcentration(),
                medFromJson.getPresentacion(),
                medFromJson.getStock(),
                medFromJson.getBrand(),
                medFromJson.getPrescription(),
                medFromJson.getPrice(),
                medFromJson.getSoldUnits()
        );
        assertNotNull(createdMed);
        assertNotNull(createdMed.getIdMedicine());
        assertEquals(medFromJson.getName(), createdMed.getName());
    }
}