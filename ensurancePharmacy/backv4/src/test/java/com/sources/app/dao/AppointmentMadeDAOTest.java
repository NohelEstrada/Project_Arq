package com.sources.app.dao;

import com.sources.app.entities.AppointmentMade;
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
class AppointmentMadeDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<AppointmentMade> mockQuery;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private AppointmentMadeDAO appointmentMadeDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockSession.createQuery(anyString(), eq(AppointmentMade.class))).thenReturn(mockQuery);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success() {
        // Arrange
        Long citaId = 1L;
        Long userId = 2L;
        Date madeDate = new Date();

        // Act
        AppointmentMade result = appointmentMadeDAO.create(citaId, userId, madeDate);

        // Assert
        assertNotNull(result);
        assertEquals(citaId, result.getIdCita());
        assertEquals(userId, result.getIdUser());
        assertEquals(madeDate, result.getAppointmentMadeDate());
        verify(mockSession).save(any(AppointmentMade.class));
        verify(mockTransaction).commit();
    }

    @Test
    void create_Exception() {
        // Arrange
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(AppointmentMade.class));

        // Act
        AppointmentMade result = appointmentMadeDAO.create(1L, 2L, new Date());

        // Assert
        assertNull(result); // Returns null
        verify(mockSession).save(any(AppointmentMade.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        AppointmentMade expectedAppMade = new AppointmentMade();
        when(mockSession.get(AppointmentMade.class, id)).thenReturn(expectedAppMade);

        // Act
        AppointmentMade result = appointmentMadeDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAppMade, result);
        verify(mockSession).get(AppointmentMade.class, id);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(AppointmentMade.class, id)).thenReturn(null);

        // Act
        AppointmentMade result = appointmentMadeDAO.findById(id);

        // Assert
        assertNull(result);
    }
    
     @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(AppointmentMade.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        AppointmentMade result = appointmentMadeDAO.findById(id);

        // Assert
        assertNull(result);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<AppointmentMade> expectedList = Collections.singletonList(new AppointmentMade());
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<AppointmentMade> result = appointmentMadeDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM AppointmentMade", AppointmentMade.class);
    }
    
     @Test
    void findAll_Exception() {
        // Arrange
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<AppointmentMade> result = appointmentMadeDAO.findAll();

        // Assert
        assertNull(result);
    }

    @Test
    void update_Success() {
        // Arrange
        AppointmentMade appMadeToUpdate = new AppointmentMade();

        // Act
        AppointmentMade result = appointmentMadeDAO.update(appMadeToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(appMadeToUpdate, result);
        verify(mockSession).update(appMadeToUpdate);
        verify(mockTransaction).commit();
    }

    @Test
    void update_Exception() {
        // Arrange
        AppointmentMade appMadeToUpdate = new AppointmentMade();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(AppointmentMade.class));

        // Act
        AppointmentMade result = appointmentMadeDAO.update(appMadeToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(appMadeToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
} 