package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Bill;
import com.sources.app.entities.Prescription;

public class BillDAOTest {

    private BillDAO billDAO = new BillDAO();
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Prueba que deserializa un objeto Bill desde el archivo JSON externo.
     */
    @Test
    public void testBillFromJsonFile() throws Exception {
        // Cargar el archivo bill.json desde el mismo paquete (se espera que esté en el classpath)
        InputStream is = getClass().getResourceAsStream("bill.json");
        assertNotNull(is, "No se encontró el archivo bill.json");

        // Deserializar el JSON en un objeto Bill
        Bill billFromJson = mapper.readValue(is, Bill.class);
        assertNotNull(billFromJson, "El objeto Bill deserializado no debe ser nulo");
        assertEquals("125.0", billFromJson.getTotal(), "El campo 'total' debe coincidir con el valor esperado");
    }

    /**
     * Prueba la creación de un Bill utilizando datos del JSON.
     */
    @Test
    public void testCreateBillFromJson() throws Exception {
        // Cargar y deserializar el archivo bill.json
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/bill.json");
        assertNotNull(is, "No se encontró el archivo bill.json");
        Bill billFromJson = mapper.readValue(is, Bill.class);

        // Crear un objeto dummy de Prescription para la prueba
        Prescription prescription = new Prescription();

        // Usar los datos del JSON para crear un Bill a través del DAO
        Bill createdBill = billDAO.create(prescription, billFromJson.getTaxes(), billFromJson.getSubtotal(), billFromJson.getCopay(), billFromJson.getTotal());
        assertNotNull(createdBill, "El Bill creado no debe ser nulo");
        assertNotNull(createdBill.getIdBill(), "El ID del Bill debe ser generado");
        assertEquals("125.0", createdBill.getTotal(), "El campo 'total' debe coincidir con el valor esperado");
    }
}