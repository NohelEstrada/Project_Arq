package com.sources.app.dao;

import com.sources.app.entities.Hospital;
import com.sources.app.entities.HospitalInsuranceService;
import com.sources.app.entities.InsuranceService;
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
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospitalInsuranceServiceDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<HospitalInsuranceService> mockQuery;
    @Mock
    private Hospital mockHospital;
    @Mock
    private InsuranceService mockInsuranceService;
    @Mock
    private TransactionStatus mockTransactionStatus;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private HospitalInsuranceServiceDAO hospitalInsuranceServiceDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockTransaction.getStatus()).thenReturn(mockTransactionStatus);
        lenient().when(mockTransactionStatus.canRollback()).thenReturn(true);
        lenient().when(mockSession.createQuery(anyString(), eq(HospitalInsuranceService.class))).thenReturn(mockQuery);
        lenient().when(mockSession.isOpen()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void approveService_NewRelation() {
        // Arrange
        String notes = "Approved notes";
        when(mockQuery.setParameter(eq("hospital"), eq(mockHospital))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("service"), eq(mockInsuranceService))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenReturn(null); // Simulate relation doesn't exist

        // Act
        HospitalInsuranceService result = hospitalInsuranceServiceDAO.approveService(mockHospital, mockInsuranceService, notes);

        // Assert
        assertNotNull(result);
        assertEquals(mockHospital, result.getHospital());
        assertEquals(mockInsuranceService, result.getInsuranceService());
        assertEquals(1, result.getApproved());
        assertNotNull(result.getApprovalDate());
        assertEquals(notes, result.getNotes());
        verify(mockSession).saveOrUpdate(any(HospitalInsuranceService.class)); // Should save new
        verify(mockTransaction).commit();
        verify(mockSession).close();
    }

    @Test
    void approveService_UpdateExistingRelation() {
        // Arrange
        String notes = "Updated approval notes";
        HospitalInsuranceService existingRelation = new HospitalInsuranceService();
        existingRelation.setApproved(0); // Was previously not approved
        existingRelation.setHospital(mockHospital);
        existingRelation.setInsuranceService(mockInsuranceService);
        
        when(mockQuery.setParameter(eq("hospital"), eq(mockHospital))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("service"), eq(mockInsuranceService))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenReturn(existingRelation); // Simulate relation exists

        // Act
        HospitalInsuranceService result = hospitalInsuranceServiceDAO.approveService(mockHospital, mockInsuranceService, notes);

        // Assert
        assertNotNull(result);
        assertEquals(existingRelation, result); // Should be the same object instance
        assertEquals(1, result.getApproved());
        assertNotNull(result.getApprovalDate());
        assertEquals(notes, result.getNotes());
        verify(mockSession).saveOrUpdate(existingRelation); // Should update existing
        verify(mockTransaction).commit();
         verify(mockSession).close();
    }
    
    @Test
    void approveService_Exception() {
        // Arrange
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenThrow(new RuntimeException("DB Query Error"));
        
        // Act
        HospitalInsuranceService result = hospitalInsuranceServiceDAO.approveService(mockHospital, mockInsuranceService, "notes");

        // Assert
        assertNull(result);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
         verify(mockSession).close();
    }

    @Test
    void revokeApproval_Success() {
        // Arrange
        HospitalInsuranceService existingRelation = new HospitalInsuranceService();
        existingRelation.setApproved(1);
        when(mockQuery.setParameter(eq("hospital"), eq(mockHospital))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("service"), eq(mockInsuranceService))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenReturn(existingRelation);

        // Act
        boolean result = hospitalInsuranceServiceDAO.revokeApproval(mockHospital, mockInsuranceService);

        // Assert
        assertTrue(result);
        assertEquals(0, existingRelation.getApproved()); // Check state changed
        verify(mockSession).update(existingRelation);
        verify(mockTransaction).commit();
         verify(mockSession).close();
    }

    @Test
    void revokeApproval_RelationNotFound() {
        // Arrange
        when(mockQuery.setParameter(eq("hospital"), eq(mockHospital))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("service"), eq(mockInsuranceService))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenReturn(null);

        // Act
        boolean result = hospitalInsuranceServiceDAO.revokeApproval(mockHospital, mockInsuranceService);

        // Assert
        assertFalse(result);
        verify(mockSession, never()).update(any());
        verify(mockTransaction, never()).commit(); // No commit if not found
         verify(mockSession).close();
    }

    @Test
    void revokeApproval_Exception() {
        // Arrange
         when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenThrow(new RuntimeException("DB Query Error"));

        // Act
        boolean result = hospitalInsuranceServiceDAO.revokeApproval(mockHospital, mockInsuranceService);

        // Assert
        assertFalse(result);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
         verify(mockSession).close();
    }

    @Test
    void findApprovedByHospital_Success() {
        // Arrange
        List<HospitalInsuranceService> expectedList = Collections.singletonList(new HospitalInsuranceService());
        when(mockQuery.setParameter(eq("hospital"), eq(mockHospital))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<HospitalInsuranceService> result = hospitalInsuranceServiceDAO.findApprovedByHospital(mockHospital);

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM HospitalInsuranceService WHERE hospital = :hospital AND approved = 1", HospitalInsuranceService.class);
        verify(mockQuery).setParameter("hospital", mockHospital);
    }
    
    @Test
    void findApprovedByHospital_Exception() {
        // Arrange
        when(mockQuery.setParameter(eq("hospital"), eq(mockHospital))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<HospitalInsuranceService> result = hospitalInsuranceServiceDAO.findApprovedByHospital(mockHospital);

        // Assert
        assertNull(result);
    }

    @Test
    void findHospitalsByService_Success() {
        // Arrange
        List<HospitalInsuranceService> expectedList = Collections.singletonList(new HospitalInsuranceService());
        when(mockQuery.setParameter(eq("service"), eq(mockInsuranceService))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<HospitalInsuranceService> result = hospitalInsuranceServiceDAO.findHospitalsByService(mockInsuranceService);

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM HospitalInsuranceService WHERE insuranceService = :service AND approved = 1", HospitalInsuranceService.class);
        verify(mockQuery).setParameter("service", mockInsuranceService);
    }
    
    @Test
    void findHospitalsByService_Exception() {
        // Arrange
        when(mockQuery.setParameter(eq("service"), eq(mockInsuranceService))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<HospitalInsuranceService> result = hospitalInsuranceServiceDAO.findHospitalsByService(mockInsuranceService);

        // Assert
        assertNull(result);
    }

    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        HospitalInsuranceService expectedRelation = new HospitalInsuranceService();
        when(mockSession.get(HospitalInsuranceService.class, id)).thenReturn(expectedRelation);

        // Act
        HospitalInsuranceService result = hospitalInsuranceServiceDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedRelation, result);
        verify(mockSession).get(HospitalInsuranceService.class, id);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(HospitalInsuranceService.class, id)).thenReturn(null);

        // Act
        HospitalInsuranceService result = hospitalInsuranceServiceDAO.findById(id);

        // Assert
        assertNull(result);
    }
    
    @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(HospitalInsuranceService.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        HospitalInsuranceService result = hospitalInsuranceServiceDAO.findById(id);

        // Assert
        assertNull(result);
    }

    @Test
    void delete_Success() {
        // Arrange
        Long id = 1L;
        HospitalInsuranceService relationToDelete = new HospitalInsuranceService();
        when(mockSession.get(HospitalInsuranceService.class, id)).thenReturn(relationToDelete);

        // Act
        boolean result = hospitalInsuranceServiceDAO.delete(id);

        // Assert
        assertTrue(result);
        verify(mockSession).get(HospitalInsuranceService.class, id);
        verify(mockSession).delete(relationToDelete);
        verify(mockTransaction).commit();
         verify(mockSession).close();
    }

    @Test
    void delete_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(HospitalInsuranceService.class, id)).thenReturn(null);

        // Act
        boolean result = hospitalInsuranceServiceDAO.delete(id);

        // Assert
        assertFalse(result);
        verify(mockSession).get(HospitalInsuranceService.class, id);
        verify(mockSession, never()).delete(any());
        verify(mockTransaction, never()).commit();
         verify(mockSession).close();
    }

    @Test
    void delete_Exception() {
        // Arrange
        Long id = 1L;
        HospitalInsuranceService relationToDelete = new HospitalInsuranceService();
        when(mockSession.get(HospitalInsuranceService.class, id)).thenReturn(relationToDelete);
        doThrow(new RuntimeException("DB Delete Error")).when(mockSession).delete(any(HospitalInsuranceService.class));

        // Act
        boolean result = hospitalInsuranceServiceDAO.delete(id);

        // Assert
        assertFalse(result);
        verify(mockSession).delete(relationToDelete);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
         verify(mockSession).close();
    }
} 