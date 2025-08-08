package com.sources.app.entities;

import jakarta.persistence.*;
import java.util.Date;
import java.math.BigDecimal;

/**
 * Representa una entidad que almacena totales diarios para una farmacia, mapeada a la tabla TOTAL_PHARMACY.
 */
@Entity
@Table(name = "TOTAL_PHARMACY")
public class TotalPharmacy {

    /**
     * Identificador único del registro de total de farmacia.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TOTAL_PHARMACY")
    private Long idTotalPharmacy;

    /**
     * Farmacia a la que pertenece este total diario.
     * Relación Many-to-One obligatoria con la entidad Pharmacy.
     */
    // Relación ManyToOne con Pharmacy (la columna ID_PHARMACY es llave foránea)
    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_PHARMACY", nullable = false)
    private Pharmacy pharmacy;

    /**
     * Fecha a la que corresponde el total.
     * Se almacena solo la fecha. No puede ser nulo.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "TOTAL_DATE", nullable = false)
    private Date totalDate;

    /**
     * Monto total para la farmacia en la fecha especificada.
     * Se almacena con precisión decimal (10, 2). No puede ser nulo.
     */
    @Column(name = "TOTAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    /**
     * Constructor por defecto para la entidad TotalPharmacy.
     * Requerido por JPA.
     */
    // Constructor por defecto
    public TotalPharmacy() {}

    // Getters y Setters
    /**
     * Obtiene el ID del registro de total de farmacia.
     * @return el ID del total.
     */
    public Long getIdTotalPharmacy() {
        return idTotalPharmacy;
    }

    /**
     * Establece el ID del registro de total de farmacia.
     * @param idTotalPharmacy el ID a establecer.
     */
    public void setIdTotalPharmacy(Long idTotalPharmacy) {
        this.idTotalPharmacy = idTotalPharmacy;
    }

    /**
     * Obtiene la farmacia asociada.
     * @return la entidad Pharmacy.
     */
    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    /**
     * Establece la farmacia asociada.
     * @param pharmacy la entidad Pharmacy a establecer.
     */
    public void setPharmacy(Pharmacy pharmacy) {
        this.pharmacy = pharmacy;
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
