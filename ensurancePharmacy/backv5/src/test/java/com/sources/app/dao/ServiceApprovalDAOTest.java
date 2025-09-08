package com.sources.app.dao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.sources.app.entities.Hospital;
import com.sources.app.entities.ServiceApproval;
import com.sources.app.entities.User;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServiceApprovalDAOTest {

    @Test
    public void testServiceApprovalDAOInstantiation() {
        // TODO: implement tests for ServiceApprovalDAO
        ServiceApprovalDAO instance = new ServiceApprovalDAO();
        assertNotNull(instance);
    }

    private User createUser(String email) {
        UserDAO userDAO = new UserDAO();
        return userDAO.create(
                "Test User",
                "1234567890101",
                "555-0000",
                email,
                new Date(),
                "Street 1",
                "secret"
        );
    }

    private Hospital createHospital() {
        HospitalDAO hospitalDAO = new HospitalDAO();
        return hospitalDAO.create(
                "General Hospital",
                "555-1111",
                "h@h.com",
                "Ave 2",
                '1'
        );
    }

    private ServiceApproval createApproval(User u, Hospital h, String nameSuffix) {
        ServiceApprovalDAO dao = new ServiceApprovalDAO();
        return dao.create(
                u,
                h,
                "SRV-" + nameSuffix,
                "Servicio " + nameSuffix,
                "Desc",
                1000.0,
                700.0,
                300.0,
                "PENDING"
        );
    }

    @Test
    public void testCreateAndFetchByIdAndCode() {
        String email = "user_" + System.currentTimeMillis() + "@test.com";
        User u = createUser(email);
        Hospital h = createHospital();
        assertNotNull(u);
        assertNotNull(h);

        ServiceApprovalDAO dao = new ServiceApprovalDAO();
        ServiceApproval saved = createApproval(u, h, "A");
        assertNotNull(saved);
        assertNotNull(saved.getIdApproval());
        assertNotNull(saved.getApprovalCode());
        assertTrue(saved.getApprovalCode().matches("AP[0-9A-F]{8}"));
        assertEquals("PENDING", saved.getStatus());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getApprovalDate());

        ServiceApproval byId = dao.getById(saved.getIdApproval());
        assertNotNull(byId);
        assertEquals(saved.getIdApproval(), byId.getIdApproval());

        ServiceApproval byCode = dao.getByApprovalCode(saved.getApprovalCode());
        assertNotNull(byCode);
        assertEquals(saved.getApprovalCode(), byCode.getApprovalCode());
    }

    @Test
    public void testUpdatePrescription() {
        String email = "user_" + System.nanoTime() + "@test.com";
        User u = createUser(email);
        Hospital h = createHospital();
        ServiceApprovalDAO dao = new ServiceApprovalDAO();
        ServiceApproval saved = createApproval(u, h, "B");

        ServiceApproval updated = dao.updatePrescription(saved.getIdApproval(), "RX-123", 123.45);
        assertNotNull(updated);
        assertEquals("RX-123", updated.getPrescriptionId());
        assertEquals(123.45, updated.getPrescriptionTotal());
    }

    @Test
    public void testUpdateStatusRejectedAndCompleted() throws Exception {
        String email = "user_" + (System.currentTimeMillis() + 1) + "@test.com";
        User u = createUser(email);
        Hospital h = createHospital();
        ServiceApprovalDAO dao = new ServiceApprovalDAO();
        ServiceApproval saved = createApproval(u, h, "C");

        ServiceApproval rejected = dao.updateStatus(saved.getIdApproval(), "REJECTED", "datos incompletos");
        assertNotNull(rejected);
        assertEquals("REJECTED", rejected.getStatus());
        assertEquals("datos incompletos", rejected.getRejectionReason());
        assertNull(rejected.getCompletedDate());

        // Pequeña espera para asegurar timestamps distintos si el proveedor lo soporta
        TimeUnit.MILLISECONDS.sleep(5);

        ServiceApproval completed = dao.updateStatus(saved.getIdApproval(), "COMPLETED", null);
        assertNotNull(completed);
        assertEquals("COMPLETED", completed.getStatus());
        assertNotNull(completed.getCompletedDate());
    }

    @Test
    public void testQueriesByUserHospitalAndAll() throws Exception {
        String email = "user_" + (System.currentTimeMillis() + 2) + "@test.com";
        User u = createUser(email);
        Hospital h = createHospital();
        ServiceApprovalDAO dao = new ServiceApprovalDAO();

        createApproval(u, h, "D1");
        TimeUnit.MILLISECONDS.sleep(2);
        createApproval(u, h, "D2");

        List<ServiceApproval> byUser = dao.getByUser(u.getIdUser());
        assertNotNull(byUser);
        assertTrue(byUser.size() >= 2);

        List<ServiceApproval> byHospital = dao.getByHospital(h.getIdHospital());
        assertNotNull(byHospital);
        assertTrue(byHospital.size() >= 2);

        List<ServiceApproval> all = dao.getAll();
        assertNotNull(all);
        assertTrue(all.size() >= 2);

        // Validar orden descendente aproximado (si es posible)
        if (byUser.size() >= 2 && byUser.get(0).getCreatedAt() != null && byUser.get(1).getCreatedAt() != null) {
            assertTrue(byUser.get(0).getCreatedAt().compareTo(byUser.get(1).getCreatedAt()) >= 0);
        }
    }

    @Test
    public void testCreateFailsOnNullRequiredFields() {
        // Build a request with null user and hospital to violate NOT NULL constraints
        ServiceApprovalDAO dao = new ServiceApprovalDAO();
        com.sources.app.dto.ServiceApprovalCreateRequest req = new com.sources.app.dto.ServiceApprovalCreateRequest();
        req.setUser(null);
        req.setHospital(null);
        req.setServiceId("SRV-NULL");
        req.setServiceName("Servicio Null");
        req.setServiceDescription("Desc");
        req.setServiceCost(100.0);
        req.setCoveredAmount(50.0);
        req.setPatientAmount(50.0);
        req.setStatus("PENDING");
        try {
            ServiceApproval created = dao.create(req);
            assertNull(created, "DAO.create should return null when constraints are violated and rollback occurs");
        } catch (Exception e) {
            // Some providers (SQLite + Hibernate) may close the connection on failed commit/rollback.
            // Accept this as an expected negative-path outcome.
            String msg = e.getMessage() == null ? "" : e.getMessage();
            assertTrue(e instanceof IllegalStateException || msg.toLowerCase().contains("constraint") || msg.toLowerCase().contains("closed"));
        }
    }

    @Test
    public void testUpdatePrescription_NotFound_ReturnsNull() {
        ServiceApprovalDAO dao = new ServiceApprovalDAO();
        // Use a very large ID unlikely to exist in test DB
        ServiceApproval updated = dao.updatePrescription(999_999_999L, "RX-NONE", 12.34);
        assertNull(updated);
    }

    @Test
    public void testUpdateStatus_NotFound_ReturnsNull() {
        ServiceApprovalDAO dao = new ServiceApprovalDAO();
        ServiceApproval updated = dao.updateStatus(888_888_888L, "APPROVED", null);
        assertNull(updated);
    }

    @Test
    public void testUpdateStatus_SpanishVariants() {
        String email = "user_" + (System.currentTimeMillis() + 3) + "@test.com";
        User u = createUser(email);
        Hospital h = createHospital();
        ServiceApprovalDAO dao = new ServiceApprovalDAO();
        ServiceApproval saved = createApproval(u, h, "ES");

        // RECHAZADO -> sets rejection reason
        ServiceApproval rechazado = dao.updateStatus(saved.getIdApproval(), "RECHAZADO", "falta de datos");
        assertNotNull(rechazado);
        assertEquals("RECHAZADO", rechazado.getStatus());
        assertEquals("falta de datos", rechazado.getRejectionReason());
        assertNull(rechazado.getCompletedDate());

        // COMPLETADO -> sets completed date
        ServiceApproval completado = dao.updateStatus(saved.getIdApproval(), "COMPLETADO", null);
        assertNotNull(completado);
        assertEquals("COMPLETADO", completado.getStatus());
        assertNotNull(completado.getCompletedDate());
    }

    @Test
    public void testUpdateStatus_NullStatusTriggersRollback() {
        String email = "user_" + (System.currentTimeMillis() + 4) + "@test.com";
        User u = createUser(email);
        Hospital h = createHospital();
        ServiceApprovalDAO dao = new ServiceApprovalDAO();
        ServiceApproval saved = createApproval(u, h, "NULLS");

        // Setting null to a NOT NULL column may trigger provider-specific exceptions on rollback
        try {
            ServiceApproval result = dao.updateStatus(saved.getIdApproval(), null, null);
            assertNull(result);
        } catch (Exception e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            assertTrue(e instanceof IllegalStateException || msg.toLowerCase().contains("constraint") || msg.toLowerCase().contains("closed"));
        }
    }

    @Test
    public void testGetByApprovalCode_NotFoundReturnsNull() {
        ServiceApprovalDAO dao = new ServiceApprovalDAO();
        ServiceApproval byCode = dao.getByApprovalCode("AP00000000");
        assertNull(byCode);
    }
}
