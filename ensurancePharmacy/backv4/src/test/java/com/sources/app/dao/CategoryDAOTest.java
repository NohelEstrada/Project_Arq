package com.sources.app.dao;

import com.sources.app.entities.Category;
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
class CategoryDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<Category> mockQuery;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private CategoryDAO categoryDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockSession.createQuery(anyString(), eq(Category.class))).thenReturn(mockQuery);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success() {
        // Arrange
        String name = "Test Category";
        Integer enabled = 1;

        // Act
        Category result = categoryDAO.create(name, enabled);

        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(enabled, result.getEnabled());
        verify(mockSession).save(any(Category.class));
        verify(mockTransaction).commit();
    }

    @Test
    void create_Exception() {
        // Arrange
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(Category.class));

        // Act
        Category result = categoryDAO.create("Error Cat", 1);

        // Assert
        assertNull(result); // Returns null
        verify(mockSession).save(any(Category.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        Category expectedCategory = new Category();
        when(mockSession.get(Category.class, id)).thenReturn(expectedCategory);

        // Act
        Category result = categoryDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedCategory, result);
        verify(mockSession).get(Category.class, id);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Category.class, id)).thenReturn(null);

        // Act
        Category result = categoryDAO.findById(id);

        // Assert
        assertNull(result);
    }
    
    @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Category.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        Category result = categoryDAO.findById(id);

        // Assert
        assertNull(result);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<Category> expectedList = Collections.singletonList(new Category());
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Category> result = categoryDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM Category", Category.class);
    }
    
    @Test
    void findAll_Exception() {
        // Arrange
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<Category> result = categoryDAO.findAll();

        // Assert
        assertNull(result);
    }

    @Test
    void update_Success() {
        // Arrange
        Category categoryToUpdate = new Category();

        // Act
        Category result = categoryDAO.update(categoryToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(categoryToUpdate, result);
        verify(mockSession).update(categoryToUpdate);
        verify(mockTransaction).commit();
    }

    @Test
    void update_Exception() {
        // Arrange
        Category categoryToUpdate = new Category();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(Category.class));

        // Act
        Category result = categoryDAO.update(categoryToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(categoryToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
} 