package com.sources.app.dao;

import com.sources.app.entities.Comments;
import com.sources.app.entities.User;
import com.sources.app.entities.Medicine;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) para gestionar entidades {@link Comments}.
 * Esta clase proporciona métodos para realizar operaciones CRUD (Crear, Leer, Actualizar)
 * en registros de Comentarios, que representan comentarios de usuarios, potencialmente vinculados a otros comentarios
 * y asociados con un {@link Medicine}. Utiliza Hibernate para interacciones con la base de datos.
 */
public class CommentsDAO {
    private static final Logger LOGGER = Logger.getLogger(CommentsDAO.class.getName());

    /**
     * Crea un nuevo registro de Comentario en la base de datos.
     *
     * @param user        El {@link User} que publicó el comentario.
     * @param prevComment El {@link Comments} precedente al que este comentario responde (puede ser null).
     * @param commentText El contenido textual del comentario.
     * @param medicine    El {@link Medicine} con el que está asociado el comentario.
     * @return La entidad {@link Comments} recién creada, o null si ocurrió un error.
     */
    public Comments create(User user, Comments prevComment, String commentText, Medicine medicine) {
        Transaction tx = null;
        Comments comments = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            comments = new Comments();
            comments.setUser(user);
            comments.setPrevComment(prevComment);
            comments.setCommentText(commentText);
            comments.setMedicine(medicine);

            session.persist(comments);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.log(Level.SEVERE, "Error creating Comments (user present: " + (user != null) + 
                    ", medicine present: " + (medicine != null) + ")", e);
        }
        return comments;
    }

    /**
     * Recupera todos los registros de Comentario de la base de datos.
     *
     * @return Una lista de todas las entidades {@link Comments}, o null si ocurrió un error.
     */
    public List<Comments> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Comments> query = session.createQuery("FROM Comments", Comments.class);
            return query.list();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching all Comments records", e);
            return Collections.emptyList();
        }
    }

    /**
     * Recupera un registro de Comentario específico por su identificador único.
     *
     * @param id El ID del Comentario a recuperar.
     * @return La entidad {@link Comments} correspondiente al ID dado, o null si no se encuentra o ocurrió un error.
     */
    public Comments getById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Comments.class, id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching Comments by id: " + id, e);
            return null;
        }
    }

    /**
     * Actualiza un registro de Comentario existente en la base de datos.
     * Esto típicamente implica cambiar el texto del comentario.
     *
     * @param comments La entidad {@link Comments} con información actualizada. El ID debe coincidir con un registro existente.
     * @return La entidad {@link Comments} actualizada, o null si la actualización falló o ocurrió un error.
     */
    public Comments update(Comments comments) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(comments);
            tx.commit();
            return comments;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            LOGGER.log(Level.SEVERE, "Error updating Comments (entity present: " + (comments != null) + ")", e);
            return null;
        }
    }
}
