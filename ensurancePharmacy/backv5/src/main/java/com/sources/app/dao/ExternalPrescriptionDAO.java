package com.sources.app.dao;

import com.sources.app.entities.Prescription;
import com.sources.app.exceptions.ExternalServiceException;
import com.sources.app.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para acceder a los datos de Recetas (Prescription), incorporando un paso de verificación externa.
 * <p>
 * **Advertencia de diseño:** Este DAO mezcla la lógica de acceso a datos (obtener una Receta)
 * con la interacción con un servicio externo (verificación de usuario vía HTTP) y depende directamente
 * de {@link com.sun.net.httpserver.HttpExchange}, lo cual representa un mal diseño de capas y
 * acopla el DAO a la capa de manejo HTTP. La justificación para verificar el email de un usuario
 * antes de obtener una receta específica por su ID tampoco está clara.
 * La gestión de transacciones alrededor de la llamada externa puede ser defectuosa.
 * </p>
 */
public class ExternalPrescriptionDAO {
    private static final Logger LOGGER = Logger.getLogger(ExternalPrescriptionDAO.class.getName());

    /**
     * Recupera una receta por su ID, pero solo después de verificar exitosamente el email proporcionado
     * mediante una solicitud HTTP GET externa a un endpoint de verificación.
     * <p>
     * **Advertencias de diseño:**
     * <ul>
     *     <li>Realiza una llamada HTTP externa dentro de un método DAO, mezclando responsabilidades.</li>
     *     <li>Depende de {@link HttpExchange} para construir la URL de verificación, probablemente de forma incorrecta y acoplando capas.</li>
     *     <li>La lógica que vincula la verificación del email del usuario con la obtención de un ID de receta específico es inusual.</li>
     *     <li>La gestión de transacciones de Hibernate alrededor de la llamada externa es potencialmente incorrecta.</li>
     * </ul>
     *
     * @param id       El ID de la {@link Prescription} a recuperar.
     * @param email    La dirección de correo electrónico del usuario a verificar externamente.
     * @param exchange El objeto {@link HttpExchange} original (usado problemáticamente para construir la URL de verificación).
     * @return La entidad {@link Prescription} si se encuentra y el usuario es verificado externamente,
     *         {@code null} en caso contrario (usuario no verificado, receta no encontrada, o ocurrió un error durante la verificación o la obtención).
     */
    public Prescription getbyId(Long id, String email, HttpExchange exchange) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            try {
                transaction = session.beginTransaction();
                final String base = exchange.getRequestURI().toString();
                LOGGER.log(Level.INFO, () -> "ExternalPrescriptionDAO verifying for email=" + email + ", baseURI=" + base);
                
                if (!verifyUserExternally(base, email)) {
                    rollbackTransaction(transaction, "unverified user for email=" + email);
                    return null;
                }
                
                return fetchPrescriptionById(session, transaction, id);
            } catch (Exception e) {
                rollbackTransaction(transaction, "top-level error for id=" + id + ", email=" + email);
                LOGGER.log(Level.SEVERE, () -> "Error fetching Prescription by id: " + id + " after external verification for email: " + email);
                return null;
            }
        }
    }
    
    private boolean verifyUserExternally(String base, String email) {
        try {
            final String baseNoSlash = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
            String encodedEmail = email == null ? "" : URLEncoder.encode(email, StandardCharsets.UTF_8);
            String verifyUrl = String.format("%s/api2/verification?email=%s", baseNoSlash, encodedEmail);
            try {
                URL url = URI.create(verifyUrl).toURL();
                LOGGER.log(Level.INFO, () -> "Verification URL built: " + url);
                
                return performHttpVerification(url, email);
            } catch(ExternalServiceException e) {
                LOGGER.log(Level.SEVERE, () -> "Error verifying user via external service for email: " + email + "; baseURI: " + base);
                return false;
            } catch(Exception e) {
                LOGGER.log(Level.SEVERE, () -> "Unexpected error verifying user via external service for email: " + email + "; baseURI: " + base);
                return false;
            }
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, () -> "Error verifying user via external service for email: " + email + "; baseURI: " + base);
            return false;
        }
    }
    
    private boolean performHttpVerification(URL url, String email) throws ExternalServiceException {
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            
            int status = con.getResponseCode();
            String verify = readHttpResponse(con).trim();
            con.disconnect();
            
            LOGGER.log(Level.INFO, () -> "Verification HTTP status=" + status + ", body='" + verify + "'");
            return "1".equals(verify);
        } catch (Exception e) {
            throw new ExternalServiceException("Failed to perform HTTP verification for email: " + email, e);
        }
    }
    
    private String readHttpResponse(HttpURLConnection con) throws Exception {
        StringBuilder content = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
        }
        return content.toString();
    }
    
    private Prescription fetchPrescriptionById(Session session, Transaction transaction, Long id) {
        Prescription prescription = session.get(Prescription.class, id);
        if (prescription == null) {
            LOGGER.log(Level.INFO, () -> "Prescription not found after verification for id=" + id);
        } else {
            LOGGER.log(Level.INFO, () -> "Prescription found after verification: id=" + prescription.getIdPrescription());
        }
        transaction.commit();
        return prescription;
    }
    
    private void rollbackTransaction(Transaction transaction, String context) {
        if (transaction != null) {
            try { 
                transaction.rollback(); 
            } catch (Exception re) { 
                LOGGER.log(Level.WARNING, () -> "Rollback failed after " + context);
            }
        }
    }
}