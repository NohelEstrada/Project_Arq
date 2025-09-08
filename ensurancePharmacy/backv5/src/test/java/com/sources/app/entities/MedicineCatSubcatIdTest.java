package com.sources.app.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MedicineCatSubcatIdTest {

    @Test
    public void testMedicineCatSubcatIdInstantiation() {
        MedicineCatSubcatId instance = new MedicineCatSubcatId();
        assertNotNull(instance);
    }

    @Test
    public void testMedicineCatSubcatIdConstructorWithParameters() {
        Long medicineId = 1L;
        Long categoryId = 2L;
        Long subcategoryId = 3L;
        MedicineCatSubcatId id = new MedicineCatSubcatId(medicineId, categoryId, subcategoryId);
        
        assertEquals(medicineId, id.getMedicineId());
        assertEquals(categoryId, id.getCategoryId());
        assertEquals(subcategoryId, id.getSubcategoryId());
    }

    @Test
    public void testMedicineCatSubcatIdSettersAndGetters() {
        MedicineCatSubcatId id = new MedicineCatSubcatId();
        Long medicineId = 10L;
        Long categoryId = 20L;
        Long subcategoryId = 30L;
        
        id.setMedicineId(medicineId);
        id.setCategoryId(categoryId);
        id.setSubcategoryId(subcategoryId);
        
        assertEquals(medicineId, id.getMedicineId());
        assertEquals(categoryId, id.getCategoryId());
        assertEquals(subcategoryId, id.getSubcategoryId());
    }

    @Test
    public void testMedicineCatSubcatIdEquals() {
        MedicineCatSubcatId id1 = new MedicineCatSubcatId(1L, 2L, 3L);
        MedicineCatSubcatId id2 = new MedicineCatSubcatId(1L, 2L, 3L);
        MedicineCatSubcatId id3 = new MedicineCatSubcatId(2L, 3L, 4L);
        
        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertEquals(id1, id1);
        assertNotEquals(id1, null);
        assertNotEquals(id1, "not a MedicineCatSubcatId");
    }

    @Test
    public void testMedicineCatSubcatIdHashCode() {
        MedicineCatSubcatId id1 = new MedicineCatSubcatId(1L, 2L, 3L);
        MedicineCatSubcatId id2 = new MedicineCatSubcatId(1L, 2L, 3L);
        MedicineCatSubcatId id3 = new MedicineCatSubcatId(2L, 3L, 4L);
        
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1.hashCode(), id3.hashCode());
    }

    @Test
    public void testMedicineCatSubcatIdWithNullValues() {
        MedicineCatSubcatId id1 = new MedicineCatSubcatId(null, null, null);
        MedicineCatSubcatId id2 = new MedicineCatSubcatId(null, null, null);
        MedicineCatSubcatId id3 = new MedicineCatSubcatId(1L, null, null);
        
        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertEquals(id1.hashCode(), id2.hashCode());
    }
}
