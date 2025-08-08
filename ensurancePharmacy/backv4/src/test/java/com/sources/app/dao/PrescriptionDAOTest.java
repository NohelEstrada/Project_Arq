package com.sources.app.dao;

import com.sources.app.entities.*;
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
class PrescriptionDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<Prescription> mockQuery;
    @Mock
    private Hospital mockHospital;
    @Mock
    private User mockUser;
    @Mock
    private Medicine mockMedicine;
    @Mock
    private Pharmacy mockPharmacy;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private PrescriptionDAO prescriptionDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockSession.createQuery(anyString(), eq(Prescription.class))).thenReturn(mockQuery);
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
        Long medicineId = 3L;
        Long pharmacyId = 4L;
        Date presDate = new Date();
        BigDecimal total = new BigDecimal("50.00");
        BigDecimal copay = new BigDecimal("5.00");
        String comment = "Take with food";
        Integer secured = 1;
        String auth = "AUTH123";

        when(mockSession.get(Hospital.class, hospitalId)).thenReturn(mockHospital);
        when(mockSession.get(User.class, userId)).thenReturn(mockUser);
        when(mockSession.get(Medicine.class, medicineId)).thenReturn(mockMedicine);
        when(mockSession.get(Pharmacy.class, pharmacyId)).thenReturn(mockPharmacy);

        // Act
        Prescription result = prescriptionDAO.create(hospitalId, userId, medicineId, pharmacyId,
                presDate, total, copay, comment, secured, auth);

        // Assert
        assertNotNull(result);
        assertEquals(mockHospital, result.getHospital());
        assertEquals(mockUser, result.getUser());
        assertEquals(mockMedicine, result.getMedicine());
        assertEquals(mockPharmacy, result.getPharmacy());
        assertEquals(presDate, result.getPrescriptionDate());
        assertEquals(total, result.getTotal());
        // ... assert other fields
        verify(mockSession).save(any(Prescription.class));
        verify(mockTransaction).commit();
    }

    @Test
    void create_RelatedEntityNotFound() {
        // Arrange
        Long hospitalId = 1L;
        Long userId = 2L;
        Long medicineId = 3L;
        Long pharmacyId = 4L;

        when(mockSession.get(Hospital.class, hospitalId)).thenReturn(mockHospital);
        when(mockSession.get(User.class, userId)).thenReturn(null); // User not found
        // No need to mock medicine/pharmacy if user check fails

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
             prescriptionDAO.create(hospitalId, userId, medicineId, pharmacyId,
                new Date(), BigDecimal.ZERO, BigDecimal.ZERO, "", 0, "");
        });
        assertTrue(exception.getMessage().contains("Alguna entidad relacionada no fue encontrada"));
        verify(mockSession).get(Hospital.class, hospitalId);
        verify(mockSession).get(User.class, userId);
        verify(mockSession, never()).get(eq(Medicine.class), anyLong());
        verify(mockSession, never()).get(eq(Pharmacy.class), anyLong());
        verify(mockSession, never()).save(any(Prescription.class));
        verify(mockTransaction).rollback(); // Rollback should occur
        verify(mockTransaction, never()).commit();
    }

    @Test
    void create_ExceptionDuringSave() {
        // Arrange
        Long hospitalId = 1L, userId = 2L, medicineId = 3L, pharmacyId = 4L;
        when(mockSession.get(Hospital.class, hospitalId)).thenReturn(mockHospital);
        when(mockSession.get(User.class, userId)).thenReturn(mockUser);
        when(mockSession.get(Medicine.class, medicineId)).thenReturn(mockMedicine);
        when(mockSession.get(Pharmacy.class, pharmacyId)).thenReturn(mockPharmacy);
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(Prescription.class));

        // Act
        Prescription result = prescriptionDAO.create(hospitalId, userId, medicineId, pharmacyId,
                new Date(), BigDecimal.ZERO, BigDecimal.ZERO, "", 0, "");

        // Assert
        assertNull(result); // Method returns null on exception
        verify(mockSession).save(any(Prescription.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void findByUserId_Success() {
        // Arrange
        Long userId = 1L;
        List<Prescription> expectedList = Collections.singletonList(new Prescription());
        when(mockQuery.setParameter(eq("idUser"), eq(userId))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Prescription> result = prescriptionDAO.findByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM Prescription p WHERE p.user.idUser = :idUser", Prescription.class);
        verify(mockQuery).setParameter("idUser", userId);
    }
    
    @Test
    void findByUserId_Exception() {
        // Arrange
        Long userId = 1L;
        when(mockQuery.setParameter(eq("idUser"), eq(userId))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<Prescription> result = prescriptionDAO.findByUserId(userId);

        // Assert
        assertNull(result);
    }

    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        Prescription expectedPrescription = new Prescription();
        when(mockSession.get(Prescription.class, id)).thenReturn(expectedPrescription);

        // Act
        Prescription result = prescriptionDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPrescription, result);
        verify(mockSession).get(Prescription.class, id);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(Prescription.class, id)).thenReturn(null);

        // Act
        Prescription result = prescriptionDAO.findById(id);

        // Assert
        assertNull(result);
    }
    
    @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
         when(mockSession.get(Prescription.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        Prescription result = prescriptionDAO.findById(id);

        // Assert
        assertNull(result);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<Prescription> expectedList = Collections.singletonList(new Prescription());
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<Prescription> result = prescriptionDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM Prescription", Prescription.class);
    }
    
     @Test
    void findAll_Exception() {
        // Arrange
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<Prescription> result = prescriptionDAO.findAll();

        // Assert
        assertNull(result);
    }

    @Test
    void update_Success() {
        // Arrange
        Prescription prescriptionToUpdate = new Prescription();

        // Act
        Prescription result = prescriptionDAO.update(prescriptionToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(prescriptionToUpdate, result);
        verify(mockSession).update(prescriptionToUpdate);
        verify(mockTransaction).commit();
    }

    @Test
    void update_Exception() {
        // Arrange
        Prescription prescriptionToUpdate = new Prescription();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(Prescription.class));

        // Act
        Prescription result = prescriptionDAO.update(prescriptionToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(prescriptionToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
} 