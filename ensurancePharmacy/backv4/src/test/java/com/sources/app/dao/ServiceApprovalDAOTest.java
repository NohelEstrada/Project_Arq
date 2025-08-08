package com.sources.app.dao;

import com.sources.app.entities.ServiceApproval;
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
class ServiceApprovalDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<ServiceApproval> mockQuery;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    // Spy needed for methods calling other public methods (updatePrescriptionInfo, updateStatus)
    @InjectMocks
    private ServiceApprovalDAO serviceApprovalDAO;
    private ServiceApprovalDAO daoSpy;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockSession.createQuery(anyString(), eq(ServiceApproval.class))).thenReturn(mockQuery);
        daoSpy = Mockito.spy(serviceApprovalDAO);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void create_Success_WithCodeAndDateProvided() {
        // Arrange
        ServiceApproval approval = new ServiceApproval();
        approval.setApprovalCode("MANUALCODE");
        approval.setApprovalDate(new Date());
        // Set other required fields

        // Act
        ServiceApproval result = serviceApprovalDAO.create(approval);

        // Assert
        assertNotNull(result);
        assertEquals(approval, result);
        assertEquals("MANUALCODE", result.getApprovalCode()); // Should use provided code
        assertNotNull(result.getApprovalDate()); // Should use provided date
        verify(mockSession).save(approval);
        verify(mockTransaction).commit();
    }

    @Test
    void create_Success_GenerateCodeAndDate() {
        // Arrange
        ServiceApproval approval = new ServiceApproval();
        // approvalCode and approvalDate are null

        // Act
        ServiceApproval result = serviceApprovalDAO.create(approval);

        // Assert
        assertNotNull(result);
        assertEquals(approval, result);
        assertNotNull(result.getApprovalCode()); // Should generate code
        assertTrue(result.getApprovalCode().startsWith("AP"));
        assertEquals(10, result.getApprovalCode().length()); // AP + 8 chars
        assertNotNull(result.getApprovalDate()); // Should set current date
        verify(mockSession).save(approval);
        verify(mockTransaction).commit();
    }

    @Test
    void create_Exception() {
        // Arrange
        ServiceApproval approval = new ServiceApproval();
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(ServiceApproval.class));

        // Act
        ServiceApproval result = serviceApprovalDAO.create(approval);

        // Assert
        assertNull(result);
        verify(mockSession).save(approval);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void update_Success() {
        // Arrange
        ServiceApproval approval = new ServiceApproval();

        // Act
        ServiceApproval result = serviceApprovalDAO.update(approval);

        // Assert
        assertNotNull(result);
        assertEquals(approval, result);
        verify(mockSession).update(approval);
        verify(mockTransaction).commit();
    }

    @Test
    void update_Exception() {
        // Arrange
        ServiceApproval approval = new ServiceApproval();
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(ServiceApproval.class));

        // Act
        ServiceApproval result = serviceApprovalDAO.update(approval);

        // Assert
        assertNull(result);
        verify(mockSession).update(approval);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    // Use Spy for updatePrescriptionInfo
    @Test
    void updatePrescriptionInfo_Success() {
        // Arrange
        String approvalCode = "AP123";
        Long prescriptionId = 5L;
        Double total = 150.75;
        ServiceApproval existingApproval = new ServiceApproval();
        existingApproval.setApprovalCode(approvalCode);
        
        doReturn(existingApproval).when(daoSpy).getByApprovalCode(approvalCode);
        // Stub the internal update call
        doReturn(existingApproval).when(daoSpy).update(any(ServiceApproval.class)); 

        // Act
        ServiceApproval result = daoSpy.updatePrescriptionInfo(approvalCode, prescriptionId, total);

        // Assert
        assertNotNull(result);
        assertEquals(prescriptionId, result.getPrescriptionId());
        assertEquals(total, result.getPrescriptionTotal());
        verify(daoSpy).getByApprovalCode(approvalCode);
        verify(daoSpy).update(existingApproval); // Verify the specific object was passed to update
    }
    
    @Test
    void updatePrescriptionInfo_ApprovalNotFound() {
        // Arrange
        String approvalCode = "AP_NOTFOUND";
        doReturn(null).when(daoSpy).getByApprovalCode(approvalCode);

        // Act
        ServiceApproval result = daoSpy.updatePrescriptionInfo(approvalCode, 1L, 10.0);

        // Assert
        assertNull(result);
        verify(daoSpy).getByApprovalCode(approvalCode);
        verify(daoSpy, never()).update(any(ServiceApproval.class));
    }
    
    @Test
    void updatePrescriptionInfo_UpdateFailed() {
        // Arrange
        String approvalCode = "AP_UPDATE_FAIL";
        ServiceApproval existingApproval = new ServiceApproval();
        existingApproval.setApprovalCode(approvalCode);
        
        doReturn(existingApproval).when(daoSpy).getByApprovalCode(approvalCode);
        doReturn(null).when(daoSpy).update(any(ServiceApproval.class)); // Simulate update returning null

        // Act
        ServiceApproval result = daoSpy.updatePrescriptionInfo(approvalCode, 1L, 10.0);

        // Assert
        assertNull(result);
        verify(daoSpy).getByApprovalCode(approvalCode);
        verify(daoSpy).update(existingApproval);
    }

    // Use Spy for updateStatus
    @Test
    void updateStatus_Success_Completed() {
        // Arrange
        String approvalCode = "AP_COMPLETE";
        String newStatus = "COMPLETED";
        ServiceApproval existingApproval = new ServiceApproval();
        existingApproval.setApprovalCode(approvalCode);
        existingApproval.setStatus("PENDING");
        
        doReturn(existingApproval).when(daoSpy).getByApprovalCode(approvalCode);
        doReturn(existingApproval).when(daoSpy).update(any(ServiceApproval.class));
        
        // Act
        ServiceApproval result = daoSpy.updateStatus(approvalCode, newStatus, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        assertNotNull(result.getCompletedDate());
        assertNull(result.getRejectionReason());
        verify(daoSpy).getByApprovalCode(approvalCode);
        verify(daoSpy).update(existingApproval);
    }
    
     @Test
    void updateStatus_Success_Rejected() {
        // Arrange
        String approvalCode = "AP_REJECT";
        String newStatus = "REJECTED";
        String reason = "Insufficient funds";
        ServiceApproval existingApproval = new ServiceApproval();
        existingApproval.setApprovalCode(approvalCode);
        existingApproval.setStatus("PENDING");
        
        doReturn(existingApproval).when(daoSpy).getByApprovalCode(approvalCode);
        doReturn(existingApproval).when(daoSpy).update(any(ServiceApproval.class));
        
        // Act
        ServiceApproval result = daoSpy.updateStatus(approvalCode, newStatus, reason);
        
        // Assert
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        assertEquals(reason, result.getRejectionReason());
        assertNull(result.getCompletedDate());
        verify(daoSpy).getByApprovalCode(approvalCode);
        verify(daoSpy).update(existingApproval);
    }
    
    @Test
    void updateStatus_ApprovalNotFound() {
        // Arrange
        String approvalCode = "AP_STATUS_NOTFOUND";
        doReturn(null).when(daoSpy).getByApprovalCode(approvalCode);
        
        // Act
        ServiceApproval result = daoSpy.updateStatus(approvalCode, "COMPLETED", null);
        
        // Assert
        assertNull(result);
        verify(daoSpy).getByApprovalCode(approvalCode);
        verify(daoSpy, never()).update(any(ServiceApproval.class));
    }
    
     @Test
    void updateStatus_UpdateFailed() {
        // Arrange
        String approvalCode = "AP_STATUS_UPDATE_FAIL";
        ServiceApproval existingApproval = new ServiceApproval();
        existingApproval.setApprovalCode(approvalCode);
        
        doReturn(existingApproval).when(daoSpy).getByApprovalCode(approvalCode);
        doReturn(null).when(daoSpy).update(any(ServiceApproval.class)); // Simulate update failure
        
        // Act
        ServiceApproval result = daoSpy.updateStatus(approvalCode, "COMPLETED", null);
        
        // Assert
        assertNull(result);
        verify(daoSpy).getByApprovalCode(approvalCode);
        verify(daoSpy).update(existingApproval);
    }

    @Test
    void getById_Found() {
        // Arrange
        Long id = 1L;
        ServiceApproval expectedApproval = new ServiceApproval();
        when(mockSession.get(ServiceApproval.class, id)).thenReturn(expectedApproval);

        // Act
        ServiceApproval result = serviceApprovalDAO.getById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedApproval, result);
        verify(mockSession).get(ServiceApproval.class, id);
    }

    @Test
    void getById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(ServiceApproval.class, id)).thenReturn(null);

        // Act
        ServiceApproval result = serviceApprovalDAO.getById(id);

        // Assert
        assertNull(result);
    }
    
     @Test
    void getById_Exception() {
        // Arrange
        Long id = 1L;
         when(mockSession.get(ServiceApproval.class, id)).thenThrow(new RuntimeException("DB Error"));

        // Act
        ServiceApproval result = serviceApprovalDAO.getById(id);

        // Assert
        assertNull(result);
    }

    @Test
    void getByApprovalCode_Found() {
        // Arrange
        String code = "AP12345678";
        ServiceApproval expectedApproval = new ServiceApproval();
        when(mockQuery.setParameter(eq("code"), eq(code))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenReturn(expectedApproval);

        // Act
        ServiceApproval result = serviceApprovalDAO.getByApprovalCode(code);

        // Assert
        assertNotNull(result);
        assertEquals(expectedApproval, result);
        verify(mockSession).createQuery("FROM ServiceApproval WHERE approvalCode = :code", ServiceApproval.class);
        verify(mockQuery).setParameter("code", code);
    }

    @Test
    void getByApprovalCode_NotFound() {
        // Arrange
        String code = "AP_NOTFOUND";
        when(mockQuery.setParameter(eq("code"), eq(code))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenReturn(null);

        // Act
        ServiceApproval result = serviceApprovalDAO.getByApprovalCode(code);

        // Assert
        assertNull(result);
    }
    
     @Test
    void getByApprovalCode_Exception() {
        // Arrange
        String code = "AP_ERROR";
        when(mockQuery.setParameter(eq("code"), eq(code))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenThrow(new RuntimeException("DB Error"));

        // Act
        ServiceApproval result = serviceApprovalDAO.getByApprovalCode(code);

        // Assert
        assertNull(result);
    }

    @Test
    void getByUserId_Success() {
        // Arrange
        Long userId = 1L;
        List<ServiceApproval> expectedList = Collections.singletonList(new ServiceApproval());
        when(mockQuery.setParameter(eq("userId"), eq(userId))).thenReturn(mockQuery);
        when(mockQuery.list()).thenReturn(expectedList);

        // Act
        List<ServiceApproval> result = serviceApprovalDAO.getByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM ServiceApproval WHERE userId = :userId ORDER BY approvalDate DESC", ServiceApproval.class);
        verify(mockQuery).setParameter("userId", userId);
    }
    
    @Test
    void getByUserId_Exception() {
        // Arrange
        Long userId = 1L;
        when(mockQuery.setParameter(eq("userId"), eq(userId))).thenReturn(mockQuery);
        when(mockQuery.list()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<ServiceApproval> result = serviceApprovalDAO.getByUserId(userId);

        // Assert
        assertNull(result);
    }

    @Test
    void getByHospitalId_Success() {
        // Arrange
        Long hospitalId = 1L;
        List<ServiceApproval> expectedList = Collections.singletonList(new ServiceApproval());
        when(mockQuery.setParameter(eq("hospitalId"), eq(hospitalId))).thenReturn(mockQuery);
        when(mockQuery.list()).thenReturn(expectedList);

        // Act
        List<ServiceApproval> result = serviceApprovalDAO.getByHospitalId(hospitalId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM ServiceApproval WHERE hospitalId = :hospitalId ORDER BY approvalDate DESC", ServiceApproval.class);
        verify(mockQuery).setParameter("hospitalId", hospitalId);
    }
    
    @Test
    void getByHospitalId_Exception() {
        // Arrange
        Long hospitalId = 1L;
        when(mockQuery.setParameter(eq("hospitalId"), eq(hospitalId))).thenReturn(mockQuery);
        when(mockQuery.list()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<ServiceApproval> result = serviceApprovalDAO.getByHospitalId(hospitalId);

        // Assert
        assertNull(result);
    }

    @Test
    void getAll_Success() {
        // Arrange
        List<ServiceApproval> expectedList = Collections.singletonList(new ServiceApproval());
        when(mockQuery.list()).thenReturn(expectedList);

        // Act
        List<ServiceApproval> result = serviceApprovalDAO.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM ServiceApproval ORDER BY approvalDate DESC", ServiceApproval.class);
        verify(mockQuery).list();
    }
    
    @Test
    void getAll_Exception() {
        // Arrange
        when(mockQuery.list()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<ServiceApproval> result = serviceApprovalDAO.getAll();

        // Assert
        assertNull(result);
    }

    // Test alias methods using spy
    @Test
    void findAll_CallsGetAll() {
        // Arrange
        List<ServiceApproval> expectedList = Collections.emptyList();
        doReturn(expectedList).when(daoSpy).getAll();

        // Act
        List<ServiceApproval> result = daoSpy.findAll();

        // Assert
        assertEquals(expectedList, result);
        verify(daoSpy).getAll();
    }

    @Test
    void findByApprovalCode_CallsGetByApprovalCode() {
        // Arrange
        String code = "ALIAS_CODE";
        ServiceApproval expectedApproval = new ServiceApproval();
        doReturn(expectedApproval).when(daoSpy).getByApprovalCode(code);

        // Act
        ServiceApproval result = daoSpy.findByApprovalCode(code);

        // Assert
        assertEquals(expectedApproval, result);
        verify(daoSpy).getByApprovalCode(code);
    }
    
     @Test
    void findByPrescriptionId_Found() {
        // Arrange
        Long prescriptionId = 99L;
        ServiceApproval expectedApproval = new ServiceApproval();
        when(mockQuery.setParameter(eq("prescriptionId"), eq(prescriptionId))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenReturn(expectedApproval);

        // Act
        ServiceApproval result = serviceApprovalDAO.findByPrescriptionId(prescriptionId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedApproval, result);
        verify(mockSession).createQuery("FROM ServiceApproval WHERE prescriptionId = :prescriptionId", ServiceApproval.class);
        verify(mockQuery).setParameter("prescriptionId", prescriptionId);
    }

    @Test
    void findByPrescriptionId_NotFound() {
        // Arrange
        Long prescriptionId = 99L;
        when(mockQuery.setParameter(eq("prescriptionId"), eq(prescriptionId))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenReturn(null);

        // Act
        ServiceApproval result = serviceApprovalDAO.findByPrescriptionId(prescriptionId);

        // Assert
        assertNull(result);
    }
    
     @Test
    void findByPrescriptionId_Exception() {
        // Arrange
        Long prescriptionId = 99L;
        when(mockQuery.setParameter(eq("prescriptionId"), eq(prescriptionId))).thenReturn(mockQuery);
        when(mockQuery.uniqueResult()).thenThrow(new RuntimeException("DB Error"));

        // Act
        ServiceApproval result = serviceApprovalDAO.findByPrescriptionId(prescriptionId);

        // Assert
        assertNull(result);
    }
    
    // Note: generateApprovalCode is private and implicitly tested via the create method tests.
} 