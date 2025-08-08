package com.sources.app.entities;

import jakarta.persistence.*;
import com.sources.app.entities.Medicine;

/**
 * Entidad que representa un comentario en el sistema.
 * Mapea a la tabla "COMMENTS". Asociado a un usuario, un medicamento y opcionalmente
 * a un comentario previo para formar hilos.
 */
@Entity
@Table(name = "COMMENTS")
public class Comments {

    /** Identificador único del comentario. Generado automáticamente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_COMMENTS")
    private Long idComments;

    /** Usuario que realizó el comentario. */
    @ManyToOne
    @JoinColumn(name = "ID_USER", referencedColumnName = "ID_USER")
    private User user;

    /** Comentario al que responde este comentario (para hilos). Puede ser nulo. */
    @ManyToOne
    @JoinColumn(name = "ID_PREV_COMMENT", referencedColumnName = "ID_COMMENTS")
    private Comments prevComment;

    /** Texto del comentario. */
    @Column(name = "COMMENT_TEXT")
    private String commentText;

    /** Medicamento sobre el cual se realiza el comentario. */
    @ManyToOne
    @JoinColumn(name = "ID_MEDICINE", referencedColumnName = "ID_MEDICINE")
    private Medicine medicine;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Comments() {
    }

    /**
     * Constructor para crear un nuevo comentario.
     * Nota: Este constructor no inicializa el campo 'medicine'.
     *
     * @param user El usuario que realiza el comentario.
     * @param prevComment El comentario previo (puede ser nulo).
     * @param commentText El texto del comentario.
     */
    public Comments(User user, Comments prevComment, String commentText) {
        this.user = user;
        this.prevComment = prevComment;
        this.commentText = commentText;
    }

    // Getters y setters
    public Long getIdComments() {
        return idComments;
    }

    public void setIdComments(Long idComments) {
        this.idComments = idComments;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Comments getPrevComment() {
        return prevComment;
    }

    public void setPrevComment(Comments prevComment) {
        this.prevComment = prevComment;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }
}
