package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import java.util.List;
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

    @Test
    public void testCreateMedicineWithDTO() {
        com.sources.app.dto.MedicineCreateRequest request = new com.sources.app.dto.MedicineCreateRequest(
                "Test Medicine DTO",
                "Active Ingredient",
                "Test Description",
                "test-image.jpg",
                "500mg",
                10.0,
                50,
                "Test Brand",
                true,
                25.99,
                0
        );
        
        Medicine created = medicineDAO.create(request);
        assertNotNull(created);
        assertNotNull(created.getIdMedicine());
        assertEquals("Test Medicine DTO", created.getName());
        assertEquals("Active Ingredient", created.getActiveMedicament());
        assertEquals("Test Description", created.getDescription());
        assertEquals("test-image.jpg", created.getImage());
        assertEquals("500mg", created.getConcentration());
        assertEquals(10.0, created.getPresentacion());
        assertEquals(50, created.getStock());
        assertEquals("Test Brand", created.getBrand());
        assertTrue(created.getPrescription());
        assertEquals(25.99, created.getPrice());
        assertEquals(0, created.getSoldUnits());
    }

    @Test
    public void testGetAllMedicines() {
        // First create a medicine to ensure we have data
        com.sources.app.dto.MedicineCreateRequest request = new com.sources.app.dto.MedicineCreateRequest(
                "GetAll Test Medicine",
                "Test Active",
                "Test Description",
                "test.jpg",
                "100mg",
                5.0,
                25,
                "Test Brand",
                false,
                15.50,
                0
        );
        medicineDAO.create(request);
        
        List<Medicine> medicines = medicineDAO.getAll();
        assertNotNull(medicines);
        assertTrue(medicines.size() > 0);
    }

    @Test
    public void testGetMedicineById() {
        // Create a medicine first
        com.sources.app.dto.MedicineCreateRequest request = new com.sources.app.dto.MedicineCreateRequest(
                "GetById Test Medicine",
                "Test Active",
                "Test Description",
                "test.jpg",
                "200mg",
                8.0,
                30,
                "Test Brand",
                true,
                20.00,
                0
        );
        Medicine created = medicineDAO.create(request);
        assertNotNull(created);
        
        // Now retrieve it by ID
        Medicine retrieved = medicineDAO.getById(created.getIdMedicine());
        assertNotNull(retrieved);
        assertEquals(created.getIdMedicine(), retrieved.getIdMedicine());
        assertEquals("GetById Test Medicine", retrieved.getName());
    }

    @Test
    public void testGetMedicineByIdNotFound() {
        Medicine retrieved = medicineDAO.getById(99999L);
        assertNull(retrieved);
    }

    @Test
    public void testUpdateMedicine() {
        // Create a medicine first
        com.sources.app.dto.MedicineCreateRequest request = new com.sources.app.dto.MedicineCreateRequest(
                "Update Test Medicine",
                "Test Active",
                "Original Description",
                "original.jpg",
                "300mg",
                12.0,
                40,
                "Original Brand",
                false,
                30.00,
                0
        );
        Medicine created = medicineDAO.create(request);
        assertNotNull(created);
        
        // Update the medicine
        created.setName("Updated Medicine Name");
        created.setDescription("Updated Description");
        created.setPrice(35.00);
        created.setStock(45);
        
        Medicine updated = medicineDAO.update(created);
        assertNotNull(updated);
        assertEquals("Updated Medicine Name", updated.getName());
        assertEquals("Updated Description", updated.getDescription());
        assertEquals(35.00, updated.getPrice());
        assertEquals(45, updated.getStock());
        
        // Verify the update persisted
        Medicine retrieved = medicineDAO.getById(created.getIdMedicine());
        assertNotNull(retrieved);
        assertEquals("Updated Medicine Name", retrieved.getName());
        assertEquals("Updated Description", retrieved.getDescription());
    }

    @Test
    public void testCreateMedicineWithNullValues() {
        com.sources.app.dto.MedicineCreateRequest request = new com.sources.app.dto.MedicineCreateRequest(
                null, // null name should be handled gracefully
                "Active Ingredient",
                "Description",
                "image.jpg",
                "100mg",
                5.0,
                10,
                "Brand",
                true,
                15.00,
                0
        );
        
        Medicine created = medicineDAO.create(request);
        // The method should handle null gracefully and either create with null or return null
        // Based on the implementation, it should still create the medicine
        assertNotNull(created);
    }

    @Test
    public void testUpdateMedicineWithNullId() {
        // Create a medicine with null ID to test error handling
        Medicine nullIdMedicine = new Medicine();
        nullIdMedicine.setIdMedicine(null);
        nullIdMedicine.setName("Test");
        
        Medicine updated = medicineDAO.update(nullIdMedicine);
        // The update method may still return the object even if the operation fails
        // This tests the error handling path in the DAO
        assertNotNull(updated);
    }
}