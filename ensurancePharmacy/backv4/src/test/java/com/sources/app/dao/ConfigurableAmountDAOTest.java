package com.sources.app.dao;

import com.sources.app.entities.ConfigurableAmount;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurableAmountDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<ConfigurableAmount> mockQuery;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private ConfigurableAmountDAO configurableAmountDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockSession.createQuery(anyString(), eq(ConfigurableAmount.class))).thenReturn(mockQuery);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success() {
        // Arrange
        BigDecimal amount = new BigDecimal("300.00");

        // Act
        ConfigurableAmount result = configurableAmountDAO.create(amount);

        // Assert
        assertNotNull(result);
        assertEquals(amount, result.getPrescriptionAmount());
        verify(mockSession).save(any(ConfigurableAmount.class));
        verify(mockTransaction).commit();
    }

    @Test
    void create_Exception() {
        // Arrange
        BigDecimal amount = new BigDecimal("300.00");
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(ConfigurableAmount.class));

        // Act
        ConfigurableAmount result = configurableAmountDAO.create(amount);

        // Assert
        assertNull(result); // Returns null on exception
        verify(mockSession).save(any(ConfigurableAmount.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        ConfigurableAmount expectedAmount = new ConfigurableAmount();
        expectedAmount.setIdConfigurableAmount(id);
        expectedAmount.setPrescriptionAmount(new BigDecimal("250.00"));
        when(mockSession.get(ConfigurableAmount.class, id)).thenReturn(expectedAmount);

        // Act
        ConfigurableAmount result = configurableAmountDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAmount, result);
        verify(mockSession).get(ConfigurableAmount.class, id);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(ConfigurableAmount.class, id)).thenReturn(null);

        // Act
        ConfigurableAmount result = configurableAmountDAO.findById(id);

        // Assert
        assertNull(result);
        verify(mockSession).get(ConfigurableAmount.class, id);
    }

    @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(ConfigurableAmount.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        ConfigurableAmount result = configurableAmountDAO.findById(id);

        // Assert
        assertNull(result);
        verify(mockSession).get(ConfigurableAmount.class, id);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<ConfigurableAmount> expectedList = Collections.singletonList(new ConfigurableAmount());
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<ConfigurableAmount> result = configurableAmountDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM ConfigurableAmount", ConfigurableAmount.class);
    }

    @Test
    void findAll_Exception() {
        // Arrange
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<ConfigurableAmount> result = configurableAmountDAO.findAll();

        // Assert
        assertNull(result);
        verify(mockSession).createQuery("FROM ConfigurableAmount", ConfigurableAmount.class);
    }

    @Test
    void update_Success() {
        // Arrange
        ConfigurableAmount amountToUpdate = new ConfigurableAmount();
        amountToUpdate.setIdConfigurableAmount(1L);
        amountToUpdate.setPrescriptionAmount(new BigDecimal("400.00"));

        // Act
        ConfigurableAmount result = configurableAmountDAO.update(amountToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(amountToUpdate, result);
        verify(mockSession).update(amountToUpdate);
        verify(mockTransaction).commit();
    }

    @Test
    void update_Exception() {
        // Arrange
        ConfigurableAmount amountToUpdate = new ConfigurableAmount();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(ConfigurableAmount.class));

        // Act
        ConfigurableAmount result = configurableAmountDAO.update(amountToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(amountToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void findCurrentConfig_Found() {
        // Arrange
        ConfigurableAmount expectedConfig = new ConfigurableAmount();
        expectedConfig.setPrescriptionAmount(new BigDecimal("250.00"));
        when(mockQuery.setMaxResults(1)).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenReturn(expectedConfig);

        // Act
        ConfigurableAmount result = configurableAmountDAO.findCurrentConfig();

        // Assert
        assertNotNull(result);
        assertEquals(expectedConfig, result);
        verify(mockSession).createQuery("FROM ConfigurableAmount", ConfigurableAmount.class);
        verify(mockQuery).setMaxResults(1);
        verify(mockQuery).uniqueResult();
        verify(mockSession, never()).save(any(ConfigurableAmount.class)); // Should not create default
    }

    @Test
    void findCurrentConfig_NotFound_CreatesDefault() {
        // Arrange
        when(mockQuery.setMaxResults(1)).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenReturn(null); // Simulate config not found

        ConfigurableAmount defaultConfig = new ConfigurableAmount(); // The object created by the DAO
        defaultConfig.setPrescriptionAmount(new BigDecimal("250.00"));
        
        // Need to mock the create call *within* findCurrentConfig
        // Re-mock session/transaction for the create call
        Session createSession = mock(Session.class);
        Transaction createTransaction = mock(Transaction.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        // Make openSession return the specific mock session when create is called internally
        when(mockSessionFactory.openSession()).thenReturn(mockSession) // For the initial find query
                                           .thenReturn(createSession); // For the subsequent create call
        when(createSession.beginTransaction()).thenReturn(createTransaction);
       
        // Act
        ConfigurableAmount result = configurableAmountDAO.findCurrentConfig();

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("250.00"), result.getPrescriptionAmount());
        // Verify the find part
        verify(mockSession).createQuery("FROM ConfigurableAmount", ConfigurableAmount.class);
        verify(mockQuery).setMaxResults(1);
        verify(mockQuery).uniqueResult();
        // Verify the create part
        verify(createSession).save(any(ConfigurableAmount.class));
        verify(createTransaction).commit();
    }
    
    @Test
    void findCurrentConfig_ExceptionFinding_CreatesDefault() {
        // Arrange
        when(mockQuery.setMaxResults(1)).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenThrow(new RuntimeException("DB Find Error")); // Simulate exception during find

        ConfigurableAmount defaultConfig = new ConfigurableAmount();
        defaultConfig.setPrescriptionAmount(new BigDecimal("250.00"));

        // Mock the create call
        Session createSession = mock(Session.class);
        Transaction createTransaction = mock(Transaction.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        when(mockSessionFactory.openSession()).thenReturn(mockSession) // For the initial find query
                                           .thenReturn(createSession); // For the subsequent create call
        when(createSession.beginTransaction()).thenReturn(createTransaction);

        // Act
        ConfigurableAmount result = configurableAmountDAO.findCurrentConfig();

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("250.00"), result.getPrescriptionAmount());
        // Verify the find part (that it was attempted)
        verify(mockSession).createQuery("FROM ConfigurableAmount", ConfigurableAmount.class);
        verify(mockQuery).setMaxResults(1);
        verify(mockQuery).uniqueResult();
        // Verify the create part was executed due to the exception
        verify(createSession).save(any(ConfigurableAmount.class));
        verify(createTransaction).commit();
    }
     @Test
    void findCurrentConfig_ExceptionFinding_ExceptionCreatingDefault() {
        // Arrange
        when(mockQuery.setMaxResults(1)).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenThrow(new RuntimeException("DB Find Error")); // Simulate exception during find

        // Mock the create call to also throw an exception
        Session createSession = mock(Session.class);
        Transaction createTransaction = mock(Transaction.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        when(mockSessionFactory.openSession()).thenReturn(mockSession) // For the initial find query
                                           .thenReturn(createSession); // For the subsequent create call
        when(createSession.beginTransaction()).thenReturn(createTransaction);
        doThrow(new RuntimeException("DB Create Error")).when(createSession).save(any(ConfigurableAmount.class));

        // Act
        ConfigurableAmount result = configurableAmountDAO.findCurrentConfig();

        // Assert
        assertNull(result); // Should return null if create also fails
        // Verify the find part (that it was attempted)
        verify(mockSession).createQuery("FROM ConfigurableAmount", ConfigurableAmount.class);
        verify(mockQuery).setMaxResults(1);
        verify(mockQuery).uniqueResult();
        // Verify the create part was attempted and failed
        verify(createSession).save(any(ConfigurableAmount.class));
        verify(createTransaction).rollback(); // Rollback should occur in create
    }
} 