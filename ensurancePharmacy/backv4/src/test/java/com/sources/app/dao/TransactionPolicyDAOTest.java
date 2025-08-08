package com.sources.app.dao;

import com.sources.app.entities.Policy;
import com.sources.app.entities.TransactionPolicy;
import com.sources.app.entities.User;
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
class TransactionPolicyDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<TransactionPolicy> mockQuery;
    @Mock
    private Policy mockPolicy;
    @Mock
    private User mockUser;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private TransactionPolicyDAO transactionPolicyDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().doReturn(mockSession).when(mockSessionFactory).openSession();
        lenient().doReturn(mockTransaction).when(mockSession).beginTransaction();
        lenient().doReturn(mockQuery).when(mockSession).createQuery(anyString(), eq(TransactionPolicy.class));
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success() {
        // Arrange
        Long policyId = 1L;
        Long userId = 2L;
        Date payDate = new Date();
        BigDecimal total = new BigDecimal("199.99");

        when(mockSession.get(Policy.class, policyId)).thenReturn(mockPolicy);
        when(mockSession.get(User.class, userId)).thenReturn(mockUser);

        // Act
        TransactionPolicy result = transactionPolicyDAO.create(policyId, userId, payDate, total);

        // Assert
        assertNotNull(result);
        assertEquals(mockPolicy, result.getPolicy());
        assertEquals(mockUser, result.getUser());
        assertEquals(payDate, result.getPayDate());
        assertEquals(total, result.getTotal());
        verify(mockSession).save(any(TransactionPolicy.class));
        verify(mockTransaction).commit();
    }

    @Test
    void create_PolicyNotFound() {
        // Arrange
        Long policyId = 1L;
        Long userId = 2L;
        when(mockSession.get(Policy.class, policyId)).thenReturn(null);
        when(mockSession.get(User.class, userId)).thenReturn(mockUser);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionPolicyDAO.create(policyId, userId, new Date(), BigDecimal.ZERO);
        });
        assertEquals("Policy o User no encontrados.", exception.getMessage());
        verify(mockSession).get(Policy.class, policyId);
        verify(mockSession, never()).get(eq(User.class), eq(userId));
        verify(mockSession, never()).save(any());
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
    
     @Test
    void create_UserNotFound() {
        // Arrange
        Long policyId = 1L;
        Long userId = 2L;
        when(mockSession.get(Policy.class, policyId)).thenReturn(mockPolicy);
        when(mockSession.get(User.class, userId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionPolicyDAO.create(policyId, userId, new Date(), BigDecimal.ZERO);
        });
        assertEquals("Policy o User no encontrados.", exception.getMessage());
        verify(mockSession).get(Policy.class, policyId);
        verify(mockSession).get(User.class, userId);
        verify(mockSession, never()).save(any());
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void create_ExceptionDuringSave() {
        // Arrange
        Long policyId = 1L;
        Long userId = 2L;
        when(mockSession.get(Policy.class, policyId)).thenReturn(mockPolicy);
        when(mockSession.get(User.class, userId)).thenReturn(mockUser);
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(TransactionPolicy.class));

        // Act
        TransactionPolicy result = transactionPolicyDAO.create(policyId, userId, new Date(), BigDecimal.ZERO);

        // Assert
        assertNull(result); // Returns null
        verify(mockSession).save(any(TransactionPolicy.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        TransactionPolicy expectedTransaction = new TransactionPolicy();
        when(mockSession.get(TransactionPolicy.class, id)).thenReturn(expectedTransaction);

        // Act
        TransactionPolicy result = transactionPolicyDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedTransaction, result);
        verify(mockSession).get(TransactionPolicy.class, id);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(TransactionPolicy.class, id)).thenReturn(null);

        // Act
        TransactionPolicy result = transactionPolicyDAO.findById(id);

        // Assert
        assertNull(result);
    }
    
     @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(TransactionPolicy.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        TransactionPolicy result = transactionPolicyDAO.findById(id);

        // Assert
        assertNull(result);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<TransactionPolicy> expectedList = Collections.singletonList(new TransactionPolicy());
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<TransactionPolicy> result = transactionPolicyDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM TransactionPolicy", TransactionPolicy.class);
    }
    
     @Test
    void findAll_Exception() {
        // Arrange
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<TransactionPolicy> result = transactionPolicyDAO.findAll();

        // Assert
        assertNull(result);
    }

    @Test
    void update_Success() {
        // Arrange
        TransactionPolicy tpToUpdate = new TransactionPolicy();

        // Act
        TransactionPolicy result = transactionPolicyDAO.update(tpToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(tpToUpdate, result);
        verify(mockSession).update(tpToUpdate);
        verify(mockTransaction).commit();
    }

    @Test
    void update_Exception() {
        // Arrange
        TransactionPolicy tpToUpdate = new TransactionPolicy();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(TransactionPolicy.class));

        // Act
        TransactionPolicy result = transactionPolicyDAO.update(tpToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(tpToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
} 