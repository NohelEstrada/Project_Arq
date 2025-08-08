package com.sources.app.dao;

import com.sources.app.entities.Prescription;
import com.sources.app.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            String path = exchange.getRequestURI().getPath();
            try {
                // Crea un objeto URL con el endpoint de la API incluyendo el email proporcionado
                URL url = new URL(path + "/api2/verification?email=" + email);
                // Abre una conexión a la URL
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                // Establece el método de solicitud HTTP a GET
                con.setRequestMethod("GET");
                // Obtiene el código de respuesta
                int status = con.getResponseCode();
                // Lee la respuesta
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                // Desconecta la conexión
                con.disconnect();
                // Guarda la respuesta en la variable 'verify'
                String verify = content.toString();
                
                // Si el usuario no está verificado, retornar null
                if (!"1".equals(verify)) {
                    return null;
                }
                
            } catch(Exception e) {
                e.printStackTrace();
                transaction.rollback();
                return null;
            }
            
            Prescription prescription = session.get(Prescription.class, id);
            transaction.commit();
            return prescription;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}