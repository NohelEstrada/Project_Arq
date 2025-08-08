package com.sources.app.dao;

import com.sources.app.entities.Medicine;
import com.sources.app.entities.Pharmacy;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<Medicine> mockQuery;
    @Mock
    private Pharmacy mockPharmacy;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private MedicineDAO medicineDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockSession.createQuery(anyString(), eq(Medicine.class))).thenReturn(mockQuery);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success() {
        // Arrange
        String name = "Aspirin";
        String description = "Pain reliever";
        BigDecimal price = new BigDecimal("9.99");
        Integer enabled = 1;
        String activePrinciple = "Acetylsalicylic Acid";
        String presentation = "Tablet";
        Integer stock = 100;
        String brand = "Bayer";
        Integer coverage = 1;

        // Act
        Medicine result = medicineDAO.create(name, description, price, mockPharmacy,
                enabled, activePrinciple, presentation, stock, brand, coverage);

        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
        assertEquals(price, result.getPrice());
        assertEquals(mockPharmacy, result.getPharmacy());
        assertEquals(enabled, result.getEnabled());
        // ... assert other fields
        verify(mockSession).save(any(Medicine.class));
        verify(mockTransaction).commit();
    }

    @Test
    void create_ExceptionDuringSave() {
        // Arrange
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(Medicine.class));

        // Act
        Medicine result = medicineDAO.create("N", "D", BigDecimal.ZERO, mockPharmacy,
                1, "AP", "P", 10, "B", 1);

        // Assert
        assertNull(result); // Returns null
        verify(mockSession).save(any(Medicine.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        Medicine expectedMedicine = new Medicine();
        when(mockSession.get(Medicine.class, id)).thenReturn(expectedMedicine);

        // Act
        Medicine result = medicineDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedMedicine, result);
        verify(mockSession).get(Medicine.class, id);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Medicine.class, id)).thenReturn(null);

        // Act
        Medicine result = medicineDAO.findById(id);

        // Assert
        assertNull(result);
    }
    
     @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Medicine.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        Medicine result = medicineDAO.findById(id);

        // Assert
        assertNull(result);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<Medicine> expectedList = Collections.singletonList(new Medicine());
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Medicine> result = medicineDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM Medicine", Medicine.class);
    }
    
     @Test
    void findAll_Exception() {
        // Arrange
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<Medicine> result = medicineDAO.findAll();

        // Assert
        assertNull(result);
    }

    @Test
    void update_Success() {
        // Arrange
        Medicine medicineToUpdate = new Medicine();

        // Act
        Medicine result = medicineDAO.update(medicineToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(medicineToUpdate, result);
        verify(mockSession).update(medicineToUpdate);
        verify(mockTransaction).commit();
    }

    @Test
    void update_Exception() {
        // Arrange
        Medicine medicineToUpdate = new Medicine();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(Medicine.class));

        // Act
        Medicine result = medicineDAO.update(medicineToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(medicineToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
} 