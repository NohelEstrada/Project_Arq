package com.sources.app.controllers;

import com.sources.app.dao.HospitalDAO;
import com.sources.app.dao.ServiceApprovalDAO;
import com.sources.app.dao.SystemConfigDAO;
import com.sources.app.dao.UserDAO;
import com.sources.app.entities.Hospital;
import com.sources.app.entities.Policy;
import com.sources.app.entities.ServiceApproval;
import com.sources.app.entities.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Path("/service-approvals")
public class ServiceApprovalController {

    // Constants for frequently used literals
    private static final String KEY_SERVICE_COST = "serviceCost";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_APPROVAL_ID = "approvalId";
    private static final String KEY_APPROVAL_CODE = "approvalCode";
    private static final String KEY_PRESCRIPTION_ID = "prescriptionId";
    private static final String KEY_PRESCRIPTION_TOTAL = "prescriptionTotal";
    private static final String KEY_STATUS = "status";
    private static final String ERROR_PREFIX = "Error interno al procesar la solicitud: ";

    private ServiceApprovalDAO serviceApprovalDAO = new ServiceApprovalDAO();
    private UserDAO userDAO = new UserDAO();
    private HospitalDAO hospitalDAO = new HospitalDAO();
    private SystemConfigDAO systemConfigDAO = new SystemConfigDAO();

    /**
     * Endpoint para solicitar aprobación de servicio médico
     */
    @POST
    @Path("/request")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestServiceApproval(String jsonRequest) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject requestData = (JSONObject) parser.parse(jsonRequest);
            
            // Extraer datos de la solicitud
            Long userId = Long.parseLong(requestData.get("userId").toString());
            Long hospitalId = Long.parseLong(requestData.get("hospitalId").toString());
            String serviceId = (String) requestData.get("serviceId");
            String serviceName = (String) requestData.get("serviceName");
            String serviceDescription = (String) requestData.get("serviceDescription");
            Double serviceCost = Double.parseDouble(requestData.get(KEY_SERVICE_COST).toString());
            
            // Obtener usuario y hospital
            User user = userDAO.getById(userId);
            Hospital hospital = hospitalDAO.getById(hospitalId);
            
