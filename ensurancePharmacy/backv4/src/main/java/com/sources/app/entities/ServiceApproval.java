package com.sources.app.entities;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Entidad que representa una aprobación de servicio médico mapeada a la tabla `service_approvals`.
 */
@Entity
@Table(name = "service_approvals")
public class ServiceApproval {
    /**
     * Identificador único de la aprobación del servicio.
     * Generado mediante una secuencia.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_approval_seq")
    @SequenceGenerator(name = "service_approval_seq", sequenceName = "service_approval_seq", allocationSize = 1)
    private Long id;
    
    /**
     * Código único de la aprobación.
     * No puede ser nulo.
     */
    @Column(name = "approval_code", unique = true, nullable = false)
    private String approvalCode;
    
    /**
     * Identificador del usuario asociado a la aprobación.
     * No puede ser nulo.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * Identificador del hospital asociado a la aprobación.
     * No puede ser nulo.
     */
    @Column(name = "hospital_id", nullable = false)
    private Long hospitalId;
    
    /**
     * Identificador del servicio en el sistema del hospital.
     * No puede ser nulo.
     */
    @Column(name = "service_id", nullable = false)
    private String serviceId;  // ID del servicio en el sistema del hospital
    
    /**
     * Nombre del servicio.
     * No puede ser nulo.
     */
    @Column(name = "service_name", nullable = false)
    private String serviceName;
    
    /**
     * Costo total del servicio.
     * No puede ser nulo.
     */
    @Column(name = "service_cost", nullable = false)
    private Double serviceCost;
    
    /**
     * Monto cubierto por el seguro.
     * No puede ser nulo.
     */
    @Column(name = "covered_amount", nullable = false)
    private Double coveredAmount;
    
    /**
     * Monto a pagar por el paciente.
     * No puede ser nulo.
     */
    @Column(name = "patient_amount", nullable = false)
    private Double patientAmount;
    
    /**
     * Estado actual de la aprobación (PENDING, APPROVED, REJECTED, COMPLETED).
     * No puede ser nulo.
     */
    @Column(name = "status", nullable = false)
    private String status;  // PENDING, APPROVED, REJECTED, COMPLETED
    
    /**
     * Fecha en que se aprobó el servicio.
     * Puede ser nulo si aún no está aprobado.
     */
    @Column(name = "approval_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date approvalDate;
    
    /**
     * Fecha en que se completó el servicio.
     * Puede ser nulo si aún no está completado.
     */
    @Column(name = "completed_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date completedDate;
    
    /**
     * Razón del rechazo, si aplica.
     * Puede ser nulo.
     */
    @Column(name = "rejection_reason")
    private String rejectionReason;
    
    /**
     * Identificador de la prescripción asociada, si aplica.
     * Puede ser nulo.
     */
    @Column(name = "prescription_id")
    private Long prescriptionId;
    
    /**
     * Total de la prescripción asociada, si aplica.
     * Puede ser nulo.
     */
    @Column(name = "prescription_total")
    private Double prescriptionTotal;

    /**
     * Constructor por defecto.
     */
    public ServiceApproval() {
    }

    /**
     * Obtiene el ID de la aprobación.
     * @return El ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el ID de la aprobación.
     * @param id El ID a establecer.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene el código de aprobación.
     * @return El código de aprobación.
     */
    public String getApprovalCode() {
        return approvalCode;
    }

    /**
     * Establece el código de aprobación.
     * @param approvalCode El código de aprobación a establecer.
     */
    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }

    /**
     * Obtiene el ID del usuario.
     * @return El ID del usuario.
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Establece el ID del usuario.
     * @param userId El ID del usuario a establecer.
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Obtiene el ID del hospital.
     * @return El ID del hospital.
     */
    public Long getHospitalId() {
        return hospitalId;
    }

    /**
     * Establece el ID del hospital.
     * @param hospitalId El ID del hospital a establecer.
     */
    public void setHospitalId(Long hospitalId) {
        this.hospitalId = hospitalId;
    }

    /**
     * Obtiene el ID del servicio del hospital.
     * @return El ID del servicio.
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Establece el ID del servicio del hospital.
     * @param serviceId El ID del servicio a establecer.
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * Obtiene el nombre del servicio.
     * @return El nombre del servicio.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Establece el nombre del servicio.
     * @param serviceName El nombre del servicio a establecer.
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Obtiene el costo del servicio.
     * @return El costo del servicio.
     */
    public Double getServiceCost() {
        return serviceCost;
    }

    /**
     * Establece el costo del servicio.
     * @param serviceCost El costo del servicio a establecer.
     */
    public void setServiceCost(Double serviceCost) {
        this.serviceCost = serviceCost;
    }

    /**
     * Obtiene el monto cubierto.
     * @return El monto cubierto.
     */
    public Double getCoveredAmount() {
        return coveredAmount;
    }

    /**
     * Establece el monto cubierto.
     * @param coveredAmount El monto cubierto a establecer.
     */
    public void setCoveredAmount(Double coveredAmount) {
        this.coveredAmount = coveredAmount;
    }

    /**
     * Obtiene el monto a pagar por el paciente.
     * @return El monto del paciente.
     */
    public Double getPatientAmount() {
        return patientAmount;
    }

    /**
     * Establece el monto a pagar por el paciente.
     * @param patientAmount El monto del paciente a establecer.
     */
    public void setPatientAmount(Double patientAmount) {
        this.patientAmount = patientAmount;
    }

    /**
     * Obtiene el estado de la aprobación.
     * @return El estado.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Establece el estado de la aprobación.
     * @param status El estado a establecer.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Obtiene la fecha de aprobación.
     * @return La fecha de aprobación.
     */
    public Date getApprovalDate() {
        return approvalDate;
    }

    /**
     * Establece la fecha de aprobación.
     * @param approvalDate La fecha de aprobación a establecer.
     */
    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    /**
     * Obtiene la fecha de completación.
     * @return La fecha de completación.
     */
    public Date getCompletedDate() {
        return completedDate;
    }

    /**
     * Establece la fecha de completación.
     * @param completedDate La fecha de completación a establecer.
     */
    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    /**
     * Obtiene la razón del rechazo.
     * @return La razón del rechazo.
     */
    public String getRejectionReason() {
        return rejectionReason;
    }

    /**
     * Establece la razón del rechazo.
     * @param rejectionReason La razón del rechazo a establecer.
     */
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    /**
     * Obtiene el ID de la prescripción asociada.
     * @return El ID de la prescripción.
     */
    public Long getPrescriptionId() {
        return prescriptionId;
    }

    /**
     * Establece el ID de la prescripción asociada.
     * @param prescriptionId El ID de la prescripción a establecer.
     */
    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    /**
     * Obtiene el total de la prescripción asociada.
     * @return El total de la prescripción.
     */
    public Double getPrescriptionTotal() {
        return prescriptionTotal;
    }

    /**
     * Establece el total de la prescripción asociada.
     * @param prescriptionTotal El total de la prescripción a establecer.
     */
    public void setPrescriptionTotal(Double prescriptionTotal) {
        this.prescriptionTotal = prescriptionTotal;
    }
} 