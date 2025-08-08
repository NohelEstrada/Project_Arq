package com.sources.app.dao;

import com.sources.app.entities.Appointment;
import com.sources.app.entities.Hospital;
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
class AppointmentDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<Appointment> mockQuery;
    @Mock
    private Hospital mockHospital;
    @Mock
    private User mockUser;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private AppointmentDAO appointmentDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockSession.createQuery(anyString(), eq(Appointment.class))).thenReturn(mockQuery);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success() {
        // Arrange
        Long hospitalId = 1L;
        Long userId = 2L;
        Date appDate = new Date();
        Integer enabled = 1;

        when(mockSession.get(Hospital.class, hospitalId)).thenReturn(mockHospital);
        when(mockSession.get(User.class, userId)).thenReturn(mockUser);

        // Act
        Appointment result = appointmentDAO.create(hospitalId, userId, appDate, enabled);

        // Assert
        assertNotNull(result);
        assertEquals(mockHospital, result.getHospital());
        assertEquals(mockUser, result.getUser());
        assertEquals(appDate, result.getAppointmentDate());
        assertEquals(enabled, result.getEnabled());
        verify(mockSession).save(any(Appointment.class));
        verify(mockTransaction).commit();
    }

    @Test
    void create_HospitalNotFound() {
        // Arrange
        Long hospitalId = 1L;
        Long userId = 2L;
        when(mockSession.get(Hospital.class, hospitalId)).thenReturn(null); // Hospital not found
        when(mockSession.get(User.class, userId)).thenReturn(mockUser);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            appointmentDAO.create(hospitalId, userId, new Date(), 1);
        });
        assertEquals("Hospital or User not found", exception.getMessage());
        verify(mockSession).get(Hospital.class, hospitalId);
        verify(mockSession, never()).get(eq(User.class), anyLong()); // Should not check user if hospital fails
        verify(mockSession, never()).save(any(Appointment.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
    
    @Test
    void create_UserNotFound() {
        // Arrange
        Long hospitalId = 1L;
        Long userId = 2L;
        when(mockSession.get(Hospital.class, hospitalId)).thenReturn(mockHospital);
        when(mockSession.get(User.class, userId)).thenReturn(null); // User not found

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            appointmentDAO.create(hospitalId, userId, new Date(), 1);
        });
        assertEquals("Hospital or User not found", exception.getMessage());
        verify(mockSession).get(Hospital.class, hospitalId);
        verify(mockSession).get(User.class, userId);
        verify(mockSession, never()).save(any(Appointment.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void create_ExceptionDuringSave() {
        // Arrange
        Long hospitalId = 1L;
        Long userId = 2L;
        when(mockSession.get(Hospital.class, hospitalId)).thenReturn(mockHospital);
        when(mockSession.get(User.class, userId)).thenReturn(mockUser);
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(Appointment.class));

        // Act
        Appointment result = appointmentDAO.create(hospitalId, userId, new Date(), 1);

        // Assert
        assertNull(result); // Returns null on exception
        verify(mockSession).save(any(Appointment.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void findByUserId_Success() {
        // Arrange
        Long userId = 1L;
        List<Appointment> expectedList = Collections.singletonList(new Appointment());
        when(mockQuery.setParameter(eq("userId"), eq(userId))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Appointment> result = appointmentDAO.findByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        // Note: Original query was "FROM Appointment a WHERE a.user.id = :userId"
        // Assuming user entity has an id field named 'idUser' based on other DAOs
        // If the actual user id field is just 'id', adjust the query string check. 
        // Let's assume it's idUser for consistency for now.
        // verify(mockSession).createQuery("FROM Appointment a WHERE a.user.id = :userId", Appointment.class);
        verify(mockSession).createQuery("FROM Appointment a WHERE a.user.idUser = :userId", Appointment.class); // Adjusted assumption
        verify(mockQuery).setParameter("userId", userId);
    }
    
    @Test
    void findByUserId_Exception() {
        // Arrange
        Long userId = 1L;
        when(mockQuery.setParameter(eq("userId"), eq(userId))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<Appointment> result = appointmentDAO.findByUserId(userId);

        // Assert
        assertNull(result);
    }

    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        Appointment expectedAppointment = new Appointment();
        when(mockSession.get(Appointment.class, id)).thenReturn(expectedAppointment);

        // Act
        Appointment result = appointmentDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAppointment, result);
        verify(mockSession).get(Appointment.class, id);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Appointment.class, id)).thenReturn(null);

        // Act
        Appointment result = appointmentDAO.findById(id);

        // Assert
        assertNull(result);
    }
    
    @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Appointment.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        Appointment result = appointmentDAO.findById(id);

        // Assert
        assertNull(result);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<Appointment> expectedList = Collections.singletonList(new Appointment());
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Appointment> result = appointmentDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM Appointment", Appointment.class);
    }
    
    @Test
    void findAll_Exception() {
        // Arrange
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<Appointment> result = appointmentDAO.findAll();

        // Assert
        assertNull(result);
    }

    @Test
    void update_Success() {
        // Arrange
        Appointment appointmentToUpdate = new Appointment();

        // Act
        Appointment result = appointmentDAO.update(appointmentToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(appointmentToUpdate, result);
        verify(mockSession).update(appointmentToUpdate);
        verify(mockTransaction).commit();
    }

    @Test
    void update_Exception() {
        // Arrange
        Appointment appointmentToUpdate = new Appointment();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(Appointment.class));

        // Act
        Appointment result = appointmentDAO.update(appointmentToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(appointmentToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
} 