package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Bill;
import com.sources.app.entities.Prescription;
import com.sources.app.entities.Hospital;
import com.sources.app.entities.User;

public class BillDAOTest {

    private BillDAO billDAO = new BillDAO();
    private PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private HospitalDAO hospitalDAO = new HospitalDAO();
    private UserDAO userDAO = new UserDAO();
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

        // Preparar entidades relacionadas persistidas: Hospital, User y Prescription
        Hospital hospital = hospitalDAO.create("Hosp Test", "555-0000", "h@test.com", "Addr", '1');
        assertNotNull(hospital, "El hospital debe persistirse");
        User user = userDAO.create("User Test", "123", "555", "u@test.com", new java.util.Date(), "Addr", "pwd");
        assertNotNull(user, "El usuario debe persistirse");
        Prescription prescription = prescriptionDAO.create(hospital, user, 'Y');
        assertNotNull(prescription, "La prescripción debe persistirse");

        // Usar los datos del JSON para crear un Bill a través del DAO
        Bill createdBill = billDAO.create(prescription, billFromJson.getTaxes(), billFromJson.getSubtotal(), billFromJson.getCopay(), billFromJson.getTotal());
        assertNotNull(createdBill, "El Bill creado no debe ser nulo");
        assertNotNull(createdBill.getIdBill(), "El ID del Bill debe ser generado");
        assertEquals("125.0", createdBill.getTotal(), "El campo 'total' debe coincidir con el valor esperado");
    }

    @Test
    public void testGetAllAndLookupsIncreaseCountAndReturnCreated() {
        java.util.List<Bill> before = billDAO.getAll();
        int base = before == null ? 0 : before.size();

        Hospital hospital = hospitalDAO.create("Hosp A", "555-1111", "a@h.com", "Addr A", '1');
        assertNotNull(hospital);
        User user = userDAO.create("User A", "111", "111", "a@u.com", new java.util.Date(), "Addr A", "pwd");
        assertNotNull(user);
        Prescription p = prescriptionDAO.create(hospital, user, 'Y');
        assertNotNull(p);

        Bill b = billDAO.create(p, 12.5, 100.0, 5.0, "117.5");
        assertNotNull(b);

        java.util.List<Bill> after = billDAO.getAll();
        assertNotNull(after);
        assertTrue(after.size() >= base + 1, "Se esperaba que aumente el conteo después de crear un Bill");

        Bill byId = billDAO.getById(b.getIdBill());
        assertNotNull(byId);
        assertEquals("117.5", byId.getTotal());

        Bill byPrescription = billDAO.getByPrescriptionId(p.getIdPrescription());
        assertNotNull(byPrescription);
        assertEquals(b.getIdBill(), byPrescription.getIdBill());
    }

    @Test
    public void testUpdateBillPersistsChanges() {
        Hospital hospital = hospitalDAO.create("Hosp B", "555-2222", "b@h.com", "Addr B", '1');
        User user = userDAO.create("User B", "222", "222", "b@u.com", new java.util.Date(), "Addr B", "pwd");
        Prescription p = prescriptionDAO.create(hospital, user, 'Y');
        Bill b = billDAO.create(p, 10.0, 200.0, 0.0, "210.0");
        assertNotNull(b);

        b.setTaxes(15.0);
        b.setTotal("215.0");
        Bill updated = billDAO.update(b);
        assertNotNull(updated);
        assertEquals(15.0, updated.getTaxes());
        assertEquals("215.0", updated.getTotal());

        Bill reloaded = billDAO.getById(b.getIdBill());
        assertNotNull(reloaded);
        assertEquals(15.0, reloaded.getTaxes());
        assertEquals("215.0", reloaded.getTotal());
    }

    @Test
    public void testNegativeLookupsReturnNull() {
        assertNull(billDAO.getById(-1L));
        assertNull(billDAO.getByPrescriptionId(-1L));
    }
}

