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
 * Clase principal de la aplicación para el backend de Ensurance Pharmacy.
 * Inicializa la conexión a la base de datos, los DAOs, el servidor HTTP y configura los endpoints de la API.
 * También incluye lógica para verificaciones periódicas como la expiración de servicios.
 */
public class App {
    private static final UserDAO userDAO = new UserDAO();
    private static final PolicyDAO policyDAO = new PolicyDAO();
    private static final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private static final AppointmentMadeDAO appointmentMadeDAO = new AppointmentMadeDAO();
    private static final CategoryDAO categoryDAO = new CategoryDAO();
    private static final ConfigurableAmountDAO configurableAmountDAO = new ConfigurableAmountDAO();
    private static final HospitalDAO hospitalDAO = new HospitalDAO();
    private static final MedicineDAO medicineDAO = new MedicineDAO();
    private static final MedicinePresDAO medicinePresDAO = new MedicinePresDAO();
    private static final PharmacyDAO pharmacyDAO = new PharmacyDAO();
    private static final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private static final ServiceDAO serviceDAO = new ServiceDAO();
    private static final TotalHospitalDAO totalHospitalDAO = new TotalHospitalDAO();
    private static final TotalPharmacyDAO totalPharmacyDAO = new TotalPharmacyDAO();
    private static final TransactionsDAO transactionsDAO = new TransactionsDAO();
    private static final TransactionPolicyDAO transactionPolicyDAO = new TransactionPolicyDAO();
    private static final ServiceCategoryDAO serviceCategoryDAO = new ServiceCategoryDAO();
    private static final InsuranceServiceDAO insuranceServiceDAO = new InsuranceServiceDAO();
    private static final HospitalInsuranceServiceDAO hospitalInsuranceServiceDAO = new HospitalInsuranceServiceDAO();
    private static final EnsuranceAppointmentDAO ensuranceAppointmentDAO = new EnsuranceAppointmentDAO();
    private static final PrescriptionApprovalDAO prescriptionApprovalDAO = new PrescriptionApprovalDAO();

    /**
     * Constructor privado para prevenir la instanciación de la clase de utilidad.
     */
    private App() {
        // Prevent instantiation
    }

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
     * El punto de entrada principal de la aplicación.
     * Inicializa el servidor, configura los manejadores de contexto para varios endpoints de la API,
     * realiza comprobaciones iniciales de la base de datos e inicia el servidor HTTP.
     * También programa una tarea diaria para verificar los servicios de usuario expirados.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     * @throws Exception Si hay un error al iniciar el servidor o conectarse a la base de datos.
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

        // Configurar puerto desde variable de entorno o input de consola
        int port = 8080; // Puerto predeterminado
        
        // Primero verificar si hay variable de entorno
        String envPort = System.getenv("SERVER_PORT");
        if (envPort != null && !envPort.isEmpty()) {
            try {
                port = Integer.parseInt(envPort);
                System.out.println("Usando puerto desde variable de entorno SERVER_PORT: " + port);
            } catch (NumberFormatException e) {
                System.out.println("Variable SERVER_PORT inválida. Se usará el puerto predeterminado 8080.");
            }
        } else {
            // Si no hay variable de entorno, intentar leer de consola solo si System.in está disponible
            try {
                if (System.in.available() > 0 || System.console() != null) {
                    System.out.println("Ingrese el puerto para iniciar el servidor (predeterminado: 8080): ");
                    java.util.Scanner scanner = new java.util.Scanner(System.in);
                    String portInput = scanner.nextLine().trim();
                    
                    if (!portInput.isEmpty()) {
                        try {
                            port = Integer.parseInt(portInput);
                        } catch (NumberFormatException e) {
                            System.out.println("Formato de puerto inválido. Se usará el puerto predeterminado 8080.");
                        }
                    }
                } else {
                    System.out.println("No hay consola disponible. Usando puerto predeterminado: " + port);
                }
            } catch (Exception e) {
                System.out.println("Error al leer puerto de consola. Usando puerto predeterminado: " + port);
            }
        }

