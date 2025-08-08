package com.sources.app.dao;

import com.sources.app.entities.Pharmacy;
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
class PharmacyDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<Pharmacy> mockQuery;
    @Mock
    private TransactionStatus mockTransactionStatus;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private PharmacyDAO pharmacyDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockTransaction.getStatus()).thenReturn(mockTransactionStatus);
        lenient().when(mockTransactionStatus.canRollback()).thenReturn(true);
        lenient().when(mockSession.createQuery(anyString(), eq(Pharmacy.class))).thenReturn(mockQuery);
        lenient().when(mockSession.isOpen()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success() {
        // Arrange
        String name = "Central Pharmacy";
        String address = "10 Pharmacy Ln";
        Long phone = 9876543210L;
        String email = "info@centralpharmacy.com";
        Integer enabled = 1;

        // Act
        Pharmacy result = pharmacyDAO.create(name, address, phone, email, enabled);

        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(address, result.getAddress());
        assertEquals(phone, result.getPhone());
        assertEquals(email, result.getEmail());
        assertEquals(enabled, result.getEnabled());
        verify(mockSession).save(any(Pharmacy.class));
        verify(mockTransaction).commit();
        verify(mockSession).close();
    }

    @Test
    void create_Exception() {
        // Arrange
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(Pharmacy.class));

        // Act
        Pharmacy result = pharmacyDAO.create("Test", "Addr", 1L, "email", 1);

        // Assert
        assertNotNull(result);
        verify(mockSession).save(any(Pharmacy.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
        verify(mockSession).close();
    }

    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        Pharmacy expectedPharmacy = new Pharmacy();
        when(mockSession.get(Pharmacy.class, id)).thenReturn(expectedPharmacy);

        // Act
        Pharmacy result = pharmacyDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPharmacy, result);
        verify(mockSession).get(Pharmacy.class, id);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Pharmacy.class, id)).thenReturn(null);

        // Act
        Pharmacy result = pharmacyDAO.findById(id);

        // Assert
        assertNull(result);
    }
    
    @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Pharmacy.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        Pharmacy result = pharmacyDAO.findById(id);

        // Assert
        assertNull(result);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<Pharmacy> expectedList = Collections.singletonList(new Pharmacy());
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Pharmacy> result = pharmacyDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM Pharmacy", Pharmacy.class);
    }
    
    @Test
    void findAll_Exception() {
        // Arrange
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<Pharmacy> result = pharmacyDAO.findAll();

        // Assert
        assertNull(result);
    }

    @Test
    void update_Success() {
        // Arrange
        Pharmacy pharmacyToUpdate = new Pharmacy();
        pharmacyToUpdate.setIdPharmacy(1L);
        pharmacyToUpdate.setName("Updated Pharmacy Name");

        // Act
        Pharmacy result = pharmacyDAO.update(pharmacyToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(pharmacyToUpdate, result);
        verify(mockSession).update(pharmacyToUpdate);
        verify(mockTransaction).commit();
        verify(mockSession).close();
    }

    @Test
    void update_Exception() {
        // Arrange
        Pharmacy pharmacyToUpdate = new Pharmacy();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(Pharmacy.class));

        // Act
        Pharmacy result = pharmacyDAO.update(pharmacyToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(pharmacyToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
        verify(mockSession).close();
    }
} 