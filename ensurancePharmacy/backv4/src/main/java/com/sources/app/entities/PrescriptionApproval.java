package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Representa una entidad de aprobación de prescripción mapeada a la tabla PRESCRIPTION_APPROVALS.
 * Almacena información sobre el estado de aprobación de las prescripciones médicas.
 */
@Entity
@Table(name = "PRESCRIPTION_APPROVALS")
public class PrescriptionApproval {

    /**
     * Identificador único de la aprobación.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_APPROVAL")
    private Long idApproval;

    /**
     * Número de autorización único para la aprobación.
     * No puede ser nulo, es único y tiene longitud máxima de 50 caracteres.
     */
    @Column(name = "AUTHORIZATION_NUMBER", unique = true, nullable = false, length = 50)
    private String authorizationNumber;

    /**
     * Identificador del usuario asociado a la prescripción.
     * No puede ser nulo.
     */
    @Column(name = "ID_USER", nullable = false)
    private Long idUser;

    /**
     * ID de la prescripción en el sistema del hospital (origen).
     * Puede ser nulo. Longitud máxima de 100 caracteres.
     */
    @Column(name = "PRESCRIPTION_ID_HOSPITAL", nullable = true, length = 100) // ID de la receta en el sistema del hospital
    private String prescriptionIdHospital;

    /**
     * Detalles de la prescripción, posiblemente en formato JSON o texto.
     * Longitud máxima de 2000 caracteres. Puede ser nulo.
     */
    @Column(name = "PRESCRIPTION_DETAILS", length = 2000) // Puede ser JSON o texto
    private String prescriptionDetails;

    /**
     * Costo total de la prescripción.
     * No puede ser nulo.
     */
    @Column(name = "PRESCRIPTION_COST", nullable = false)
    private Double prescriptionCost;

    /**
     * Fecha y hora en que se realizó la aprobación.
     * Se almacena con precisión de timestamp. No puede ser nulo.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "APPROVAL_DATE", nullable = false)
    private Date approvalDate;

    /**
     * Estado de la aprobación (ej. APPROVED, REJECTED).
     * No puede ser nulo. Longitud máxima de 20 caracteres.
     */
    @Column(name = "STATUS", nullable = false, length = 20) // APPROVED, REJECTED
    private String status;

    /**
     * Razón del rechazo, si aplica.
     * Longitud máxima de 255 caracteres. Puede ser nulo.
     */
    @Column(name = "REJECTION_REASON", length = 255)
    private String rejectionReason;

    /**
     * Constructor por defecto.
     * Inicializa la fecha de aprobación con la fecha y hora actual.
     */
    // Constructor
    public PrescriptionApproval() {
        this.approvalDate = new Date();
    }

    // Getters y Setters
    /**
     * Obtiene el ID de la aprobación.
     * @return el ID de la aprobación.
     */
    public Long getIdApproval() { return idApproval; }
    /**
     * Establece el ID de la aprobación.
     * @param idApproval el ID a establecer.
     */
    public void setIdApproval(Long idApproval) { this.idApproval = idApproval; }

    /**
     * Obtiene el número de autorización.
     * @return el número de autorización.
     */
    public String getAuthorizationNumber() { return authorizationNumber; }
    /**
     * Establece el número de autorización.
     * @param authorizationNumber el número a establecer.
     */
    public void setAuthorizationNumber(String authorizationNumber) { this.authorizationNumber = authorizationNumber; }

    /**
     * Obtiene el ID del usuario.
     * @return el ID del usuario.
     */
    public Long getIdUser() { return idUser; }
    /**
     * Establece el ID del usuario.
     * @param idUser el ID a establecer.
     */
    public void setIdUser(Long idUser) { this.idUser = idUser; }

    /**
     * Obtiene el ID de la prescripción del hospital.
     * @return el ID de la prescripción del hospital.
     */
    public String getPrescriptionIdHospital() { return prescriptionIdHospital; }
    /**
     * Establece el ID de la prescripción del hospital.
     * @param prescriptionIdHospital el ID a establecer.
     */
    public void setPrescriptionIdHospital(String prescriptionIdHospital) { this.prescriptionIdHospital = prescriptionIdHospital; }

    /**
     * Obtiene los detalles de la prescripción.
     * @return los detalles de la prescripción.
     */
    public String getPrescriptionDetails() { return prescriptionDetails; }
    /**
     * Establece los detalles de la prescripción.
     * @param prescriptionDetails los detalles a establecer.
     */
    public void setPrescriptionDetails(String prescriptionDetails) { this.prescriptionDetails = prescriptionDetails; }

    /**
     * Obtiene el costo de la prescripción.
     * @return el costo de la prescripción.
     */
    public Double getPrescriptionCost() { return prescriptionCost; }
    /**
     * Establece el costo de la prescripción.
     * @param prescriptionCost el costo a establecer.
     */
    public void setPrescriptionCost(Double prescriptionCost) { this.prescriptionCost = prescriptionCost; }

    /**
     * Obtiene la fecha de aprobación.
     * @return la fecha de aprobación.
     */
    public Date getApprovalDate() { return approvalDate; }
    /**
     * Establece la fecha de aprobación.
     * @param approvalDate la fecha a establecer.
     */
    public void setApprovalDate(Date approvalDate) { this.approvalDate = approvalDate; }

    /**
     * Obtiene el estado de la aprobación.
     * @return el estado.
     */
    public String getStatus() { return status; }
    /**
     * Establece el estado de la aprobación.
     * @param status el estado a establecer.
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Obtiene la razón del rechazo.
     * @return la razón del rechazo.
     */
    public String getRejectionReason() { return rejectionReason; }
    /**
     * Establece la razón del rechazo.
     * @param rejectionReason la razón a establecer.
     */
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
} 