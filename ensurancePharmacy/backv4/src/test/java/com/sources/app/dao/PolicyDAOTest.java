package com.sources.app.dao;

import com.sources.app.entities.Policy;
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
class PolicyDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<Policy> mockQuery;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private PolicyDAO policyDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockSession.createQuery(anyString(), eq(Policy.class))).thenReturn(mockQuery);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success() {
        // Arrange
        Float percentage = 0.8f;
        Date creationDate = new Date();
        Date expDate = new Date(System.currentTimeMillis() + 31536000000L); // 1 year later
        Float cost = 500.0f;
        Integer enabled = 1;

        // Act
        Policy result = policyDAO.create(percentage, creationDate, expDate, cost, enabled);

        // Assert
        assertNotNull(result);
        assertEquals(percentage, result.getPercentage());
        assertEquals(creationDate, result.getCreationDate());
        assertEquals(expDate, result.getExpDate());
        assertEquals(cost, result.getCost());
        assertEquals(enabled, result.getEnabled());
        verify(mockSession).save(any(Policy.class));
        verify(mockTransaction).commit();
    }

    @Test
    void create_Exception() {
        // Arrange
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(Policy.class));
        when(mockTransaction.isActive()).thenReturn(true); // For rollback check

        // Act
        Policy result = policyDAO.create(0.5f, new Date(), new Date(), 100f, 1);

        // Assert
        assertNull(result); // Returns null
        verify(mockSession).save(any(Policy.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void find_Found() {
        // Arrange
        Long id = 1L;
        Policy expectedPolicy = new Policy();
        when(mockSession.get(Policy.class, id)).thenReturn(expectedPolicy);

        // Act
        Policy result = policyDAO.find(id); // DAO method name is find, not findById

        // Assert
        assertNotNull(result);
        assertEquals(expectedPolicy, result);
        verify(mockSession).get(Policy.class, id);
    }

    @Test
    void find_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Policy.class, id)).thenReturn(null);

        // Act
        Policy result = policyDAO.find(id);

        // Assert
        assertNull(result);
    }
    
     @Test
    void find_Exception() {
        // Arrange
        Long id = 1L;
         when(mockSession.get(Policy.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        Policy result = policyDAO.find(id);

        // Assert
        assertNull(result);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<Policy> expectedList = Collections.singletonList(new Policy());
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Policy> result = policyDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM Policy", Policy.class);
    }
    
     @Test
    void findAll_Exception() {
        // Arrange
         when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<Policy> result = policyDAO.findAll();

        // Assert
        assertNull(result);
    }

    @Test
    void update_Success() {
        // Arrange
        Policy policyToUpdate = new Policy();

        // Act
        Policy result = policyDAO.update(policyToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(policyToUpdate, result);
        verify(mockSession).update(policyToUpdate);
        verify(mockTransaction).commit();
    }

    @Test
    void update_Exception() {
        // Arrange
        Policy policyToUpdate = new Policy();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(Policy.class));

        // Act
        Policy result = policyDAO.update(policyToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(policyToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void delete_Success() {
        // Arrange
        Long id = 1L;
        Policy policyToDelete = new Policy();
        when(mockSession.get(Policy.class, id)).thenReturn(policyToDelete);

        // Act
        boolean result = policyDAO.delete(id);

        // Assert
        assertTrue(result);
        verify(mockSession).get(Policy.class, id);
        verify(mockSession).delete(policyToDelete);
        verify(mockTransaction).commit();
    }

    @Test
    void delete_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Policy.class, id)).thenReturn(null);

        // Act
        boolean result = policyDAO.delete(id);

        // Assert
        assertFalse(result);
        verify(mockSession).get(Policy.class, id);
        verify(mockSession, never()).delete(any());
        verify(mockTransaction, never()).commit(); // No commit if not found
    }

    @Test
    void delete_Exception() {
        // Arrange
        Long id = 1L;
        Policy policyToDelete = new Policy();
        when(mockSession.get(Policy.class, id)).thenReturn(policyToDelete);
        doThrow(new RuntimeException("DB Delete Error")).when(mockSession).delete(any(Policy.class));

        // Act
        boolean result = policyDAO.delete(id);

        // Assert
        assertFalse(result);
        verify(mockSession).delete(policyToDelete);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
} 