        // Verificar servicios expirados al iniciar
        System.out.println("Verificando servicios expirados...");
        int updatedUsers = userDAO.checkAllUsersServiceExpiration();
        System.out.println("Se actualizaron " + updatedUsers + " usuarios con servicios expirados.");
        
        // Programar tarea para verificar servicios expirados cada día
        java.util.Timer timer = new java.util.Timer(true);
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                System.out.println("Ejecutando verificación programada de servicios expirados...");
                int count = userDAO.checkAllUsersServiceExpiration();
                System.out.println("Verificación programada: se actualizaron " + count + " usuarios con servicios expirados.");
            }
        }, 
        // Ejecutar cada 24 horas (en milisegundos)
        86400000, 86400000);

        // Crear y configurar el servidor HTTP
        HttpServer server = HttpServer.create(new InetSocketAddress(ip, port), 0);
        server.createContext("/api/login", new LoginHandler(userDAO));
        server.createContext("/api/users", new UserHandler(userDAO));
        server.createContext("/api/policy", new PolicyHandler(policyDAO));
        server.createContext("/api/appointment", new AppointmentHandler(appointmentDAO));
        server.createContext("/api/appointmentmade", new AppointmentMadeHandler(appointmentMadeDAO));
        server.createContext("/api/category", new CategoryHandler(categoryDAO));
        server.createContext("/api/configurableamount", new ConfigurableAmountHandler(configurableAmountDAO));
        server.createContext("/api/hospital", new HospitalHandler(hospitalDAO));
        server.createContext("/api/medicine", new MedicineHandler(medicineDAO));
        server.createContext("/api/medicinepres", new MedicinePresHandler(medicinePresDAO));
        server.createContext("/api/pharmacy", new PharmacyHandler(pharmacyDAO));
        server.createContext("/api/prescription", new PrescriptionHandler(prescriptionDAO));
        server.createContext("/api/service", new ServiceHandler(serviceDAO));
        server.createContext("/api/totalhospital", new TotalHospitalHandler(totalHospitalDAO));
        server.createContext("/api/totalpharmacy", new TotalPharmacyHandler(totalPharmacyDAO));
        server.createContext("/api/transactions", new TransactionsHandler(transactionsDAO));
        server.createContext("/api/transactionpolicy", new TransactionPolicyHandler(transactionPolicyDAO));
        server.createContext("/api/servicecategory", new ServiceCategoryHandler(serviceCategoryDAO));
        server.createContext("/api/notifications/email", new NotificationHandler());
        // Nuevos endpoints para servicios de seguro y relaciones con hospitales
        server.createContext("/api/insurance-services", new InsuranceServiceHandler(insuranceServiceDAO, categoryDAO));
        server.createContext("/api/hospital-services", new HospitalInsuranceServiceHandler(hospitalInsuranceServiceDAO, hospitalDAO, insuranceServiceDAO));
        // Nuevo handler para la integración con el hospital
        server.createContext("/api/hospital-integration", new HospitalRedirectHandler());
        // Registrar el nuevo handler para buscar usuarios por email
        server.createContext("/api/users/by-email", new UserByEmailHandler(userDAO));
        // Nuevo handler para las citas de seguro
        server.createContext("/api/ensurance-appointments", new EnsuranceAppointmentHandler(ensuranceAppointmentDAO));
        // Handler para aprobaciones de recetas
        server.createContext("/api/prescriptions/", new PrescriptionApprovalHandler(prescriptionApprovalDAO, userDAO, configurableAmountDAO));
        // Actualizar handler para monto configurable
        server.createContext("/api/configurable-amount/", new ConfigurableAmountHandler(configurableAmountDAO));
        // Registrar el nuevo handler para proxy de servicios de hospital
        server.createContext("/api/hospital-proxy", new HospitalServiceProxyHandler(hospitalDAO));
        
        server.setExecutor(null); // Usa el executor por defecto
        server.start();
        System.out.println("Servidor iniciado en http://" + ip + ":" + port + "/api");
    }
}
