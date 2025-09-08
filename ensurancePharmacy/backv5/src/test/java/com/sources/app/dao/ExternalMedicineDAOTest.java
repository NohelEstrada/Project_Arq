package com.sources.app.dao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import com.sources.app.entities.Medicine;

public class ExternalMedicineDAOTest {

    private final ExternalMedicineDAO externalDAO = new ExternalMedicineDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();

    @Test
    public void testGetAllReflectsInsertedMedicinesCount() {
        List<Medicine> before = externalDAO.getAll();
        assertNotNull(before);
        int base = before.size();

        Medicine m1 = medicineDAO.create("DolorOff", "Paracetamol", "Desc 1", "img1", "500mg", 1.0, 100, "Acme", false, 10.5, 0);
        Medicine m2 = medicineDAO.create("Ibufast", "Ibuprofeno", "Desc 2", "img2", "400mg", 1.0, 50, "Beta", true, 15.0, 0);

        assertNotNull(m1);
        assertNotNull(m2);

        List<Medicine> after = externalDAO.getAll();
        assertNotNull(after);
        assertTrue(after.size() >= base + 2, "Expected list size to grow by at least 2 after inserts");
    }

    @Test
    public void testGetAllReturnsInsertedMedicines() {
        Medicine m1 = medicineDAO.create("DolorOff", "Paracetamol", "Desc 1", "img1", "500mg", 1.0, 100, "Acme", false, 10.5, 0);
        Medicine m2 = medicineDAO.create("Ibufast", "Ibuprofeno", "Desc 2", "img2", "400mg", 1.0, 50, "Beta", true, 15.0, 0);

        assertNotNull(m1);
        assertNotNull(m2);

        List<Medicine> meds = externalDAO.getAll();
        assertNotNull(meds);
        assertTrue(meds.size() >= 2, "Expected at least the inserted medicines to be returned");

        boolean hasM1 = meds.stream().anyMatch(m -> "DolorOff".equals(m.getName()));
        boolean hasM2 = meds.stream().anyMatch(m -> "Ibufast".equals(m.getName()));
        assertTrue(hasM1 && hasM2, "Inserted medicines should be present in results");
    }
}

