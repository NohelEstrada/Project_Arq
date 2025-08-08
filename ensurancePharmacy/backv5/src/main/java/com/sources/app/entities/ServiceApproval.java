package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Entidad que representa la aprobación de un servicio (posiblemente por un seguro).
 * Contiene información sobre el servicio, costos, cobertura, usuario, hospital y estado de aprobación.
 * Mapea a la tabla "SERVICE_APPROVALS".
 */
@Entity
@Table(name = "SERVICE_APPROVALS")
public class ServiceApproval {

    /** Identificador único de la aprobación. Generado automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_APPROVAL")
    private Long idApproval;

    /** Código único de aprobación asignado por el sistema externo (e.g., seguro). */
    @Column(name = "APPROVAL_CODE", length = 20, nullable = false, unique = true)
    private String approvalCode;

    /** Usuario asociado a la aprobación del servicio. */
    @ManyToOne
    @JoinColumn(name = "ID_USER", nullable = false)
    private User user;

    /** Hospital donde se proveerá el servicio aprobado. */
    @ManyToOne
    @JoinColumn(name = "ID_HOSPITAL", nullable = false)
    private Hospital hospital;

    /** Identificador del servicio específico aprobado (e.g., código de procedimiento). */
    @Column(name = "SERVICE_ID", nullable = false)
    private String serviceId;

    /** Nombre del servicio aprobado. */
    @Column(name = "SERVICE_NAME", nullable = false)
    private String serviceName;

    /** Descripción adicional del servicio aprobado. */
    @Column(name = "SERVICE_DESCRIPTION")
    private String serviceDescription;

    /** Costo total estimado o real del servicio. */
    @Column(name = "SERVICE_COST", nullable = false)
    private Double serviceCost;

    /** Monto cubierto por el seguro u otra entidad. */
    @Column(name = "COVERED_AMOUNT", nullable = false)
    private Double coveredAmount;

    /** Monto a pagar por el paciente (copago u otros). */
    @Column(name = "PATIENT_AMOUNT", nullable = false)
    private Double patientAmount;

    /** Identificador de la prescripción asociada (si aplica). Puede ser un ID externo o interno. */
    @Column(name = "PRESCRIPTION_ID")
    private String prescriptionId;
    
    /** Costo total de la prescripción asociada (si aplica). */
    @Column(name = "PRESCRIPTION_TOTAL")
    private Double prescriptionTotal;
    
    /** Estado actual de la aprobación (e.g., "APPROVED", "REJECTED", "PENDING", "COMPLETED"). */
    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;
    
    /** Razón del rechazo (si el estado es "REJECTED"). */
    @Column(name = "REJECTION_REASON")
    private String rejectionReason;
    
    /** Fecha en que se registró la aprobación o el último cambio de estado significativo. */
    @Column(name = "APPROVAL_DATE", nullable = false)
    private Date approvalDate;
    
    /** Fecha en que el servicio asociado se completó (si aplica). */
    @Column(name = "COMPLETED_DATE")
    private Date completedDate;
    
    /** Fecha de creación del registro de aprobación en el sistema. */
    @Column(name = "CREATED_AT", nullable = false)
    private Date createdAt;
    
    /**
     * Método invocado por JPA antes de persistir la entidad por primera vez.
     * Establece las fechas de creación y aprobación iniciales a la fecha actual.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        approvalDate = new Date();
    }

    // Constructores
    public ServiceApproval() {
    }

    // Getters y Setters
    public Long getIdApproval() {
        return idApproval;
    }

    public void setIdApproval(Long idApproval) {
        this.idApproval = idApproval;
    }

    public String getApprovalCode() {
        return approvalCode;
    }

    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public Double getServiceCost() {
        return serviceCost;
    }

    public void setServiceCost(Double serviceCost) {
        this.serviceCost = serviceCost;
    }

    public Double getCoveredAmount() {
        return coveredAmount;
    }

    public void setCoveredAmount(Double coveredAmount) {
        this.coveredAmount = coveredAmount;
    }

    public Double getPatientAmount() {
        return patientAmount;
    }

    public void setPatientAmount(Double patientAmount) {
        this.patientAmount = patientAmount;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(String prescriptionId) {
        this.prescriptionId = prescriptionId;
    }
    
    public Double getPrescriptionTotal() {
        return prescriptionTotal;
    }

    public void setPrescriptionTotal(Double prescriptionTotal) {
        this.prescriptionTotal = prescriptionTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }
    
    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
} 