package com.sources.app.entities;

import jakarta.persistence.*;

/**
 * Representa la relación entre un hospital y un servicio de seguro, mapeada a la tabla HOSPITAL_INSURANCE_SERVICE.
 * Indica si un servicio de seguro específico está aprobado para un hospital particular.
 */
@Entity
@Table(name = "HOSPITAL_INSURANCE_SERVICE")
public class HospitalInsuranceService {

    /**
     * Identificador único de la relación hospital-servicio de seguro.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HOSPITAL_SERVICE")
    private Long idHospitalService;

    /**
     * Hospital al que se asocia el servicio de seguro.
     * Relación Many-to-One obligatoria con la entidad Hospital.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_HOSPITAL", nullable = false)
    private Hospital hospital;

    /**
     * Servicio de seguro asociado al hospital.
     * Relación Many-to-One obligatoria con la entidad InsuranceService.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_INSURANCE_SERVICE", nullable = false)
    private InsuranceService insuranceService;

    /**
     * Estado de aprobación del servicio para el hospital.
     * Típicamente 1 para aprobado, 0 para no aprobado. No puede ser nulo.
     */
    @Column(name = "APPROVED", nullable = false)
    private Integer approved;

    /**
     * Fecha en que se realizó la aprobación.
     * Puede ser nulo si aún no está aprobado.
     */
    @Column(name = "APPROVAL_DATE")
    private java.util.Date approvalDate;

    /**
     * Notas adicionales sobre la relación o aprobación.
     * Tiene una longitud máxima de 255 caracteres. Puede ser nulo.
     */
    @Column(name = "NOTES", length = 255)
    private String notes;

    /**
     * Constructor por defecto para la entidad HospitalInsuranceService.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public HospitalInsuranceService() {}

    // Getters y Setters
    /**
     * Obtiene el ID de la relación hospital-servicio.
     * @return el ID de la relación.
     */
    public Long getIdHospitalService() {
        return idHospitalService;
    }

    /**
     * Establece el ID de la relación hospital-servicio.
     * @param idHospitalService el ID a establecer.
     */
    public void setIdHospitalService(Long idHospitalService) {
        this.idHospitalService = idHospitalService;
    }

    /**
     * Obtiene el hospital asociado.
     * @return la entidad Hospital.
     */
    public Hospital getHospital() {
        return hospital;
    }

    /**
     * Establece el hospital asociado.
     * @param hospital la entidad Hospital a establecer.
     */
    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    /**
     * Obtiene el servicio de seguro asociado.
     * @return la entidad InsuranceService.
     */
    public InsuranceService getInsuranceService() {
        return insuranceService;
    }

    /**
     * Establece el servicio de seguro asociado.
     * @param insuranceService la entidad InsuranceService a establecer.
     */
    public void setInsuranceService(InsuranceService insuranceService) {
        this.insuranceService = insuranceService;
    }

    /**
     * Obtiene el estado de aprobación.
     * @return el estado de aprobación (1 o 0).
     */
    public Integer getApproved() {
        return approved;
    }

    /**
     * Establece el estado de aprobación.
     * @param approved el estado a establecer.
     */
    public void setApproved(Integer approved) {
        this.approved = approved;
    }

    /**
     * Obtiene la fecha de aprobación.
     * @return la fecha de aprobación.
     */
    public java.util.Date getApprovalDate() {
        return approvalDate;
    }

    /**
     * Establece la fecha de aprobación.
     * @param approvalDate la fecha a establecer.
     */
    public void setApprovalDate(java.util.Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    /**
     * Obtiene las notas adicionales.
     * @return las notas.
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Establece las notas adicionales.
     * @param notes las notas a establecer.
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }
} 