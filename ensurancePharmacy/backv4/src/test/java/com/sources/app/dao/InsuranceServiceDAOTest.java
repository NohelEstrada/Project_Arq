package com.sources.app.dao;

import com.sources.app.entities.Category;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsuranceServiceDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<InsuranceService> mockQuery;
    @Mock
    private Category mockCategory;
    @Mock
    private Category mockSubcategory;
    @Mock
    private TransactionStatus mockTransactionStatus;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private InsuranceServiceDAO insuranceServiceDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockTransaction.getStatus()).thenReturn(mockTransactionStatus);
        lenient().when(mockTransactionStatus.canRollback()).thenReturn(true);
        lenient().when(mockSession.createQuery(anyString(), eq(InsuranceService.class))).thenReturn(mockQuery);
        lenient().when(mockSession.isOpen()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success() {
        // Arrange
        InsuranceService serviceToCreate = new InsuranceService();
        
        // Act
        InsuranceService result = insuranceServiceDAO.create(serviceToCreate);

        // Assert
        assertNotNull(result);
        assertEquals(serviceToCreate, result);
        verify(mockSession).save(serviceToCreate);
        verify(mockTransaction).commit();
        verify(mockSession).close(); // Verify close
    }

    @Test
    void create_Exception() {
        // Arrange
        InsuranceService serviceToCreate = new InsuranceService();
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(InsuranceService.class));

        // Act
        InsuranceService result = insuranceServiceDAO.create(serviceToCreate);

        // Assert
        assertNull(result);
        verify(mockSession).save(serviceToCreate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
        verify(mockSession).close(); // Verify close
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<InsuranceService> expectedList = Collections.singletonList(new InsuranceService());
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<InsuranceService> result = insuranceServiceDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM InsuranceService", InsuranceService.class);
    }

    @Test
    void findAll_Exception() {
        // Arrange
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<InsuranceService> result = insuranceServiceDAO.findAll();

        // Assert
        assertNull(result);
    }

    @Test
    void findByCategory_Success() {
        // Arrange
        List<InsuranceService> expectedList = Collections.singletonList(new InsuranceService());
        when(mockQuery.setParameter(eq("category"), eq(mockCategory))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<InsuranceService> result = insuranceServiceDAO.findByCategory(mockCategory);

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM InsuranceService WHERE category = :category", InsuranceService.class);
        verify(mockQuery).setParameter("category", mockCategory);
    }

    @Test
    void findByCategory_Exception() {
        // Arrange
        when(mockQuery.setParameter(eq("category"), eq(mockCategory))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<InsuranceService> result = insuranceServiceDAO.findByCategory(mockCategory);

        // Assert
        assertNull(result);
    }
    
    @Test
    void findBySubcategory_Success() {
        // Arrange
        List<InsuranceService> expectedList = Collections.singletonList(new InsuranceService());
        when(mockQuery.setParameter(eq("subcategory"), eq(mockSubcategory))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<InsuranceService> result = insuranceServiceDAO.findBySubcategory(mockSubcategory);

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM InsuranceService WHERE subcategory = :subcategory", InsuranceService.class);
        verify(mockQuery).setParameter("subcategory", mockSubcategory);
    }

    @Test
    void findBySubcategory_Exception() {
        // Arrange
        when(mockQuery.setParameter(eq("subcategory"), eq(mockSubcategory))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<InsuranceService> result = insuranceServiceDAO.findBySubcategory(mockSubcategory);

        // Assert
        assertNull(result);
    }

    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        InsuranceService expectedService = new InsuranceService();
        when(mockSession.get(InsuranceService.class, id)).thenReturn(expectedService);

        // Act
        InsuranceService result = insuranceServiceDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedService, result);
        verify(mockSession).get(InsuranceService.class, id);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(InsuranceService.class, id)).thenReturn(null);

        // Act
        InsuranceService result = insuranceServiceDAO.findById(id);

        // Assert
        assertNull(result);
    }
    
     @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(InsuranceService.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        InsuranceService result = insuranceServiceDAO.findById(id);

        // Assert
        assertNull(result);
    }

    @Test
    void update_Success() {
        // Arrange
        InsuranceService serviceToUpdate = new InsuranceService();

        // Act
        InsuranceService result = insuranceServiceDAO.update(serviceToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(serviceToUpdate, result);
        verify(mockSession).update(serviceToUpdate);
        verify(mockTransaction).commit();
        verify(mockSession).close(); // Verify close
    }

    @Test
    void update_Exception() {
        // Arrange
        InsuranceService serviceToUpdate = new InsuranceService();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(InsuranceService.class));

        // Act
        InsuranceService result = insuranceServiceDAO.update(serviceToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(serviceToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
        verify(mockSession).close(); // Verify close
    }

    @Test
    void delete_Success() {
        // Arrange
        Long id = 1L;
        InsuranceService serviceToDelete = new InsuranceService();
        when(mockSession.get(InsuranceService.class, id)).thenReturn(serviceToDelete);

        // Act
        boolean result = insuranceServiceDAO.delete(id);

        // Assert
        assertTrue(result);
        verify(mockSession).get(InsuranceService.class, id);
        verify(mockSession).delete(serviceToDelete);
        verify(mockTransaction).commit();
        verify(mockSession).close(); // Verify close
    }

    @Test
    void delete_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(InsuranceService.class, id)).thenReturn(null);

        // Act
        boolean result = insuranceServiceDAO.delete(id);

        // Assert
        assertFalse(result);
        verify(mockSession).get(InsuranceService.class, id);
        verify(mockSession, never()).delete(any());
        verify(mockTransaction, never()).commit(); // No commit if not found
         verify(mockSession).close(); // Verify close
    }

    @Test
    void delete_Exception() {
        // Arrange
        Long id = 1L;
        InsuranceService serviceToDelete = new InsuranceService();
        when(mockSession.get(InsuranceService.class, id)).thenReturn(serviceToDelete);
        doThrow(new RuntimeException("DB Delete Error")).when(mockSession).delete(any(InsuranceService.class));

        // Act
        boolean result = insuranceServiceDAO.delete(id);

        // Assert
        assertFalse(result);
        verify(mockSession).delete(serviceToDelete);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
         verify(mockSession).close(); // Verify close
    }

    @Test
    void findByExternalId_Success() {
        // Arrange
        String externalId = "EXT123";
        List<InsuranceService> expectedList = Collections.singletonList(new InsuranceService());
        when(mockQuery.setParameter(eq("externalId"), eq(externalId))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<InsuranceService> result = insuranceServiceDAO.findByExternalId(externalId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM InsuranceService WHERE externalId = :externalId", InsuranceService.class);
        verify(mockQuery).setParameter("externalId", externalId);
    }
    
     @Test
    void findByExternalId_Exception() {
        // Arrange
        String externalId = "EXT123";
        when(mockQuery.setParameter(eq("externalId"), eq(externalId))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<InsuranceService> result = insuranceServiceDAO.findByExternalId(externalId);

        // Assert
        assertNull(result);
    }
} 