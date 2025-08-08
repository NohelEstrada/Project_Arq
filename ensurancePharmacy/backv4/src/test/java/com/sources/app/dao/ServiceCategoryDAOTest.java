package com.sources.app.dao;

import com.sources.app.entities.Category;
import com.sources.app.entities.Service;
import com.sources.app.entities.ServiceCategory;
import com.sources.app.entities.ServiceCategoryId;
import com.sources.app.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceCategoryDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<ServiceCategory> mockQuery;
    @Mock
    private Service mockService;
    @Mock
    private Category mockCategory;

    @Captor
    ArgumentCaptor<ServiceCategory> serviceCategoryCaptor;
    @Captor
    ArgumentCaptor<ServiceCategoryId> serviceCategoryIdCaptor;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private ServiceCategoryDAO serviceCategoryDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockSession.createQuery(anyString(), eq(ServiceCategory.class))).thenReturn(mockQuery);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success() {
        // Arrange
        Long serviceId = 1L;
        Long categoryId = 2L;

        when(mockSession.get(Service.class, serviceId)).thenReturn(mockService);
        when(mockSession.get(Category.class, categoryId)).thenReturn(mockCategory);

        // Act
        ServiceCategory result = serviceCategoryDAO.create(serviceId, categoryId);

        // Assert
        assertNotNull(result);
        // Use captor to verify the object saved
        verify(mockSession).save(serviceCategoryCaptor.capture());
        ServiceCategory savedEntity = serviceCategoryCaptor.getValue();
        assertEquals(mockService, savedEntity.getService());
        assertEquals(mockCategory, savedEntity.getCategory());
        verify(mockTransaction).commit();
    }

    @Test
    void create_ServiceNotFound() {
        // Arrange
        Long serviceId = 1L;
        Long categoryId = 2L;
        when(mockSession.get(Service.class, serviceId)).thenReturn(null);
        when(mockSession.get(Category.class, categoryId)).thenReturn(mockCategory);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            serviceCategoryDAO.create(serviceId, categoryId);
        });
        assertEquals("Service o Category no encontrado.", exception.getMessage());
        verify(mockSession).get(Service.class, serviceId);
        verify(mockSession, never()).get(eq(Category.class), eq(categoryId));
        verify(mockSession, never()).save(any());
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
    
    @Test
    void create_CategoryNotFound() {
        // Arrange
        Long serviceId = 1L;
        Long categoryId = 2L;
        when(mockSession.get(Service.class, serviceId)).thenReturn(mockService);
        when(mockSession.get(Category.class, categoryId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            serviceCategoryDAO.create(serviceId, categoryId);
        });
        assertEquals("Service o Category no encontrado.", exception.getMessage());
        verify(mockSession).get(Service.class, serviceId);
        verify(mockSession).get(Category.class, categoryId);
        verify(mockSession, never()).save(any());
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void create_ExceptionDuringSave() {
        // Arrange
        Long serviceId = 1L;
        Long categoryId = 2L;
        when(mockSession.get(Service.class, serviceId)).thenReturn(mockService);
        when(mockSession.get(Category.class, categoryId)).thenReturn(mockCategory);
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(ServiceCategory.class));

        // Act
        ServiceCategory result = serviceCategoryDAO.create(serviceId, categoryId);

        // Assert
        assertNull(result); // Returns null
        verify(mockSession).save(any(ServiceCategory.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void findById_Found() {
        // Arrange
        Long serviceId = 1L;
        Long categoryId = 2L;
        ServiceCategory expectedEntity = new ServiceCategory();
        // Use captor for composite ID
        when(mockSession.get(eq(ServiceCategory.class), serviceCategoryIdCaptor.capture())).thenReturn(expectedEntity);

        // Act
        ServiceCategory result = serviceCategoryDAO.findById(serviceId, categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedEntity, result);
        ServiceCategoryId capturedId = serviceCategoryIdCaptor.getValue();
        assertNotNull(capturedId); // Verify the ID object was captured
        verify(mockSession).get(eq(ServiceCategory.class), any(ServiceCategoryId.class));
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long serviceId = 1L;
        Long categoryId = 2L;
        when(mockSession.get(eq(ServiceCategory.class), any(ServiceCategoryId.class))).thenReturn(null);

        // Act
        ServiceCategory result = serviceCategoryDAO.findById(serviceId, categoryId);

        // Assert
        assertNull(result);
    }
    
     @Test
    void findById_Exception() {
        // Arrange
        Long serviceId = 1L;
        Long categoryId = 2L;
        when(mockSession.get(eq(ServiceCategory.class), any(ServiceCategoryId.class))).thenThrow(new RuntimeException("DB Error"));

        // Act
        ServiceCategory result = serviceCategoryDAO.findById(serviceId, categoryId);

        // Assert
        assertNull(result);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<ServiceCategory> expectedList = Collections.singletonList(new ServiceCategory());
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<ServiceCategory> result = serviceCategoryDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM ServiceCategory", ServiceCategory.class);
    }
    
     @Test
    void findAll_Exception() {
        // Arrange
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<ServiceCategory> result = serviceCategoryDAO.findAll();

        // Assert
        assertNull(result);
    }

    @Test
    void update_Success() {
        // Arrange
        ServiceCategory scToUpdate = new ServiceCategory();

        // Act
        ServiceCategory result = serviceCategoryDAO.update(scToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(scToUpdate, result);
        verify(mockSession).update(scToUpdate);
        verify(mockTransaction).commit();
    }

    @Test
    void update_Exception() {
        // Arrange
        ServiceCategory scToUpdate = new ServiceCategory();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(ServiceCategory.class));

        // Act
        ServiceCategory result = serviceCategoryDAO.update(scToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(scToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
} 