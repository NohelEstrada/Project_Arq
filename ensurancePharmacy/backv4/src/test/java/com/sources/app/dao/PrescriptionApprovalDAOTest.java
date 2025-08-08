package com.sources.app.dao;

import com.sources.app.entities.PrescriptionApproval;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionApprovalDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<PrescriptionApproval> mockQuery;
    @Mock
    private TransactionStatus mockTransactionStatus;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private PrescriptionApprovalDAO prescriptionApprovalDAO;

    @BeforeEach
    void setUp() {
        // Mock the static HibernateUtil.getSessionFactory() method
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        
        // Standard mocking for session factory, session, and transaction
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        // Stub getStatus() globally
        lenient().when(mockTransaction.getStatus()).thenReturn(mockTransactionStatus);
        // Assume it can rollback by default for tests that might need it
        lenient().when(mockTransactionStatus.canRollback()).thenReturn(true); 
    }

    @AfterEach
    void tearDown() {
        // Close the static mock
        mockedHibernateUtil.close();
    }

    @Test
    void save_Success() {
        // Arrange
        PrescriptionApproval approval = new PrescriptionApproval();
        // Add necessary setters for approval object
        
        // Act
        PrescriptionApproval result = prescriptionApprovalDAO.save(approval);

        // Assert
        assertNotNull(result);
        // Add more specific assertions based on expected behavior
        verify(mockSession).save(approval);
        verify(mockTransaction).commit();
    }
    
    @Test
    void save_ExceptionRollback() {
        // Arrange
        PrescriptionApproval approval = new PrescriptionApproval();
        doThrow(new RuntimeException("DB error")).when(mockSession).save(any(PrescriptionApproval.class));

        // Act
        PrescriptionApproval result = prescriptionApprovalDAO.save(approval);

        // Assert
        assertNull(result);
        verify(mockSession).save(approval);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }


    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        PrescriptionApproval expectedApproval = new PrescriptionApproval();
        // Set properties for expectedApproval
        when(mockSession.get(PrescriptionApproval.class, id)).thenReturn(expectedApproval);

        // Act
        PrescriptionApproval result = prescriptionApprovalDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedApproval, result);
        verify(mockSession).get(PrescriptionApproval.class, id);
    }
    
    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(PrescriptionApproval.class, id)).thenReturn(null);

        // Act
        PrescriptionApproval result = prescriptionApprovalDAO.findById(id);

        // Assert
        assertNull(result);
        verify(mockSession).get(PrescriptionApproval.class, id);
    }
    
    @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(PrescriptionApproval.class, id)).thenThrow(new RuntimeException("DB error"));

        // Act
        PrescriptionApproval result = prescriptionApprovalDAO.findById(id);

        // Assert
        assertNull(result);
        verify(mockSession).get(PrescriptionApproval.class, id);
    }


    @Test
    void findAll_Success() {
        // Arrange
        List<PrescriptionApproval> expectedList = Collections.singletonList(new PrescriptionApproval());
        when(mockSession.createQuery(anyString(), eq(PrescriptionApproval.class))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<PrescriptionApproval> result = prescriptionApprovalDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM PrescriptionApproval ORDER BY approvalDate DESC", PrescriptionApproval.class);
    }
    
    @Test
    void findAll_Exception() {
        // Arrange
         when(mockSession.createQuery(anyString(), eq(PrescriptionApproval.class))).thenThrow(new RuntimeException("DB error"));

        // Act
        List<PrescriptionApproval> result = prescriptionApprovalDAO.findAll();

        // Assert
        assertNull(result);
         verify(mockSession).createQuery("FROM PrescriptionApproval ORDER BY approvalDate DESC", PrescriptionApproval.class);
    }

    @Test
    void findByUserId_Success() {
        // Arrange
        Long userId = 1L;
        List<PrescriptionApproval> expectedList = Collections.singletonList(new PrescriptionApproval());
        when(mockSession.createQuery(anyString(), eq(PrescriptionApproval.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("userId"), eq(userId))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<PrescriptionApproval> result = prescriptionApprovalDAO.findByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM PrescriptionApproval WHERE idUser = :userId ORDER BY approvalDate DESC", PrescriptionApproval.class);
        verify(mockQuery).setParameter("userId", userId);
    }
    
     @Test
    void findByUserId_Exception() {
        // Arrange
        Long userId = 1L;
        when(mockSession.createQuery(anyString(), eq(PrescriptionApproval.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("userId"), eq(userId))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB error"));


        // Act
        List<PrescriptionApproval> result = prescriptionApprovalDAO.findByUserId(userId);

        // Assert
        assertNull(result);
        verify(mockSession).createQuery("FROM PrescriptionApproval WHERE idUser = :userId ORDER BY approvalDate DESC", PrescriptionApproval.class);
        verify(mockQuery).setParameter("userId", userId);
    }
} 