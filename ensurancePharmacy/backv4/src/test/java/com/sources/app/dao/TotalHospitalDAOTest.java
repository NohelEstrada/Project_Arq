package com.sources.app.dao;

import com.sources.app.entities.Hospital;
import com.sources.app.entities.TotalHospital;
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
class TotalHospitalDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<TotalHospital> mockQuery;
    @Mock
    private Hospital mockHospital;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private TotalHospitalDAO totalHospitalDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockSession.createQuery(anyString(), eq(TotalHospital.class))).thenReturn(mockQuery);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success() {
        // Arrange
        Long hospitalId = 1L;
        Date totalDate = new Date();
        BigDecimal total = new BigDecimal("1000.50");
        when(mockSession.get(Hospital.class, hospitalId)).thenReturn(mockHospital);

        // Act
        TotalHospital result = totalHospitalDAO.create(hospitalId, totalDate, total);

        // Assert
        assertNotNull(result);
        assertEquals(mockHospital, result.getHospital());
        assertEquals(totalDate, result.getTotalDate());
        assertEquals(total, result.getTotal());
        verify(mockSession).save(any(TotalHospital.class));
        verify(mockTransaction).commit();
    }

    @Test
    void create_HospitalNotFound() {
        // Arrange
        Long hospitalId = 1L;
        when(mockSession.get(Hospital.class, hospitalId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            totalHospitalDAO.create(hospitalId, new Date(), BigDecimal.TEN);
        });
        assertTrue(exception.getMessage().contains("Hospital no encontrado"));
        verify(mockSession).get(Hospital.class, hospitalId);
        verify(mockSession, never()).save(any());
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void create_ExceptionDuringSave() {
        // Arrange
        Long hospitalId = 1L;
        when(mockSession.get(Hospital.class, hospitalId)).thenReturn(mockHospital);
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(TotalHospital.class));

        // Act
        TotalHospital result = totalHospitalDAO.create(hospitalId, new Date(), BigDecimal.TEN);

        // Assert
        assertNull(result); // Returns null
        verify(mockSession).save(any(TotalHospital.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        TotalHospital expectedTotal = new TotalHospital();
        when(mockSession.get(TotalHospital.class, id)).thenReturn(expectedTotal);

        // Act
        TotalHospital result = totalHospitalDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedTotal, result);
        verify(mockSession).get(TotalHospital.class, id);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(TotalHospital.class, id)).thenReturn(null);

        // Act
        TotalHospital result = totalHospitalDAO.findById(id);

        // Assert
        assertNull(result);
    }
    
    @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(TotalHospital.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        TotalHospital result = totalHospitalDAO.findById(id);

        // Assert
        assertNull(result);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<TotalHospital> expectedList = Collections.singletonList(new TotalHospital());
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<TotalHospital> result = totalHospitalDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM TotalHospital", TotalHospital.class);
    }
    
     @Test
    void findAll_Exception() {
        // Arrange
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<TotalHospital> result = totalHospitalDAO.findAll();

        // Assert
        assertNull(result);
    }

    @Test
    void update_Success() {
        // Arrange
        TotalHospital thToUpdate = new TotalHospital();

        // Act
        TotalHospital result = totalHospitalDAO.update(thToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(thToUpdate, result);
        verify(mockSession).update(thToUpdate);
        verify(mockTransaction).commit();
    }

    @Test
    void update_Exception() {
        // Arrange
        TotalHospital thToUpdate = new TotalHospital();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(TotalHospital.class));

        // Act
        TotalHospital result = totalHospitalDAO.update(thToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(thToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
} 