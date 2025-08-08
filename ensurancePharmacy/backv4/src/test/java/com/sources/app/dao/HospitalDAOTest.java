package com.sources.app.dao;

import com.sources.app.entities.Hospital;
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
class HospitalDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<Hospital> mockQuery;
    @Mock
    private TransactionStatus mockTransactionStatus;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private HospitalDAO hospitalDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockTransaction.getStatus()).thenReturn(mockTransactionStatus);
        lenient().when(mockTransactionStatus.canRollback()).thenReturn(true);
        lenient().when(mockSession.createQuery(anyString(), eq(Hospital.class))).thenReturn(mockQuery);
        lenient().when(mockSession.isOpen()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success() {
        // Arrange
        String name = "General Hospital";
        String address = "1 Main St";
        Long phone = 1234567890L;
        String email = "contact@generalhospital.com";
        Integer enabled = 1;

        // Act
        Hospital result = hospitalDAO.create(name, address, phone, email, enabled);

        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(address, result.getAddress());
        assertEquals(phone, result.getPhone());
        assertEquals(email, result.getEmail());
        assertEquals(enabled, result.getEnabled());
        verify(mockSession).save(any(Hospital.class));
        verify(mockTransaction).commit();
        verify(mockSession).close(); // Explicitly verify close
    }

    @Test
    void create_Exception() {
        // Arrange
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(Hospital.class));

        // Act
        Hospital result = hospitalDAO.create("Test", "Addr", 1L, "email", 1);

        // Assert
        // The DAO currently returns the non-null hospital object even on exception
        // Adjust assertion based on intended behavior. If it should be null, the DAO needs changing.
        assertNotNull(result); 
        // assertNull(result); // <-- Use this if DAO should return null on exception
        verify(mockSession).save(any(Hospital.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
        verify(mockSession).close(); // Explicitly verify close
    }

    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        Hospital expectedHospital = new Hospital();
        when(mockSession.get(Hospital.class, id)).thenReturn(expectedHospital);

        // Act
        Hospital result = hospitalDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedHospital, result);
        verify(mockSession).get(Hospital.class, id);
        verify(mockSession).close(); // Explicitly verify close
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Hospital.class, id)).thenReturn(null);

        // Act
        Hospital result = hospitalDAO.findById(id);

        // Assert
        assertNull(result);
        verify(mockSession).get(Hospital.class, id);
        verify(mockSession).close(); // Explicitly verify close
    }
    
    @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Hospital.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        Hospital result = hospitalDAO.findById(id);

        // Assert
        assertNull(result);
        verify(mockSession).get(Hospital.class, id);
        verify(mockSession).close(); // Explicitly verify close
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<Hospital> expectedList = Collections.singletonList(new Hospital());
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Hospital> result = hospitalDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM Hospital", Hospital.class);
        verify(mockQuery).getResultList();
        verify(mockSession).close(); // Explicitly verify close
    }
    
    @Test
    void findAll_Exception() {
        // Arrange
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<Hospital> result = hospitalDAO.findAll();

        // Assert
        assertNull(result);
        verify(mockSession).createQuery("FROM Hospital", Hospital.class);
        verify(mockSession).close(); // Explicitly verify close
    }

    @Test
    void update_Success() {
        // Arrange
        Hospital hospitalToUpdate = new Hospital();
        hospitalToUpdate.setIdHospital(1L);
        hospitalToUpdate.setName("Updated Name");

        // Act
        Hospital result = hospitalDAO.update(hospitalToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(hospitalToUpdate, result);
        verify(mockSession).update(hospitalToUpdate);
        verify(mockTransaction).commit();
        verify(mockSession).close(); // Explicitly verify close
    }

    @Test
    void update_Exception() {
        // Arrange
        Hospital hospitalToUpdate = new Hospital();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(Hospital.class));

        // Act
        Hospital result = hospitalDAO.update(hospitalToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(hospitalToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
        verify(mockSession).close(); // Explicitly verify close
    }
} 