package com.sources.app.dao;

import com.sources.app.entities.Category;
import com.sources.app.entities.Hospital;
import com.sources.app.entities.Service;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<Service> mockQuery;
    // Mocks for associated entities (though not strictly needed for all tests here)
    @Mock
    private Hospital mockHospital;
    @Mock
    private Category mockCategory;
    @Mock
    private Category mockSubcategory;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private ServiceDAO serviceDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        // Use lenient for query creation as different HQL strings are used
        lenient().when(mockSession.createQuery(anyString(), eq(Service.class))).thenReturn(mockQuery);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success() {
        // Arrange
        Service serviceToCreate = new Service();
        // Assume Hospital, Category, Subcategory are already set on serviceToCreate
        serviceToCreate.setHospital(mockHospital);
        serviceToCreate.setCategory(mockCategory);
        serviceToCreate.setSubcategory(mockSubcategory);

        // Act
        Service result = serviceDAO.create(serviceToCreate);

        // Assert
        assertNotNull(result);
        assertEquals(serviceToCreate, result);
        verify(mockSession).save(serviceToCreate);
        verify(mockTransaction).commit();
    }

    @Test
    void create_Exception() {
        // Arrange
        Service serviceToCreate = new Service();
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(Service.class));

        // Act
        Service result = serviceDAO.create(serviceToCreate);

        // Assert
        assertNull(result); // Returns null
        verify(mockSession).save(serviceToCreate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        Service expectedService = new Service();
        when(mockSession.get(Service.class, id)).thenReturn(expectedService);

        // Act
        Service result = serviceDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedService, result);
        verify(mockSession).get(Service.class, id);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Service.class, id)).thenReturn(null);

        // Act
        Service result = serviceDAO.findById(id);

        // Assert
        assertNull(result);
    }
    
     @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Service.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        Service result = serviceDAO.findById(id);

        // Assert
        assertNull(result);
    }

    @Test
    void findByIdWithDetails_Found() {
        // Arrange
        Long id = 1L;
        Service expectedService = new Service(); // Assume details are fetched
        String expectedHql = "select s from Service s " +
                             "join fetch s.hospital h " +
                             "join fetch s.category c " +
                             "join fetch s.subcategory sc " +
                             "where s.idService = :id";
                             
        when(mockSession.createQuery(eq(expectedHql), eq(Service.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("id"), eq(id))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenReturn(expectedService);

        // Act
        Service result = serviceDAO.findByIdWithDetails(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedService, result);
        verify(mockSession).createQuery(expectedHql, Service.class);
        verify(mockQuery).setParameter("id", id);
    }

    @Test
    void findByIdWithDetails_NotFound() {
        // Arrange
        Long id = 1L;
        String expectedHql = "select s from Service s " +
                             "join fetch s.hospital h " +
                             "join fetch s.category c " +
                             "join fetch s.subcategory sc " +
                             "where s.idService = :id";
        when(mockSession.createQuery(eq(expectedHql), eq(Service.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("id"), eq(id))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenReturn(null);

        // Act
        Service result = serviceDAO.findByIdWithDetails(id);

        // Assert
        assertNull(result);
    }
    
     @Test
    void findByIdWithDetails_Exception() {
        // Arrange
        Long id = 1L;
        String expectedHql = "select s from Service s " +
                             "join fetch s.hospital h " +
                             "join fetch s.category c " +
                             "join fetch s.subcategory sc " +
                             "where s.idService = :id";
        when(mockSession.createQuery(eq(expectedHql), eq(Service.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("id"), eq(id))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenThrow(new RuntimeException("DB Error"));

        // Act
        Service result = serviceDAO.findByIdWithDetails(id);

        // Assert
        assertNull(result);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<Service> expectedList = Collections.singletonList(new Service());
        when(mockSession.createQuery(eq("FROM Service"), eq(Service.class))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Service> result = serviceDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM Service", Service.class);
    }
    
    @Test
    void findAll_Exception() {
        // Arrange
         when(mockSession.createQuery(eq("FROM Service"), eq(Service.class))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<Service> result = serviceDAO.findAll();

        // Assert
        assertNull(result);
    }

    @Test
    void findAllWithDetails_Success() {
        // Arrange
        List<Service> expectedList = Collections.singletonList(new Service());
        String expectedHql = "select s from Service s " +
                             "join fetch s.hospital h " +
                             "join fetch s.category c " +
                             "join fetch s.subcategory sc";
        when(mockSession.createQuery(eq(expectedHql), eq(Service.class))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Service> result = serviceDAO.findAllWithDetails();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery(expectedHql, Service.class);
    }
    
     @Test
    void findAllWithDetails_Exception() {
        // Arrange
        String expectedHql = "select s from Service s " +
                             "join fetch s.hospital h " +
                             "join fetch s.category c " +
                             "join fetch s.subcategory sc";
         when(mockSession.createQuery(eq(expectedHql), eq(Service.class))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<Service> result = serviceDAO.findAllWithDetails();

        // Assert
        assertNull(result);
    }

    @Test
    void update_Success() {
        // Arrange
        Service serviceToUpdate = new Service();

        // Act
        Service result = serviceDAO.update(serviceToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(serviceToUpdate, result);
        verify(mockSession).update(serviceToUpdate);
        verify(mockTransaction).commit();
    }

    @Test
    void update_Exception() {
        // Arrange
        Service serviceToUpdate = new Service();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(Service.class));

        // Act
        Service result = serviceDAO.update(serviceToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(serviceToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
} 