            if (user == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("Usuario no encontrado"))
                        .build();
            }
            
            if (hospital == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("Hospital no encontrado"))
                        .build();
            }
            
            // Verificar que el usuario tenga una póliza activa
            Policy policy = user.getPolicy();
            if (policy == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("El usuario no tiene una póliza asignada"))
                        .build();
            }
            
            // Verificar que la póliza no haya expirado
            if (policy.getExpirationDate().before(new Date())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("La póliza del usuario ha expirado"))
                        .build();
            }
            
            // Verificar pagos pendientes (esto sería un ejemplo, el criterio real dependería del negocio)
            if (user.getStatus().equals("PENDING_PAYMENT")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("El usuario tiene pagos pendientes"))
                        .build();
            }
            
            // Calcular el monto cubierto según el porcentaje de la póliza
            double coveragePercentage = policy.getCoveragePercentage() / 100.0;
            double coveredAmount = serviceCost * coveragePercentage;
            double patientAmount = serviceCost - coveredAmount;
            
            ServiceApproval approval = serviceApprovalDAO.create(
                user, hospital, serviceId, serviceName, serviceDescription, 
                serviceCost, coveredAmount, patientAmount, STATUS_APPROVED);
            
            if (approval == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(createErrorResponse("Error al crear la aprobación del servicio"))
                        .build();
            }
            
            // Preparar la respuesta
            Map<String, Object> responseData = new HashMap<>();
            responseData.put(KEY_SUCCESS, true);
            responseData.put(KEY_APPROVAL_ID, approval.getIdApproval());
            responseData.put(KEY_APPROVAL_CODE, approval.getApprovalCode());
            responseData.put(KEY_SERVICE_COST, serviceCost);
            responseData.put("coveredAmount", coveredAmount);
            responseData.put("patientAmount", patientAmount);
            responseData.put("approvalDate", approval.getApprovalDate());
            
            return Response.status(Response.Status.OK)
                    .entity(responseData)
                    .build();
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Error interno al procesar la solicitud: " + e.getMessage()))
                    .build();
        }
    }
    
    /**
     * Endpoint para solicitar aprobación de receta médica asociada a un servicio
     */
    @POST
    @Path("/prescription")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestPrescriptionApproval(String jsonRequest) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject requestData = (JSONObject) parser.parse(jsonRequest);
            
            // Extraer datos de la solicitud
            String approvalCode = (String) requestData.get("approvalCode");
            String prescriptionId = (String) requestData.get("prescriptionId");
            Double prescriptionTotal = Double.parseDouble(requestData.get("prescriptionTotal").toString());
            
            // Obtener la aprobación del servicio
            ServiceApproval approval = serviceApprovalDAO.getByApprovalCode(approvalCode);
            
            if (approval == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("Código de aprobación no válido"))
                        .build();
            }
            
            // Verificar que la aprobación esté en estado APPROVED
            if (!STATUS_APPROVED.equals(approval.getStatus())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("La aprobación de servicio no está en estado válido para agregar receta"))
                        .build();
            }
            
            // Obtener el monto mínimo configurado para aprobación de recetas
            Double minPrescriptionAmount = systemConfigDAO.getConfigValueAsDouble("MIN_PRESCRIPTION_AMOUNT", 250.00);
            
            // Verificar si el monto de la receta es menor al mínimo
            if (prescriptionTotal < minPrescriptionAmount) {
                // Rechazar la receta y actualizar el estado de la aprobación
                approval = serviceApprovalDAO.updateStatus(approval.getIdApproval(), STATUS_REJECTED, 
                    "El monto de la receta es menor al mínimo requerido de Q" + minPrescriptionAmount);
                
                Map<String, Object> responseData = new HashMap<>();
                responseData.put(KEY_SUCCESS, false);
                responseData.put(KEY_APPROVAL_ID, approval.getIdApproval());
                responseData.put(KEY_APPROVAL_CODE, approval.getApprovalCode());
                responseData.put(KEY_STATUS, STATUS_REJECTED);
                responseData.put("rejectionReason", approval.getRejectionReason());
                
                return Response.status(Response.Status.OK)
                        .entity(responseData)
                        .build();
            }
            
            // Actualizar la información de la receta en la aprobación
            approval = serviceApprovalDAO.updatePrescription(
                approval.getIdApproval(), prescriptionId, prescriptionTotal);
            
            if (approval == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(createErrorResponse("Error al actualizar la información de la receta"))
                        .build();
            }
            
            // Preparar la respuesta
            Map<String, Object> responseData = new HashMap<>();
            responseData.put(KEY_SUCCESS, true);
            responseData.put(KEY_APPROVAL_ID, approval.getIdApproval());
            responseData.put(KEY_APPROVAL_CODE, approval.getApprovalCode());
            responseData.put(KEY_PRESCRIPTION_ID, approval.getPrescriptionId());
            responseData.put(KEY_PRESCRIPTION_TOTAL, approval.getPrescriptionTotal());
            responseData.put(KEY_STATUS, approval.getStatus());
            
            return Response.status(Response.Status.OK)
                    .entity(responseData)
                    .build();
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Error interno al procesar la solicitud: " + e.getMessage()))
                    .build();
        }
    }
    
    /**
     * Endpoint para verificar el estado de una aprobación
     */
    @GET
    @Path("/check/{approvalCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkApprovalStatus(@PathParam("approvalCode") String approvalCode) {
        try {
            ServiceApproval approval = serviceApprovalDAO.getByApprovalCode(approvalCode);
            
            if (approval == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Aprobación no encontrada"))
                        .build();
            }
            
            // Preparar la respuesta
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("approvalId", approval.getIdApproval());
            responseData.put("approvalCode", approval.getApprovalCode());
            responseData.put("serviceName", approval.getServiceName());
            responseData.put("serviceDescription", approval.getServiceDescription());
            responseData.put("serviceCost", approval.getServiceCost());
            responseData.put("coveredAmount", approval.getCoveredAmount());
            responseData.put("patientAmount", approval.getPatientAmount());
            responseData.put("status", approval.getStatus());
            
            if (approval.getPrescriptionId() != null) {
                responseData.put("prescriptionId", approval.getPrescriptionId());
                responseData.put("prescriptionTotal", approval.getPrescriptionTotal());
            }
            
            if ("REJECTED".equals(approval.getStatus())) {
                responseData.put("rejectionReason", approval.getRejectionReason());
            }
            
            return Response.status(Response.Status.OK)
                    .entity(responseData)
                    .build();
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Error interno al procesar la solicitud: " + e.getMessage()))
                    .build();
        }
    }
    
    /**
     * Endpoint para marcar una aprobación como completada
     */
    @PUT
    @Path("/complete/{approvalCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response completeApproval(@PathParam("approvalCode") String approvalCode) {
        try {
            ServiceApproval approval = serviceApprovalDAO.getByApprovalCode(approvalCode);
            
            if (approval == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(createErrorResponse("Aprobación no encontrada"))
                        .build();
            }
            
            // Verificar que la aprobación esté en estado APPROVED
            if (!STATUS_APPROVED.equals(approval.getStatus())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createErrorResponse("La aprobación no está en estado válido para completarla"))
                        .build();
            }
            
            // Actualizar el estado a COMPLETED
            approval = serviceApprovalDAO.updateStatus(
                approval.getIdApproval(), STATUS_COMPLETED, null);
            
            // Preparar la respuesta
            Map<String, Object> responseData = new HashMap<>();
            responseData.put(KEY_SUCCESS, true);
            responseData.put(KEY_APPROVAL_ID, approval.getIdApproval());
            responseData.put(KEY_APPROVAL_CODE, approval.getApprovalCode());
            responseData.put(KEY_STATUS, approval.getStatus());
            responseData.put("completedDate", approval.getCompletedDate());
            
            return Response.status(Response.Status.OK)
                    .entity(responseData)
                    .build();
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("Error interno al procesar la solicitud: " + e.getMessage()))
                    .build();
        }
    }
    
    /**
     * Método auxiliar para crear una respuesta de error
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        return errorResponse;
    }
} 