package com.sources.app.dao;

import com.sources.app.entities.Hospital;
import com.sources.app.entities.Transactions;
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

import java.util.Date;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionsDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<Transactions> mockQuery;
    @Mock
    private User mockUser;
    @Mock
    private Hospital mockHospital;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private TransactionsDAO transactionsDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().doReturn(mockSession).when(mockSessionFactory).openSession();
        lenient().doReturn(mockTransaction).when(mockSession).beginTransaction();
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success() {
        // Arrange
        Long userId = 1L;
        Long hospitalId = 1L;
        Date transDate = new Date();
        Double total = 100.0;
        Double copay = 10.0;
        String comment = "Test comment";
        String resultStatus = "Approved";
        Integer covered = 1;
        String auth = "Auth123";

        when(mockSession.get(User.class, userId)).thenReturn(mockUser);
        when(mockSession.get(Hospital.class, hospitalId)).thenReturn(mockHospital);
        // If User or Hospital have IDs, mock them: when(mockUser.getIdUser()).thenReturn(userId);

        // Act
        Transactions result = transactionsDAO.create(userId, hospitalId, transDate, total, copay, comment, resultStatus, covered, auth);

        // Assert
        assertNotNull(result);
        assertEquals(mockUser, result.getUser());
        assertEquals(mockHospital, result.getHospital());
        assertEquals(transDate, result.getTransDate());
        // ... add assertions for other fields
        verify(mockSession).get(User.class, userId);
        verify(mockSession).get(Hospital.class, hospitalId);
        verify(mockSession).save(any(Transactions.class));
        verify(mockTransaction).commit();
    }

    @Test
    void create_UserNotFound() {
        // Arrange
        Long userId = 1L;
        when(mockSession.get(User.class, userId)).thenReturn(null);
        // No need to mock hospital if user check fails first

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
             transactionsDAO.create(userId, 2L, new Date(), 100.0, 10.0, "c", "r", 1, "a");
        });
        assertTrue(exception.getMessage().contains("User or Hospital not found"));
        verify(mockSession).get(User.class, userId);
        verify(mockSession, never()).get(eq(Hospital.class), anyLong());
        verify(mockSession, never()).save(any(Transactions.class));
        verify(mockTransaction, never()).commit(); // Should not commit if exception before save
        // Depending on implementation, rollback might happen or not before rethrowing
        verify(mockTransaction, atLeastOnce()).rollback(); 
    }
    
     @Test
    void create_HospitalNotFound() {
        // Arrange
        Long userId = 1L;
        Long hospitalId = 1L;
        when(mockSession.get(User.class, userId)).thenReturn(mockUser);
        when(mockSession.get(Hospital.class, hospitalId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
             transactionsDAO.create(userId, hospitalId, new Date(), 100.0, 10.0, "c", "r", 1, "a");
        });
        assertTrue(exception.getMessage().contains("User or Hospital not found"));
        verify(mockSession).get(User.class, userId);
        verify(mockSession).get(Hospital.class, hospitalId);
        verify(mockSession, never()).save(any(Transactions.class));
        verify(mockTransaction, never()).commit();
        verify(mockTransaction, atLeastOnce()).rollback();
    }

    @Test
    void create_ExceptionDuringSave() {
        // Arrange
        Long userId = 1L;
        Long hospitalId = 1L;
        when(mockSession.get(User.class, userId)).thenReturn(mockUser);
        when(mockSession.get(Hospital.class, hospitalId)).thenReturn(mockHospital);
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(Transactions.class));

        // Act
        Transactions result = transactionsDAO.create(userId, hospitalId, new Date(), 100.0, 10.0, "c", "r", 1, "a");

        // Assert
        assertNull(result); // Method returns null on exception
        verify(mockSession).save(any(Transactions.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }


    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        Transactions expectedTransaction = new Transactions();
        when(mockSession.get(Transactions.class, id)).thenReturn(expectedTransaction);

        // Act
        Transactions result = transactionsDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedTransaction, result);
        verify(mockSession).get(Transactions.class, id);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Transactions.class, id)).thenReturn(null);

        // Act
        Transactions result = transactionsDAO.findById(id);

        // Assert
        assertNull(result);
        verify(mockSession).get(Transactions.class, id);
    }
    
    @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Transactions.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        Transactions result = transactionsDAO.findById(id);

        // Assert
        assertNull(result);
        verify(mockSession).get(Transactions.class, id);
    }

    @Test
    void findByUserId_Success() {
        // Arrange
        Long userId = 1L;
        List<Transactions> expectedList = Collections.singletonList(new Transactions());
        when(mockSession.createQuery(anyString(), eq(Transactions.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("idUser"), eq(userId))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Transactions> result = transactionsDAO.findByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM Transactions t WHERE t.user.idUser = :idUser", Transactions.class);
        verify(mockQuery).setParameter("idUser", userId);
    }
    
    @Test
    void findByUserId_Exception() {
        // Arrange
        Long userId = 1L;
        when(mockSession.createQuery(anyString(), eq(Transactions.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("idUser"), eq(userId))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<Transactions> result = transactionsDAO.findByUserId(userId);

        // Assert
        assertNull(result);
        verify(mockSession).createQuery("FROM Transactions t WHERE t.user.idUser = :idUser", Transactions.class);
        verify(mockQuery).setParameter("idUser", userId);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<Transactions> expectedList = Collections.singletonList(new Transactions());
        when(mockSession.createQuery(anyString(), eq(Transactions.class))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Transactions> result = transactionsDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM Transactions", Transactions.class);
    }
    
    @Test
    void findAll_Exception() {
        // Arrange
        when(mockSession.createQuery(anyString(), eq(Transactions.class))).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<Transactions> result = transactionsDAO.findAll();

        // Assert
        assertNull(result);
        verify(mockSession).createQuery("FROM Transactions", Transactions.class);
    }

    @Test
    void update_Success() {
        // Arrange
        Transactions transactionToUpdate = new Transactions();
        // Set necessary properties for transactionToUpdate

        // Act
        Transactions result = transactionsDAO.update(transactionToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(transactionToUpdate, result);
        verify(mockSession).update(transactionToUpdate);
        verify(mockTransaction).commit();
    }

    @Test
    void update_Exception() {
        // Arrange
        Transactions transactionToUpdate = new Transactions();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(Transactions.class));

        // Act
        Transactions result = transactionsDAO.update(transactionToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(transactionToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
} 