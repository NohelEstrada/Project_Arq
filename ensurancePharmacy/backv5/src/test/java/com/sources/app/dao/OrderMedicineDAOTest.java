package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.OrderMedicine;
import com.sources.app.entities.Orders;
import com.sources.app.entities.Medicine;

public class OrderMedicineDAOTest {
    private OrderMedicineDAO dao = new OrderMedicineDAO();
    private OrdersDAO ordersDAO = new OrdersDAO();
    private MedicineDAO medicineDAO = new MedicineDAO();
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testOrderMedicineFromJsonFile() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/orderMedicine.json");
        assertNotNull(is);
        OrderMedicine omFromJson = mapper.readValue(is, OrderMedicine.class);
        assertNotNull(omFromJson);
        assertEquals(3, omFromJson.getQuantity());
        assertEquals(100.0, omFromJson.getCost());
        assertEquals("300.0", omFromJson.getTotal());
    }

    @Test
    public void testCreateOrderMedicineFromJsonWithDependencies() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/orderMedicine.json");
        assertNotNull(is);
        OrderMedicine omFromJson = mapper.readValue(is, OrderMedicine.class);
        assertNotNull(omFromJson);

        // Persistir las dependencias
        Orders orders = ordersDAO.create("Pending", 1L);
        assertNotNull(orders);
        Medicine medicine = medicineDAO.create("Test Medicine", "ActiveMed", "Test Desc", "image.png",
                "500mg", 10.0, 100, "TestBrand", false, 9.99, 0);
        assertNotNull(medicine);

        // Crear OrderMedicine usando las dependencias persistidas
        OrderMedicine created = dao.create(orders, medicine, omFromJson.getQuantity(), omFromJson.getCost(), omFromJson.getTotal());
        assertNotNull(created);
        assertNotNull(created.getOrders());
        assertNotNull(created.getMedicine());
        assertEquals(omFromJson.getQuantity(), created.getQuantity());
        assertEquals(omFromJson.getCost(), created.getCost());
        assertEquals(omFromJson.getTotal(), created.getTotal());
    }
}