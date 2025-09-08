package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Orders;
import com.sources.app.entities.User;

public class OrdersDAOTest {
    private OrdersDAO ordersDAO = new OrdersDAO();
    private UserDAO userDAO = new UserDAO();
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testOrdersFromJsonFile() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/orders.json");
        assertNotNull(is);
        Orders ordersFromJson = mapper.readValue(is, Orders.class);
        assertNotNull(ordersFromJson);
        assertEquals("Pending", ordersFromJson.getStatus());
    }

    @Test
    public void testCreateOrdersFromJson() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/orders.json");
        assertNotNull(is);
        Orders ordersFromJson = mapper.readValue(is, Orders.class);
        // Crear un usuario real para asociar el pedido y evitar dependencia de IDs existentes
        User u = userDAO.create("User Orders", "CUI-1", "555-1000", "o@test.com", new java.util.Date(), "Addr O", "pwd");
        assertNotNull(u);
        Orders created = ordersDAO.create(ordersFromJson.getStatus(), u.getIdUser());
        assertNotNull(created);
        assertNotNull(created.getIdOrder());
        assertEquals("Pending", created.getStatus());
    }

    @Test
    public void testGetAllGetByIdAndUpdate() {
        java.util.List<Orders> before = ordersDAO.getAll();
        int base = before == null ? 0 : before.size();

        User u = userDAO.create("User B", "CUI-2", "555-2000", "b@test.com", new java.util.Date(), "Addr B", "pwd");
        assertNotNull(u);

        Orders created = ordersDAO.create("Pending", u.getIdUser());
        assertNotNull(created);

        java.util.List<Orders> after = ordersDAO.getAll();
        assertNotNull(after);
        assertTrue(after.size() >= base + 1);

        Orders byId = ordersDAO.getById(created.getIdOrder());
        assertNotNull(byId);
        assertEquals("Pending", byId.getStatus());

        // Update status and verify persistence
        byId.setStatus("Shipped");
        Orders updated = ordersDAO.update(byId);
        assertNotNull(updated);
        assertEquals("Shipped", updated.getStatus());

        Orders reloaded = ordersDAO.getById(created.getIdOrder());
        assertNotNull(reloaded);
        assertEquals("Shipped", reloaded.getStatus());
    }

    @Test
    public void testGetByIdNegativeReturnsNull() {
        assertNull(ordersDAO.getById(-1L));
    }
}