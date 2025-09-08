package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Bill;
import com.sources.app.entities.BillMedicine;
import com.sources.app.entities.Medicine;

public class BillMedicineDAOTest {

    private BillMedicineDAO billMedicineDAO = new BillMedicineDAO();
    private ObjectMapper mapper = new ObjectMapper();


    @Test
    public void testBillMedicineFromJsonFile() throws Exception {
        // Se carga el archivo desde el classpath (ruta absoluta)
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/billMedicine.json");
        assertNotNull(is, "No se encontró el archivo billMedicine.json");

        // Deserializar el JSON en un objeto BillMedicine
        BillMedicine billMedicineFromJson = mapper.readValue(is, BillMedicine.class);
        assertNotNull(billMedicineFromJson, "El objeto BillMedicine deserializado no debe ser nulo");
        assertEquals("110.0", billMedicineFromJson.getTotal(), "El campo total debe ser '110.0'");
    }


    @Test
    public void testCreateBillMedicineFromJson() throws Exception {
        // Cargar y deserializar el archivo JSON
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/billMedicine.json");
        assertNotNull(is, "No se encontró el archivo billMedicine.json");
        BillMedicine billMedicineFromJson = mapper.readValue(is, BillMedicine.class);

        // Crear objetos dummy para Bill y Medicine (ajusta según la lógica de negocio)
        Bill bill = new Bill();           // Se asume que cuenta con un constructor por defecto
        Medicine medicine = new Medicine(); // Se asume que cuenta con un constructor por defecto

        // Usar los datos del JSON para crear un BillMedicine mediante el DAO
        BillMedicine createdBillMedicine = billMedicineDAO.create(
                bill,
                medicine,
                billMedicineFromJson.getQuantity(),
                billMedicineFromJson.getCost(),
                billMedicineFromJson.getCopay(),
                billMedicineFromJson.getTotal()
        );

        assertNotNull(createdBillMedicine, "El BillMedicine creado no debe ser nulo");
        assertEquals(billMedicineFromJson.getTotal(), createdBillMedicine.getTotal(), "El campo total debe coincidir");
        assertEquals(billMedicineFromJson.getQuantity(), createdBillMedicine.getQuantity(), "La cantidad debe coincidir");
    }

    @Test
    public void testUpdateAndGetAll() throws Exception {
        // Arrange
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/billMedicine.json");
        assertNotNull(is, "No se encontró el archivo billMedicine.json");
        BillMedicine bmJson = mapper.readValue(is, BillMedicine.class);
        Bill bill = new Bill();
        Medicine medicine = new Medicine();
        BillMedicine created = billMedicineDAO.create(
                bill,
                medicine,
                bmJson.getQuantity(),
                bmJson.getCost(),
                bmJson.getCopay(),
                bmJson.getTotal()
        );
        assertNotNull(created);
        // Act - exercise update path (e.g., set same values)
        created.setQuantity(created.getQuantity());
        BillMedicine updated = billMedicineDAO.update(created);
        // Assert
        assertNotNull(updated);
        assertTrue(billMedicineDAO.getAll() != null && !billMedicineDAO.getAll().isEmpty());
    }
}