package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;
import java.math.BigDecimal;

/**
 * Representa una entidad que almacena totales diarios para un hospital, mapeada a la tabla TOTAL_HOSPITAL.
 */
@Entity
@Table(name = "TOTAL_HOSPITAL")
public class TotalHospital {

    /**
     * Identificador único del registro de total de hospital.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TOTAL_HOSPITAL")
    private Long idTotalHospital;

    /**
     * Hospital al que pertenece este total diario.
     * Relación Many-to-One obligatoria con la entidad Hospital.
     */
    // Relación ManyToOne con Hospital (la columna ID_HOSPITAL es llave foránea)
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_HOSPITAL", nullable = false)
    private Hospital hospital;

    /**
     * Fecha a la que corresponde el total.
     * Se almacena solo la fecha. No puede ser nulo.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "TOTAL_DATE", nullable = false)
    private Date totalDate;

    /**
     * Monto total para el hospital en la fecha especificada.
     * Se almacena con precisión decimal (10, 2). No puede ser nulo.
     */
    @Column(name = "TOTAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    /**
     * Constructor por defecto para la entidad TotalHospital.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public TotalHospital() {}

    // Getters y Setters
    /**
     * Obtiene el ID del registro de total de hospital.
     * @return el ID del total.
     */
    public Long getIdTotalHospital() {
        return idTotalHospital;
    }

    /**
     * Establece el ID del registro de total de hospital.
     * @param idTotalHospital el ID a establecer.
     */
    public void setIdTotalHospital(Long idTotalHospital) {
        this.idTotalHospital = idTotalHospital;
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
     * Obtiene la fecha del total.
     * @return la fecha del total.
     */
    public Date getTotalDate() {
        return totalDate;
    }

    /**
     * Establece la fecha del total.
     * @param totalDate la fecha a establecer.
     */
    public void setTotalDate(Date totalDate) {
        this.totalDate = totalDate;
    }

    /**
     * Obtiene el monto total.
     * @return el monto total.
     */
    public BigDecimal getTotal() {
        return total;
    }

    /**
     * Establece el monto total.
     * @param total el monto total a establecer.
     */
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
