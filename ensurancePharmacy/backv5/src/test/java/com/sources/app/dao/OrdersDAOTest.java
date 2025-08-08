package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Orders;

public class OrdersDAOTest {
    private OrdersDAO ordersDAO = new OrdersDAO();
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
        Orders created = ordersDAO.create(ordersFromJson.getStatus(), 1L);
        assertNotNull(created);
        assertNotNull(created.getIdOrder());
        assertEquals("Pending", created.getStatus());
    }
}