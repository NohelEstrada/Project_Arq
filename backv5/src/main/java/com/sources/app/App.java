package com.sources.app;

import com.sources.app.dao.*;
import com.sources.app.handlers.*;
import com.sources.app.util.HibernateUtil;
import com.sun.net.httpserver.HttpServer;
import org.hibernate.Session;
import java.net.InetAddress;
import java.net.*;
import java.util.Enumeration;

import java.net.InetSocketAddress;

/**
 * Clase principal de la aplicación que inicializa y configura el servidor HTTP
 * para manejar las solicitudes de la API REST. Establece las rutas,
 * inicializa los Data Access Objects (DAO) y prueba la conexión con la base de datos.
 */
public class App {
    /** DAO para operaciones relacionadas con usuarios. */
    private static final UserDAO userDAO = new UserDAO();
    /** DAO para operaciones relacionadas con facturas. */
    private static final BillDAO billDAO = new BillDAO();
    /** DAO para la relación entre facturas y medicamentos. */
    private static final BillMedicineDAO billMedicineDAO = new BillMedicineDAO();
    /** DAO para operaciones relacionadas con categorías. */
    private static final CategoryDAO categoryDAO = new CategoryDAO();
    /** DAO para operaciones relacionadas con comentarios. */
    private static final CommentsDAO commentsDAO = new CommentsDAO();
    /** DAO para operaciones relacionadas con hospitales. */
    private static final HospitalDAO hospitalDAO = new HospitalDAO();
    /** DAO para operaciones relacionadas con medicamentos. */
    private static final MedicineDAO medicineDAO = new MedicineDAO();
    /** DAO para la relación entre medicamentos, categorías y subcategorías. */
    private static final MedicineCatSubcatDAO medicineCatSubcatDAO = new MedicineCatSubcatDAO();
    /** DAO para operaciones relacionadas con pedidos. */
    private static final OrdersDAO ordersDAO = new OrdersDAO();
    /** DAO para la relación entre pedidos y medicamentos. */
    private static final OrderMedicineDAO orderMedicineDAO = new OrderMedicineDAO();
    /** DAO para operaciones relacionadas con pólizas. */
    private static final PolicyDAO policyDAO = new PolicyDAO();
    /** DAO para operaciones relacionadas con prescripciones. */
    private static final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    /** DAO para la relación entre prescripciones y medicamentos. */
    private static final PrescriptionMedicineDAO prescriptionMedicineDAO = new PrescriptionMedicineDAO();
    /** DAO para operaciones relacionadas con subcategorías. */
    private static final SubcategoryDAO subcategoryDAO = new SubcategoryDAO();
    /** DAO para operaciones relacionadas con medicamentos externos. */
    private static final ExternalMedicineDAO externalMedicineDAO = new ExternalMedicineDAO();
    
    /**
     * Obtiene la dirección IP externa (no loopback, no link-local) de la máquina local.
     * Itera sobre las interfaces de red y sus direcciones para encontrar una IPv4 adecuada.
     *
     * @return La dirección IP externa local como String, o "127.0.0.1" si no se encuentra ninguna o ocurre un error.
     */
    private static String getLocalExternalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // Ignora interfaces inactivas o de loopback
                if (!iface.isUp() || iface.isLoopback()) continue;
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
            e.printStackTrace();
        }
        return "127.0.0.1";
    }

    /**
     * Punto de entrada principal de la aplicación.
     * Realiza una prueba de conexión a la base de datos, configura un servidor HTTP
     * en el puerto 8081, mapea las rutas de la API a sus respectivos Handlers
     * (inyectando las dependencias DAO necesarias) y arranca el servidor.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     * @throws Exception Si ocurre un error durante la inicialización del servidor.
     */
    public static void main(String[] args) throws Exception {
        // Prueba de conexión a la base de datos
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (session.isConnected()) {
                System.out.println("Conexión exitosa a la base de datos!");
            } else {
                System.err.println("No se pudo establecer conexión a la base de datos.");
            }
        } catch (Exception e) {
            System.err.println("Error al conectar con la base de datos:");
            e.printStackTrace();
        }

        String ip = getLocalExternalIp();

        // Obtener puerto desde variable de entorno o usar predeterminado
        int port = 8081; // Puerto predeterminado
        
        // Intentar obtener el puerto desde variable de entorno
        String envPort = System.getenv("PHARMACY_PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                port = Integer.parseInt(envPort.trim());
                System.out.println("Puerto configurado desde variable de entorno: " + port);
            } catch (NumberFormatException e) {
                System.out.println("Formato de puerto inválido en variable de entorno. Se usará el puerto predeterminado 8081.");
            }
        } else {
            System.out.println("No se encontró variable de entorno PHARMACY_PORT. Usando puerto predeterminado: " + port);
        }

        // Buscar un puerto disponible comenzando desde el puerto especificado
        HttpServer server = null;
        int originalPort = port;
        int maxAttempts = 100; // Límite de intentos para evitar bucle infinito
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                server = HttpServer.create(new InetSocketAddress(ip, port), 0);
                System.out.println("Puerto " + port + " disponible y asignado.");
                break;
            } catch (java.net.BindException e) {
                System.out.println("Puerto " + port + " ya está en uso. Probando puerto " + (port + 1) + "...");
                port++;
            } catch (Exception e) {
                System.err.println("Error al crear servidor en puerto " + port + ": " + e.getMessage());
                port++;
            }
        }
        
        if (server == null) {
            throw new RuntimeException("No se pudo encontrar un puerto disponible después de " + maxAttempts + " intentos comenzando desde el puerto " + originalPort);
        }
        
        if (port != originalPort) {
            System.out.println("NOTA: Se cambió del puerto original " + originalPort + " al puerto " + port + " debido a que el puerto original estaba ocupado.");
        }

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
        System.out.println("Servidor iniciado en http://" + ip + ":" + port + "/api2");
    }
}

