package com.sources.app;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.hibernate.Session;

import com.sources.app.dao.BillDAO;
import com.sources.app.dao.BillMedicineDAO;
import com.sources.app.dao.CategoryDAO;
import com.sources.app.dao.CommentsDAO;
import com.sources.app.dao.ExternalMedicineDAO;
import com.sources.app.dao.HospitalDAO;
import com.sources.app.dao.MedicineDAO;
import com.sources.app.dao.OrderMedicineDAO;
import com.sources.app.dao.OrdersDAO;
import com.sources.app.dao.PrescriptionDAO;
import com.sources.app.dao.PrescriptionMedicineDAO;
import com.sources.app.dao.SubcategoryDAO;
import com.sources.app.dao.UserDAO;
import com.sources.app.handlers.BillHandler;
import com.sources.app.handlers.BillMedicineHandler;
import com.sources.app.handlers.CategoryHandler;
import com.sources.app.handlers.CommentsHandler;
import com.sources.app.handlers.ExternalMedicineHandler;
import com.sources.app.handlers.HospitalHandler;
import com.sources.app.handlers.LoginHandler;
import com.sources.app.handlers.MedicineHandler;
import com.sources.app.handlers.OrderMedicineHandler;
import com.sources.app.handlers.OrdersHandler;
import com.sources.app.handlers.PrescriptionHandler;
import com.sources.app.handlers.PrescriptionMedicineHandler;
import com.sources.app.handlers.SearchMedicineHandler;
import com.sources.app.handlers.SubcategoryHandler;
import com.sources.app.handlers.UserHandler;
import com.sources.app.handlers.VerificationHandler;
import com.sources.app.util.HibernateUtil;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase principal de la aplicación que inicializa y configura el servidor HTTP
 * para manejar las solicitudes de la API REST. Establece las rutas, inicializa
 * los Data Access Objects (DAO) y prueba la conexión con la base de datos.
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);
    /**
     * DAO para operaciones relacionadas con usuarios.
     */
    private static final UserDAO userDAO = new UserDAO();
    /**
     * DAO para operaciones relacionadas con facturas.
     */
    private static final BillDAO billDAO = new BillDAO();
    /**
     * DAO para la relación entre facturas y medicamentos.
     */
    private static final BillMedicineDAO billMedicineDAO = new BillMedicineDAO();
    /**
     * DAO para operaciones relacionadas con categorías.
     */
    private static final CategoryDAO categoryDAO = new CategoryDAO();
    /**
     * DAO para operaciones relacionadas con comentarios.
     */
    private static final CommentsDAO commentsDAO = new CommentsDAO();
    /**
     * DAO para operaciones relacionadas con hospitales.
     */
    private static final HospitalDAO hospitalDAO = new HospitalDAO();
    /**
     * DAO para operaciones relacionadas con medicamentos.
     */
    private static final MedicineDAO medicineDAO = new MedicineDAO();
    /**
     * DAO para operaciones relacionadas con pedidos.
     */
    private static final OrdersDAO ordersDAO = new OrdersDAO();
    /**
     * DAO para la relación entre pedidos y medicamentos.
     */
    private static final OrderMedicineDAO orderMedicineDAO = new OrderMedicineDAO();
    /**
     * DAO para operaciones relacionadas con prescripciones.
     */
    private static final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    /**
     * DAO para la relación entre prescripciones y medicamentos.
     */
    private static final PrescriptionMedicineDAO prescriptionMedicineDAO = new PrescriptionMedicineDAO();
    /**
     * DAO para operaciones relacionadas con subcategorías.
     */
    private static final SubcategoryDAO subcategoryDAO = new SubcategoryDAO();
    /**
     * DAO para operaciones relacionadas con medicamentos externos.
     */
    private static final ExternalMedicineDAO externalMedicineDAO = new ExternalMedicineDAO();

    /**
     * Obtiene la dirección IP externa (no loopback, no link-local) de la
     * máquina local. Itera sobre las interfaces de red y sus direcciones para
     * encontrar una IPv4 adecuada.
     *
     * @return La dirección IP externa local como String, o "127.0.0.1" si no se
     * encuentra ninguna o ocurre un error.
     */
    private static String getLocalExternalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // Ignora interfaces inactivas o de loopback
                if (!iface.isUp() || iface.isLoopback()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Solo direcciones IPv4 que no sean loopback o de enlace local
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress() && !addr.isLinkLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("Error obteniendo IP externa local", e);
        }
        return "127.0.0.1";
    }

    /**
     * Punto de entrada principal de la aplicación. Realiza una prueba de
     * conexión a la base de datos, configura un servidor HTTP en el puerto
     * 8081, mapea las rutas de la API a sus respectivos Handlers (inyectando
     * las dependencias DAO necesarias) y arranca el servidor.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     * @throws Exception Si ocurre un error durante la inicialización del
     * servidor.
     */
    public static void main(String[] args) throws Exception {
        // Prueba de conexión a la base de datos
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (session.isConnected()) {
                logger.info("Conexión exitosa a la base de datos!");
            } else {
                logger.error("No se pudo establecer conexión a la base de datos.");
            }
        } catch (Exception e) {
            logger.error("Error al conectar con la base de datos (continuando sin DB)", e);
        }

        // Host/puerto desde variables de entorno con valores por defecto
        String host = System.getenv("SERVER_HOST");
        if (host == null || host.isEmpty()) {
            host = "0.0.0.0";
        }

        String portEnv = System.getenv("SERVER_PORT");
        if (portEnv == null || portEnv.isEmpty()) {
            portEnv = System.getenv("PORT");
        }

        int port = 8081; // Puerto predeterminado
        try {
            if (portEnv != null && !portEnv.isEmpty()) {
                port = Integer.parseInt(portEnv);
            }
        } catch (NumberFormatException e) {
            logger.warn("Formato de puerto inválido. Se usará el puerto predeterminado 8081.");
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(host, port), 0);

        // Asignamos cada contexto con su respectivo Handler usando el prefijo "/api2"
        server.createContext("/api2/login", new LoginHandler(userDAO));
        server.createContext("/api2/users", new UserHandler(userDAO));
        server.createContext("/api2/bills", new BillHandler(billDAO));
        server.createContext("/api2/bill_medicines", new BillMedicineHandler(billMedicineDAO));
        server.createContext("/api2/categories", new CategoryHandler(categoryDAO));
        server.createContext("/api2/comments", new CommentsHandler(commentsDAO));
        server.createContext("/api2/hospitals", new HospitalHandler(hospitalDAO));
        server.createContext("/api2/medicines", new MedicineHandler(medicineDAO));
        server.createContext("/api2/medicines/search", new SearchMedicineHandler(medicineDAO));
        server.createContext("/api2/order_medicines", new OrderMedicineHandler(orderMedicineDAO));
        server.createContext("/api2/orders", new OrdersHandler(ordersDAO));
        server.createContext("/api2/prescription_medicines", new PrescriptionMedicineHandler(prescriptionMedicineDAO));
        server.createContext("/api2/prescriptions", new PrescriptionHandler(prescriptionDAO));
        server.createContext("/api2/subcategories", new SubcategoryHandler(subcategoryDAO));
        server.createContext("/api2/external_medicines", new ExternalMedicineHandler(externalMedicineDAO));
        server.createContext("/api2/verification", new VerificationHandler());
        server.setExecutor(null); // Usa el executor por defecto
        server.start();
        logger.info("Servidor iniciado en http://{}:{}/api2", host, port);
    }
}
