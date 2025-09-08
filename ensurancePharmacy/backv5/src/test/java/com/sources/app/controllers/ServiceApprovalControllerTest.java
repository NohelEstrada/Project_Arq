package com.sources.app.controllers;

import com.sources.app.dao.HospitalDAO;
import com.sources.app.dao.ServiceApprovalDAO;
import com.sources.app.dao.SystemConfigDAO;
import com.sources.app.dao.UserDAO;
import com.sources.app.entities.Hospital;
import com.sources.app.entities.Policy;
import com.sources.app.entities.ServiceApproval;
import com.sources.app.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class ServiceApprovalControllerTest {

    private ServiceApprovalController controller;
    private ServiceApprovalDAO serviceApprovalDAO;
    private UserDAO userDAO;
    private HospitalDAO hospitalDAO;
    private SystemConfigDAO systemConfigDAO;

    @BeforeEach
    void setUp() throws Exception {
        controller = new ServiceApprovalController();
        serviceApprovalDAO = Mockito.mock(ServiceApprovalDAO.class);
        userDAO = Mockito.mock(UserDAO.class);
        hospitalDAO = Mockito.mock(HospitalDAO.class);
        systemConfigDAO = Mockito.mock(SystemConfigDAO.class);

        injectPrivateField(controller, "serviceApprovalDAO", serviceApprovalDAO);
        injectPrivateField(controller, "userDAO", userDAO);
        injectPrivateField(controller, "hospitalDAO", hospitalDAO);
        injectPrivateField(controller, "systemConfigDAO", systemConfigDAO);
    }

    @Test
    void testRequestPrescriptionApprovalSuccess() {
        String json = "{\"approvalCode\":\"AP2\",\"prescriptionId\":\"RX-200\",\"prescriptionTotal\":500}";

        ServiceApproval approval = new ServiceApproval();
        approval.setIdApproval(7L);
        approval.setApprovalCode("AP2");
        approval.setStatus("APPROVED");

        when(serviceApprovalDAO.getByApprovalCode("AP2")).thenReturn(approval);
        when(systemConfigDAO.getConfigValueAsDouble(eq("MIN_PRESCRIPTION_AMOUNT"), anyDouble())).thenReturn(250.0);

        ServiceApproval updated = new ServiceApproval();
        updated.setIdApproval(7L);
        updated.setApprovalCode("AP2");
        updated.setStatus("APPROVED");
        updated.setPrescriptionId("RX-200");
        updated.setPrescriptionTotal(500.0);
        when(serviceApprovalDAO.updatePrescription(7L, "RX-200", 500.0)).thenReturn(updated);

        Response resp = controller.requestPrescriptionApproval(json);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Map<?, ?> map = castToMap(resp.getEntity());
        assertEquals(true, map.get("success"));
        assertEquals("AP2", map.get("approvalCode"));
        assertEquals("RX-200", map.get("prescriptionId"));
        assertEquals(500.0, (double) (Double) map.get("prescriptionTotal"), 0.0001);
        assertEquals("APPROVED", map.get("status"));
    }

    @Test
    void testCompleteApprovalSuccess() {
        ServiceApproval approval = new ServiceApproval();
        approval.setIdApproval(11L);
        approval.setApprovalCode("APCOMP");
        approval.setStatus("APPROVED");
        when(serviceApprovalDAO.getByApprovalCode("APCOMP")).thenReturn(approval);

        ServiceApproval completed = new ServiceApproval();
        completed.setIdApproval(11L);
        completed.setApprovalCode("APCOMP");
        completed.setStatus("COMPLETED");
        completed.setCompletedDate(new Date());
        when(serviceApprovalDAO.updateStatus(11L, "COMPLETED", null)).thenReturn(completed);

        Response resp = controller.completeApproval("APCOMP");
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Map<?, ?> map = castToMap(resp.getEntity());
        assertEquals(true, map.get("success"));
        assertEquals("COMPLETED", map.get("status"));
        assertNotNull(map.get("completedDate"));
    }

    @Test
    void testRequestServiceApprovalSuccess() {
        // Arrange datos de entrada
        String json = "{\"userId\":1,\"hospitalId\":10,\"serviceId\":\"SVC-1\",\"serviceName\":\"Rayos X\",\"serviceDescription\":\"Desc\",\"serviceCost\":1000}";

        // Usuario con póliza válida (80%) y activo
        User user = new User();
        Policy policy = new Policy();
        policy.setPercentage(80.0);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 30);
        policy.setExpirationDate(cal.getTime());
        user.setPolicy(policy);
        user.setStatus("ACTIVE");

        Hospital hospital = new Hospital();

        when(userDAO.getById(1L)).thenReturn(user);
        when(hospitalDAO.getById(10L)).thenReturn(hospital);

        ServiceApproval created = new ServiceApproval();
        created.setIdApproval(99L);
        created.setApprovalCode("APCODE123");
        created.setApprovalDate(new Date());
        when(serviceApprovalDAO.create(eq(user), eq(hospital), eq("SVC-1"), eq("Rayos X"), eq("Desc"), eq(1000.0), anyDouble(), anyDouble(), eq("APPROVED")))
                .thenReturn(created);

        // Act
        Response resp = controller.requestServiceApproval(json);

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Object entity = resp.getEntity();
        assertTrue(entity instanceof Map);
        Map<?, ?> map = (Map<?, ?>) entity;
        assertEquals(true, map.get("success"));
        assertEquals(99L, map.get("approvalId"));
        assertEquals("APCODE123", map.get("approvalCode"));
        assertEquals(1000.0, (double) (Double) map.get("serviceCost"), 0.0001);

        // Cobertura 80% de 1000 = 800, paciente 200
        assertEquals(800.0, (double) (Double) map.get("coveredAmount"), 0.0001);
        assertEquals(200.0, (double) (Double) map.get("patientAmount"), 0.0001);
    }

    @Test
    void testRequestServiceApprovalUserNotFound() {
        String json = "{\"userId\":2,\"hospitalId\":10,\"serviceId\":\"SVC\",\"serviceName\":\"N\",\"serviceDescription\":\"D\",\"serviceCost\":100}";
        when(userDAO.getById(2L)).thenReturn(null);

        Response resp = controller.requestServiceApproval(json);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
        Map<?, ?> map = castToMap(resp.getEntity());
        assertEquals(false, map.get("success"));
        assertTrue(((String) map.get("message")).toLowerCase().contains("usuario"));
    }

    @Test
    void testRequestPrescriptionApprovalRejectBelowMin() {
        String json = "{\"approvalCode\":\"AP1\",\"prescriptionId\":\"RX\",\"prescriptionTotal\":100}";

        ServiceApproval approval = new ServiceApproval();
        approval.setIdApproval(5L);
        approval.setApprovalCode("AP1");
        approval.setStatus("APPROVED");

        when(serviceApprovalDAO.getByApprovalCode("AP1")).thenReturn(approval);
        when(systemConfigDAO.getConfigValueAsDouble(eq("MIN_PRESCRIPTION_AMOUNT"), anyDouble())).thenReturn(250.0);

        ServiceApproval updated = new ServiceApproval();
        updated.setIdApproval(5L);
        updated.setApprovalCode("AP1");
        updated.setStatus("REJECTED");
        updated.setRejectionReason("El monto de la receta es menor al mínimo");
        when(serviceApprovalDAO.updateStatus(eq(5L), eq("REJECTED"), anyString())).thenReturn(updated);

        Response resp = controller.requestPrescriptionApproval(json);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Map<?, ?> map = castToMap(resp.getEntity());
        assertEquals(false, map.get("success"));
        assertEquals("REJECTED", map.get("status"));
        assertNotNull(map.get("rejectionReason"));
    }

    @Test
    void testCheckApprovalStatusNotFound() {
        when(serviceApprovalDAO.getByApprovalCode("NOPE")).thenReturn(null);
        Response resp = controller.checkApprovalStatus("NOPE");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), resp.getStatus());
        Map<?, ?> map = castToMap(resp.getEntity());
        assertEquals(false, map.get("success"));
    }

    @Test
    void testCompleteApprovalNotApproved() {
        ServiceApproval approval = new ServiceApproval();
        approval.setStatus("PENDING");
        when(serviceApprovalDAO.getByApprovalCode("APZ")).thenReturn(approval);

        Response resp = controller.completeApproval("APZ");
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
        Map<?, ?> map = castToMap(resp.getEntity());
        assertEquals(false, map.get("success"));
    }

    @Test
    void testRequestServiceApprovalHospitalNotFound() {
        String json = "{\"userId\":1,\"hospitalId\":99,\"serviceId\":\"S\",\"serviceName\":\"N\",\"serviceDescription\":\"D\",\"serviceCost\":100}";

        User user = new User();
        Policy policy = new Policy();
        policy.setPercentage(50.0);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 10);
        policy.setExpirationDate(cal.getTime());
        user.setPolicy(policy);
        user.setStatus("ACTIVE");

        when(userDAO.getById(1L)).thenReturn(user);
        when(hospitalDAO.getById(99L)).thenReturn(null);

        Response resp = controller.requestServiceApproval(json);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
        Map<?, ?> map = castToMap(resp.getEntity());
        assertEquals(false, map.get("success"));
    }

    @Test
    void testRequestServiceApprovalNoPolicy() {
        String json = "{\"userId\":1,\"hospitalId\":10,\"serviceId\":\"S\",\"serviceName\":\"N\",\"serviceDescription\":\"D\",\"serviceCost\":100}";

        User user = new User();
        user.setPolicy(null);
        user.setStatus("ACTIVE");

        when(userDAO.getById(1L)).thenReturn(user);
        when(hospitalDAO.getById(10L)).thenReturn(new Hospital());

        Response resp = controller.requestServiceApproval(json);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
    }

    @Test
    void testRequestServiceApprovalPolicyExpired() {
        String json = "{\"userId\":1,\"hospitalId\":10,\"serviceId\":\"S\",\"serviceName\":\"N\",\"serviceDescription\":\"D\",\"serviceCost\":100}";

        User user = new User();
        Policy policy = new Policy();
        policy.setPercentage(80.0);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        policy.setExpirationDate(cal.getTime());
        user.setPolicy(policy);
        user.setStatus("ACTIVE");

        when(userDAO.getById(1L)).thenReturn(user);
        when(hospitalDAO.getById(10L)).thenReturn(new Hospital());

        Response resp = controller.requestServiceApproval(json);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
    }

    @Test
    void testRequestServiceApprovalPendingPayment() {
        String json = "{\"userId\":1,\"hospitalId\":10,\"serviceId\":\"S\",\"serviceName\":\"N\",\"serviceDescription\":\"D\",\"serviceCost\":100}";

        User user = new User();
        Policy policy = new Policy();
        policy.setPercentage(80.0);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 5);
        policy.setExpirationDate(cal.getTime());
        user.setPolicy(policy);
        user.setStatus("PENDING_PAYMENT");

        when(userDAO.getById(1L)).thenReturn(user);
        when(hospitalDAO.getById(10L)).thenReturn(new Hospital());

        Response resp = controller.requestServiceApproval(json);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
    }

    @Test
    void testRequestServiceApprovalCreateReturnsNull() {
        String json = "{\"userId\":1,\"hospitalId\":10,\"serviceId\":\"S\",\"serviceName\":\"N\",\"serviceDescription\":\"D\",\"serviceCost\":100}";

        User user = new User();
        Policy policy = new Policy();
        policy.setPercentage(70.0);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 10);
        policy.setExpirationDate(cal.getTime());
        user.setPolicy(policy);
        user.setStatus("ACTIVE");

        when(userDAO.getById(1L)).thenReturn(user);
        when(hospitalDAO.getById(10L)).thenReturn(new Hospital());
        when(serviceApprovalDAO.create(any(), any(), anyString(), anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(), anyString()))
                .thenReturn(null);

        Response resp = controller.requestServiceApproval(json);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), resp.getStatus());
    }

    @Test
    void testRequestServiceApprovalBadJson() {
        Response resp = controller.requestServiceApproval("not-a-json");
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), resp.getStatus());
    }

    @Test
    void testRequestPrescriptionApprovalInvalidCode() {
        String json = "{\"approvalCode\":\"NO\",\"prescriptionId\":\"RX\",\"prescriptionTotal\":500}";
        when(serviceApprovalDAO.getByApprovalCode("NO")).thenReturn(null);
        Response resp = controller.requestPrescriptionApproval(json);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
    }

    @Test
    void testRequestPrescriptionApprovalWrongStatus() {
        String json = "{\"approvalCode\":\"AP3\",\"prescriptionId\":\"RX\",\"prescriptionTotal\":500}";
        ServiceApproval approval = new ServiceApproval();
        approval.setStatus("PENDING");
        when(serviceApprovalDAO.getByApprovalCode("AP3")).thenReturn(approval);
        Response resp = controller.requestPrescriptionApproval(json);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
    }

    @Test
    void testRequestPrescriptionApprovalUpdateReturnsNull() {
        String json = "{\"approvalCode\":\"AP4\",\"prescriptionId\":\"RX\",\"prescriptionTotal\":500}";
        ServiceApproval approval = new ServiceApproval();
        approval.setIdApproval(4L);
        approval.setApprovalCode("AP4");
        approval.setStatus("APPROVED");
        when(serviceApprovalDAO.getByApprovalCode("AP4")).thenReturn(approval);
        when(systemConfigDAO.getConfigValueAsDouble(eq("MIN_PRESCRIPTION_AMOUNT"), anyDouble())).thenReturn(100.0);
        when(serviceApprovalDAO.updatePrescription(4L, "RX", 500.0)).thenReturn(null);
        Response resp = controller.requestPrescriptionApproval(json);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), resp.getStatus());
    }

    @Test
    void testRequestPrescriptionApprovalBadJson() {
        Response resp = controller.requestPrescriptionApproval("{broken json}");
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), resp.getStatus());
    }

    @Test
    void testCheckApprovalStatusSuccess_WithPrescriptionAndRejected() {
        ServiceApproval approval = new ServiceApproval();
        approval.setIdApproval(12L);
        approval.setApprovalCode("APOK");
        approval.setServiceName("Consulta");
        approval.setServiceDescription("General");
        approval.setServiceCost(200.0);
        approval.setCoveredAmount(150.0);
        approval.setPatientAmount(50.0);
        approval.setPrescriptionId("RX-900");
        approval.setPrescriptionTotal(200.0);
        approval.setStatus("REJECTED");
        approval.setRejectionReason("Motivo X");
        when(serviceApprovalDAO.getByApprovalCode("APOK")).thenReturn(approval);

        Response resp = controller.checkApprovalStatus("APOK");
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Map<?, ?> map = castToMap(resp.getEntity());
        assertEquals(true, map.get("success"));
        assertEquals("Consulta", map.get("serviceName"));
        assertEquals("General", map.get("serviceDescription"));
        assertEquals(200.0, (double) (Double) map.get("serviceCost"), 0.0001);
        assertEquals(150.0, (double) (Double) map.get("coveredAmount"), 0.0001);
        assertEquals(50.0, (double) (Double) map.get("patientAmount"), 0.0001);
        assertEquals("RX-900", map.get("prescriptionId"));
        assertEquals(200.0, (double) (Double) map.get("prescriptionTotal"), 0.0001);
        assertEquals("REJECTED", map.get("status"));
        assertEquals("Motivo X", map.get("rejectionReason"));
    }

    @Test
    void testCompleteApprovalNotFound() {
        when(serviceApprovalDAO.getByApprovalCode("NONE")).thenReturn(null);
        Response resp = controller.completeApproval("NONE");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), resp.getStatus());
    }

    @Test
    void testCheckApprovalStatusSuccess_NoPrescription_NotRejected() {
        ServiceApproval approval = new ServiceApproval();
        approval.setIdApproval(13L);
        approval.setApprovalCode("APOK2");
        approval.setServiceName("Ultrasonido");
        approval.setServiceDescription("Abdominal");
        approval.setServiceCost(300.0);
        approval.setCoveredAmount(200.0);
        approval.setPatientAmount(100.0);
        approval.setStatus("APPROVED");
        when(serviceApprovalDAO.getByApprovalCode("APOK2")).thenReturn(approval);

        Response resp = controller.checkApprovalStatus("APOK2");
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Map<?, ?> map = castToMap(resp.getEntity());
        assertEquals(true, map.get("success"));
        assertEquals("Ultrasonido", map.get("serviceName"));
        assertEquals("Abdominal", map.get("serviceDescription"));
        assertEquals(300.0, (double) (Double) map.get("serviceCost"), 0.0001);
        assertEquals(200.0, (double) (Double) map.get("coveredAmount"), 0.0001);
        assertEquals(100.0, (double) (Double) map.get("patientAmount"), 0.0001);
        assertEquals("APPROVED", map.get("status"));
        assertFalse(map.containsKey("prescriptionId"));
        assertFalse(map.containsKey("rejectionReason"));
    }

    @Test
    void testRequestPrescriptionApprovalAtMinBoundary() {
        String json = "{\"approvalCode\":\"APMIN\",\"prescriptionId\":\"RX-250\",\"prescriptionTotal\":250}";

        ServiceApproval approval = new ServiceApproval();
        approval.setIdApproval(8L);
        approval.setApprovalCode("APMIN");
        approval.setStatus("APPROVED");
        when(serviceApprovalDAO.getByApprovalCode("APMIN")).thenReturn(approval);
        when(systemConfigDAO.getConfigValueAsDouble(eq("MIN_PRESCRIPTION_AMOUNT"), anyDouble())).thenReturn(250.0);

        ServiceApproval updated = new ServiceApproval();
        updated.setIdApproval(8L);
        updated.setApprovalCode("APMIN");
        updated.setStatus("APPROVED");
        updated.setPrescriptionId("RX-250");
        updated.setPrescriptionTotal(250.0);
        when(serviceApprovalDAO.updatePrescription(8L, "RX-250", 250.0)).thenReturn(updated);

        Response resp = controller.requestPrescriptionApproval(json);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Map<?, ?> map = castToMap(resp.getEntity());
        assertEquals(true, map.get("success"));
        assertEquals("APMIN", map.get("approvalCode"));
        assertEquals("RX-250", map.get("prescriptionId"));
        assertEquals(250.0, (double) (Double) map.get("prescriptionTotal"), 0.0001);
        assertEquals("APPROVED", map.get("status"));
    }

    // Helpers
    private static void injectPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private static Map<?, ?> castToMap(Object entity) {
        if (entity instanceof Map) {
            return (Map<?, ?>) entity;
        }
        if (entity instanceof HashMap) {
            return (HashMap<?, ?>) entity;
        }
        fail("Entidad de respuesta no es un Map");
        return null;
    }
}
