package com.sources.app.dao;

import com.sources.app.entities.Medicine;
import com.sources.app.entities.MedicinePres;
import com.sources.app.entities.MedicinePresId;
import com.sources.app.entities.Prescription;
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
class MedicinePresDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<MedicinePres> mockQuery;
    @Mock
    private Prescription mockPrescription;
    @Mock
    private Medicine mockMedicine;

    @Captor
    ArgumentCaptor<MedicinePres> medicinePresCaptor;
    @Captor
    ArgumentCaptor<MedicinePresId> medicinePresIdCaptor;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @InjectMocks
    private MedicinePresDAO medicinePresDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockSession.createQuery(anyString(), eq(MedicinePres.class))).thenReturn(mockQuery);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success() {
        // Arrange
        Long prescriptionId = 1L;
        Long medicineId = 2L;

        when(mockSession.get(Prescription.class, prescriptionId)).thenReturn(mockPrescription);
        when(mockSession.get(Medicine.class, medicineId)).thenReturn(mockMedicine);

        // Act
        MedicinePres result = medicinePresDAO.create(prescriptionId, medicineId);

        // Assert
        assertNotNull(result);
        // Use captor to verify the object saved has the correct entities
        verify(mockSession).save(medicinePresCaptor.capture());
        MedicinePres savedEntity = medicinePresCaptor.getValue();
        assertEquals(mockPrescription, savedEntity.getPrescription());
        assertEquals(mockMedicine, savedEntity.getMedicine());
        verify(mockTransaction).commit();
    }

    @Test
    void create_PrescriptionNotFound() {
        // Arrange
        Long prescriptionId = 1L;
        Long medicineId = 2L;
        when(mockSession.get(Prescription.class, prescriptionId)).thenReturn(null);
        when(mockSession.get(Medicine.class, medicineId)).thenReturn(mockMedicine);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            medicinePresDAO.create(prescriptionId, medicineId);
        });
        assertEquals("Prescription or Medicine not found", exception.getMessage());
        verify(mockSession).get(Prescription.class, prescriptionId);
        verify(mockSession, never()).get(eq(Medicine.class), eq(medicineId)); // Should not check medicine
        verify(mockSession, never()).save(any());
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
    
     @Test
    void create_MedicineNotFound() {
        // Arrange
        Long prescriptionId = 1L;
        Long medicineId = 2L;
        when(mockSession.get(Prescription.class, prescriptionId)).thenReturn(mockPrescription);
        when(mockSession.get(Medicine.class, medicineId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            medicinePresDAO.create(prescriptionId, medicineId);
        });
        assertEquals("Prescription or Medicine not found", exception.getMessage());
        verify(mockSession).get(Prescription.class, prescriptionId);
        verify(mockSession).get(Medicine.class, medicineId);
        verify(mockSession, never()).save(any());
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void create_ExceptionDuringSave() {
        // Arrange
        Long prescriptionId = 1L;
        Long medicineId = 2L;
        when(mockSession.get(Prescription.class, prescriptionId)).thenReturn(mockPrescription);
        when(mockSession.get(Medicine.class, medicineId)).thenReturn(mockMedicine);
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(MedicinePres.class));

        // Act
        MedicinePres result = medicinePresDAO.create(prescriptionId, medicineId);

        // Assert
        assertNull(result); // Returns null
        verify(mockSession).save(any(MedicinePres.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void findById_Found() {
        // Arrange
        Long prescriptionId = 1L;
        Long medicineId = 2L;
        MedicinePres expectedEntity = new MedicinePres();
        // Use captor to check the composite ID passed to get()
        when(mockSession.get(eq(MedicinePres.class), medicinePresIdCaptor.capture())).thenReturn(expectedEntity);

        // Act
        MedicinePres result = medicinePresDAO.findById(prescriptionId, medicineId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedEntity, result);
        MedicinePresId capturedId = medicinePresIdCaptor.getValue();
        assertNotNull(capturedId); // Verify the ID object was captured
        verify(mockSession).get(eq(MedicinePres.class), any(MedicinePresId.class));
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long prescriptionId = 1L;
        Long medicineId = 2L;
        when(mockSession.get(eq(MedicinePres.class), any(MedicinePresId.class))).thenReturn(null);

        // Act
        MedicinePres result = medicinePresDAO.findById(prescriptionId, medicineId);

        // Assert
        assertNull(result);
    }
    
    @Test
    void findById_Exception() {
        // Arrange
        Long prescriptionId = 1L;
        Long medicineId = 2L;
        when(mockSession.get(eq(MedicinePres.class), any(MedicinePresId.class))).thenThrow(new RuntimeException("DB Error"));

        // Act
        MedicinePres result = medicinePresDAO.findById(prescriptionId, medicineId);

        // Assert
        assertNull(result);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<MedicinePres> expectedList = Collections.singletonList(new MedicinePres());
        when(mockQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<MedicinePres> result = medicinePresDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM MedicinePres", MedicinePres.class);
    }
    
     @Test
    void findAll_Exception() {
        // Arrange
        when(mockQuery.getResultList()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<MedicinePres> result = medicinePresDAO.findAll();

        // Assert
        assertNull(result);
    }

    @Test
    void update_Success() {
        // Arrange
        MedicinePres medPresToUpdate = new MedicinePres();

        // Act
        MedicinePres result = medicinePresDAO.update(medPresToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(medPresToUpdate, result);
        verify(mockSession).update(medPresToUpdate);
        verify(mockTransaction).commit();
    }

    @Test
    void update_Exception() {
        // Arrange
        MedicinePres medPresToUpdate = new MedicinePres();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(MedicinePres.class));

        // Act
        MedicinePres result = medicinePresDAO.update(medPresToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).update(medPresToUpdate);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
} 