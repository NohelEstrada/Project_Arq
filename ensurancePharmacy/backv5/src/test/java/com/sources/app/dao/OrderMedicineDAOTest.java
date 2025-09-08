package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.OrderMedicine;
import com.sources.app.entities.OrderMedicineId;
import com.sources.app.entities.Orders;
import com.sources.app.entities.Medicine;

public class OrderMedicineDAOTest {
    private final OrderMedicineDAO dao = new OrderMedicineDAO();
    private final OrdersDAO ordersDAO = new OrdersDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ObjectMapper mapper = new ObjectMapper();

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

        // Crear y persistir un usuario requerido por Orders para que la prueba sea auto-contenida
        var user = userDAO.create(
            "Test User",
            "1234567890101",
            "+50255555555",
            "testuser@example.com",
            new Date(),
            "Test Address",
            "password"
        );
        assertNotNull(user);

        // Persistir las dependencias
        Orders orders = ordersDAO.create("Pending", user.getIdUser());
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

    @Test
    public void testGetAllAndGetById() {
        List<OrderMedicine> before = dao.getAll();
        int base = (before == null) ? 0 : before.size();

        long now = System.currentTimeMillis();
        var user = userDAO.create(
            "User OM1",
            "CUI-OM-" + now,
            "+50255550001",
            "om1-" + now + "@test.com",
            new Date(),
            "Addr OM1",
            "pwd"
        );
        assertNotNull(user);

        Orders orders = ordersDAO.create("Pending", user.getIdUser());
        assertNotNull(orders);

        Medicine medicine = medicineDAO.create(
            "Med OM1", "ActiveMed", "Desc OM1", "image.png",
            "500mg", 5.0, 50, "BrandOM1", false, 4.99, 0
        );
        assertNotNull(medicine);

        OrderMedicine created = dao.create(orders, medicine, 2, 5.0, "10.0");
        assertNotNull(created);

        List<OrderMedicine> after = dao.getAll();
        assertNotNull(after);
        assertTrue(after.size() >= base + 1);

        OrderMedicineId id = new OrderMedicineId(
            created.getOrders().getIdOrder(),
            created.getMedicine().getIdMedicine()
        );
        OrderMedicine byId = dao.getById(id);
        assertNotNull(byId);
        assertEquals(2, byId.getQuantity());
        assertEquals(5.0, byId.getCost());
        assertEquals("10.0", byId.getTotal());
    }

    @Test
    public void testGetByIdNotFoundReturnsNull() {
        OrderMedicineId missing = new OrderMedicineId(-1L, -1L);
        assertNull(dao.getById(missing));
    }

    @Test
    public void testUpdateOrderMedicine() {
        long now = System.currentTimeMillis();
        var user = userDAO.create(
            "User OM2",
            "CUI-OM-" + now,
            "+50255550002",
            "om2-" + now + "@test.com",
            new Date(),
            "Addr OM2",
            "pwd"
        );
        assertNotNull(user);

        Orders orders = ordersDAO.create("Pending", user.getIdUser());
        assertNotNull(orders);

        Medicine medicine = medicineDAO.create(
            "Med OM2", "ActiveOM2", "Desc OM2", "image.png",
            "250mg", 7.0, 25, "BrandOM2", false, 6.99, 0
        );
        assertNotNull(medicine);

        OrderMedicine created = dao.create(orders, medicine, 1, 2.5, "2.5");
        assertNotNull(created);

        // mutate and update
        created.setQuantity(5);
        created.setCost(4.0);
        created.setTotal("20.0");

        OrderMedicine updated = dao.update(created);
        assertNotNull(updated);
        assertEquals(5, updated.getQuantity());
        assertEquals(4.0, updated.getCost());
        assertEquals("20.0", updated.getTotal());

        // reload using composite id and verify
        OrderMedicineId id = new OrderMedicineId(
            created.getOrders().getIdOrder(),
            created.getMedicine().getIdMedicine()
        );
        OrderMedicine reloaded = dao.getById(id);
        assertNotNull(reloaded);
        assertEquals(5, reloaded.getQuantity());
        assertEquals(4.0, reloaded.getCost());
        assertEquals("20.0", reloaded.getTotal());
    }

    @Test
    public void testDeleteByIdSuccessAndNotFound() {
        long now = System.currentTimeMillis();
        var user = userDAO.create(
            "User OM3",
            "CUI-OM-" + now,
            "+50255550003",
            "om3-" + now + "@test.com",
            new Date(),
            "Addr OM3",
            "pwd"
        );
        assertNotNull(user);

        Orders orders = ordersDAO.create("Pending", user.getIdUser());
        assertNotNull(orders);

        Medicine medicine = medicineDAO.create(
            "Med OM3", "ActiveOM3", "Desc OM3", "image.png",
            "750mg", 8.0, 75, "BrandOM3", false, 7.49, 0
        );
        assertNotNull(medicine);

        OrderMedicine created = dao.create(orders, medicine, 3, 8.0, "24.0");
        assertNotNull(created);

        OrderMedicineId id = new OrderMedicineId(
            created.getOrders().getIdOrder(),
            created.getMedicine().getIdMedicine()
        );

        boolean deleted = dao.deleteById(id);
        assertTrue(deleted);
        assertNull(dao.getById(id));

        // deleting again should return false
        assertFalse(dao.deleteById(id));

        // deleting a clearly non-existing composite key should also return false
        assertFalse(dao.deleteById(new OrderMedicineId(-99L, -88L)));
    }
}