package com.sources.app.dao;

import com.sources.app.entities.Pharmacy;
import com.sources.app.entities.TotalPharmacy;
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
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TotalPharmacyDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<TotalPharmacy> mockQuery;
    @Mock
    private Pharmacy mockPharmacy;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private TotalPharmacyDAO totalPharmacyDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockSession.createQuery(anyString(), eq(TotalPharmacy.class))).thenReturn(mockQuery);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success() {
        // Arrange
        Long pharmacyId = 1L;
        Date totalDate = new Date();
        BigDecimal total = new BigDecimal("500.25");
        when(mockSession.get(Pharmacy.class, pharmacyId)).thenReturn(mockPharmacy);

        // Act
        TotalPharmacy result = totalPharmacyDAO.create(pharmacyId, totalDate, total);

        // Assert
        assertNotNull(result);
        assertEquals(mockPharmacy, result.getPharmacy());
        assertEquals(totalDate, result.getTotalDate());
        assertEquals(total, result.getTotal());
        verify(mockSession).save(any(TotalPharmacy.class));
        verify(mockTransaction).commit();
    }

    @Test
    void create_PharmacyNotFound() {
        // Arrange
        Long pharmacyId = 1L;
        when(mockSession.get(Pharmacy.class, pharmacyId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            totalPharmacyDAO.create(pharmacyId, new Date(), BigDecimal.ONE);
        });
        assertTrue(exception.getMessage().contains("Pharmacy no encontrada"));
        verify(mockSession).get(Pharmacy.class, pharmacyId);
        verify(mockSession, never()).save(any());
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void create_ExceptionDuringSave() {
        // Arrange
        Long pharmacyId = 1L;
        when(mockSession.get(Pharmacy.class, pharmacyId)).thenReturn(mockPharmacy);
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(TotalPharmacy.class));

        // Act
        TotalPharmacy result = totalPharmacyDAO.create(pharmacyId, new Date(), BigDecimal.ONE);

        // Assert
        assertNull(result); // Returns null
        verify(mockSession).save(any(TotalPharmacy.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        TotalPharmacy expectedTotal = new TotalPharmacy();
        when(mockSession.get(TotalPharmacy.class, id)).thenReturn(expectedTotal);

        // Act
        TotalPharmacy result = totalPharmacyDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedTotal, result);
        verify(mockSession).get(TotalPharmacy.class, id);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(TotalPharmacy.class, id)).thenReturn(null);

        // Act
        TotalPharmacy result = totalPharmacyDAO.findById(id);

        // Assert
        assertNull(result);
    }
    
    @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(TotalPharmacy.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        TotalPharmacy result = totalPharmacyDAO.findById(id);

        // Assert
        assertNull(result);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<TotalPharmacy> expectedList = Collections.singletonList(new TotalPharmacy());
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<TotalPharmacy> result = totalPharmacyDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM TotalPharmacy", TotalPharmacy.class);
    }
    
     @Test
    void findAll_Exception() {
        // Arrange
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<TotalPharmacy> result = totalPharmacyDAO.findAll();

        // Assert
        assertNull(result);
    }

    @Test
    void update_Success() {
        // Arrange
        TotalPharmacy tpToUpdate = new TotalPharmacy();

        // Act
        TotalPharmacy result = totalPharmacyDAO.update(tpToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(tpToUpdate, result);
        verify(mockSession).update(tpToUpdate);
        verify(mockTransaction).commit();
    }

    @Test
    void update_Exception() {
        // Arrange
        TotalPharmacy tpToUpdate = new TotalPharmacy();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(TotalPharmacy.class));

        // Act
        TotalPharmacy result = totalPharmacyDAO.update(tpToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(tpToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
} 