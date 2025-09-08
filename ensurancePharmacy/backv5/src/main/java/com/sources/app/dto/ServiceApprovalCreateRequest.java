package com.sources.app.dto;

import com.sources.app.entities.Hospital;
import com.sources.app.entities.User;

/**
 * DTO para la creación de aprobaciones de servicio que encapsula los múltiples parámetros
 * necesarios para crear una nueva aprobación de servicio.
 */
public class ServiceApprovalCreateRequest {
    private User user;
    private Hospital hospital;
    private String serviceId;
    private String serviceName;
    private String serviceDescription;
    private Double serviceCost;
    private Double coveredAmount;
    private Double patientAmount;
    private String status;

    public ServiceApprovalCreateRequest() {}

    public ServiceApprovalCreateRequest(User user, Hospital hospital, String serviceId, String serviceName,
                                      String serviceDescription, Double serviceCost, Double coveredAmount,
                                      Double patientAmount, String status) {
        this.user = user;
        this.hospital = hospital;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.serviceCost = serviceCost;
        this.coveredAmount = coveredAmount;
        this.patientAmount = patientAmount;
        this.status = status;
    }

    // Getters and Setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Hospital getHospital() { return hospital; }
    public void setHospital(Hospital hospital) { this.hospital = hospital; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getServiceDescription() { return serviceDescription; }
    public void setServiceDescription(String serviceDescription) { this.serviceDescription = serviceDescription; }

    public Double getServiceCost() { return serviceCost; }
    public void setServiceCost(Double serviceCost) { this.serviceCost = serviceCost; }

    public Double getCoveredAmount() { return coveredAmount; }
    public void setCoveredAmount(Double coveredAmount) { this.coveredAmount = coveredAmount; }

    public Double getPatientAmount() { return patientAmount; }
    public void setPatientAmount(Double patientAmount) { this.patientAmount = patientAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
