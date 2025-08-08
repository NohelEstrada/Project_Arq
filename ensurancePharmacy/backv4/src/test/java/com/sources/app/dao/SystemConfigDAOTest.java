package com.sources.app.dao;

import com.sources.app.entities.SystemConfig;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemConfigDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<SystemConfig> mockConfigQuery;
    @Mock // For the delete query
    private Query mockGenericQuery;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    // Spy needed to test methods that call other public methods of the same DAO
    @InjectMocks
    private SystemConfigDAO systemConfigDAO;
    private SystemConfigDAO daoSpy; // Spy instance

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSession);
        lenient().when(mockSession.beginTransaction()).thenReturn(mockTransaction);
        lenient().when(mockSession.createQuery(anyString(), eq(SystemConfig.class))).thenReturn(mockConfigQuery);
        // Spy the injected instance
        daoSpy = Mockito.spy(systemConfigDAO);
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void saveOrUpdate_CreateNew() {
        // Arrange
        String key = "NEW_KEY";
        String value = "new_value";
        String description = "New description";
        when(mockConfigQuery.setParameter(eq("key"), eq(key))).thenReturn(mockConfigQuery);
        when(mockConfigQuery.uniqueResult()).thenReturn(null); // Simulate key not found

        // Act
        SystemConfig result = systemConfigDAO.saveOrUpdate(key, value, description);

        // Assert
        assertNotNull(result);
        assertEquals(key, result.getConfigKey());
        assertEquals(value, result.getConfigValue());
        assertEquals(description, result.getConfigDescription());
        verify(mockSession).save(any(SystemConfig.class));
        verify(mockSession, never()).update(any(SystemConfig.class));
        verify(mockTransaction).commit();
    }

    @Test
    void saveOrUpdate_UpdateExisting() {
        // Arrange
        String key = "EXISTING_KEY";
        String newValue = "updated_value";
        String newDescription = "Updated description";
        SystemConfig existingConfig = new SystemConfig(key, "old_value", "Old desc");
        when(mockConfigQuery.setParameter(eq("key"), eq(key))).thenReturn(mockConfigQuery);
        when(mockConfigQuery.uniqueResult()).thenReturn(existingConfig); // Simulate key found

        // Act
        SystemConfig result = systemConfigDAO.saveOrUpdate(key, newValue, newDescription);

        // Assert
        assertNotNull(result);
        assertEquals(key, result.getConfigKey());
        assertEquals(newValue, result.getConfigValue());
        assertEquals(newDescription, result.getConfigDescription());
        assertEquals(existingConfig, result); // Should return the updated object
        verify(mockSession, never()).save(any(SystemConfig.class));
        verify(mockSession).update(existingConfig);
        verify(mockTransaction).commit();
    }
    
     @Test
    void saveOrUpdate_UpdateExisting_NullDescription() {
        // Arrange
        String key = "EXISTING_KEY";
        String newValue = "updated_value";
        String oldDescription = "Old desc";
        SystemConfig existingConfig = new SystemConfig(key, "old_value", oldDescription);
        when(mockConfigQuery.setParameter(eq("key"), eq(key))).thenReturn(mockConfigQuery);
        when(mockConfigQuery.uniqueResult()).thenReturn(existingConfig); // Simulate key found

        // Act
        SystemConfig result = systemConfigDAO.saveOrUpdate(key, newValue, null); // Pass null description

        // Assert
        assertNotNull(result);
        assertEquals(newValue, result.getConfigValue());
        assertEquals(oldDescription, result.getConfigDescription()); // Description should not change
        verify(mockSession).update(existingConfig);
        verify(mockTransaction).commit();
    }

    @Test
    void saveOrUpdate_Exception() {
        // Arrange
        String key = "ERROR_KEY";
        when(mockConfigQuery.setParameter(eq("key"), eq(key))).thenReturn(mockConfigQuery);
        when(mockConfigQuery.uniqueResult()).thenThrow(new RuntimeException("DB Find Error"));

        // Act
        SystemConfig result = systemConfigDAO.saveOrUpdate(key, "v", "d");

        // Assert
        assertNull(result);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void getByKey_Found() {
        // Arrange
        String key = "TEST_KEY";
        SystemConfig expectedConfig = new SystemConfig(key, "value", "desc");
        when(mockConfigQuery.setParameter(eq("key"), eq(key))).thenReturn(mockConfigQuery);
        when(mockConfigQuery.uniqueResult()).thenReturn(expectedConfig);

        // Act
        SystemConfig result = systemConfigDAO.getByKey(key);

        // Assert
        assertNotNull(result);
        assertEquals(expectedConfig, result);
        verify(mockSession).createQuery("FROM SystemConfig WHERE configKey = :key", SystemConfig.class);
        verify(mockConfigQuery).setParameter("key", key);
    }

    @Test
    void getByKey_NotFound() {
        // Arrange
        String key = "NOT_FOUND_KEY";
        when(mockConfigQuery.setParameter(eq("key"), eq(key))).thenReturn(mockConfigQuery);
        when(mockConfigQuery.uniqueResult()).thenReturn(null);

        // Act
        SystemConfig result = systemConfigDAO.getByKey(key);

        // Assert
        assertNull(result);
    }
    
     @Test
    void getByKey_Exception() {
        // Arrange
        String key = "ERROR_KEY";
        when(mockConfigQuery.setParameter(eq("key"), eq(key))).thenReturn(mockConfigQuery);
        when(mockConfigQuery.uniqueResult()).thenThrow(new RuntimeException("DB Error"));

        // Act
        SystemConfig result = systemConfigDAO.getByKey(key);

        // Assert
        assertNull(result);
    }

    // Test getConfigValue using the spy
    @Test
    void getConfigValue_Found() {
        // Arrange
        String key = "KEY1";
        String expectedValue = "VALUE1";
        String defaultValue = "DEFAULT";
        SystemConfig config = new SystemConfig(key, expectedValue, "desc");
        doReturn(config).when(daoSpy).getByKey(key);

        // Act
        String result = daoSpy.getConfigValue(key, defaultValue);

        // Assert
        assertEquals(expectedValue, result);
        verify(daoSpy).getByKey(key);
    }

    @Test
    void getConfigValue_NotFound() {
        // Arrange
        String key = "KEY_NOT_FOUND";
        String defaultValue = "DEFAULT_VALUE";
        doReturn(null).when(daoSpy).getByKey(key);

        // Act
        String result = daoSpy.getConfigValue(key, defaultValue);

        // Assert
        assertEquals(defaultValue, result);
        verify(daoSpy).getByKey(key);
    }
    
    // Tests for getConfigValueAsInt, AsDouble, AsBoolean using the spy
    @Test
    void getConfigValueAsInt_Valid() {
        String key = "INT_KEY";
        Integer defaultValue = 0;
        doReturn("123").when(daoSpy).getConfigValue(key, null);
        assertEquals(123, daoSpy.getConfigValueAsInt(key, defaultValue));
        verify(daoSpy).getConfigValue(key, null);
    }
    
    @Test
    void getConfigValueAsInt_InvalidFormat() {
        String key = "INT_KEY_INVALID";
        Integer defaultValue = 99;
        doReturn("abc").when(daoSpy).getConfigValue(key, null);
        assertEquals(defaultValue, daoSpy.getConfigValueAsInt(key, defaultValue));
        verify(daoSpy).getConfigValue(key, null);
    }
    
    @Test
    void getConfigValueAsInt_NotFound() {
        String key = "INT_KEY_NOTFOUND";
        Integer defaultValue = 55;
        doReturn(null).when(daoSpy).getConfigValue(key, null); // Simulate getByKey returns null
        assertEquals(defaultValue, daoSpy.getConfigValueAsInt(key, defaultValue));
        verify(daoSpy).getConfigValue(key, null);
    }
    
     @Test
    void getConfigValueAsDouble_Valid() {
        String key = "DOUBLE_KEY";
        Double defaultValue = 0.0;
        doReturn("123.45").when(daoSpy).getConfigValue(key, null);
        assertEquals(123.45, daoSpy.getConfigValueAsDouble(key, defaultValue));
        verify(daoSpy).getConfigValue(key, null);
    }
    
    @Test
    void getConfigValueAsDouble_InvalidFormat() {
        String key = "DOUBLE_KEY_INVALID";
        Double defaultValue = 99.9;
        doReturn("xyz").when(daoSpy).getConfigValue(key, null);
        assertEquals(defaultValue, daoSpy.getConfigValueAsDouble(key, defaultValue));
        verify(daoSpy).getConfigValue(key, null);
    }
    
    @Test
    void getConfigValueAsDouble_NotFound() {
        String key = "DOUBLE_KEY_NOTFOUND";
        Double defaultValue = 55.5;
        doReturn(null).when(daoSpy).getConfigValue(key, null); 
        assertEquals(defaultValue, daoSpy.getConfigValueAsDouble(key, defaultValue));
        verify(daoSpy).getConfigValue(key, null);
    }
    
    @Test
    void getConfigValueAsBoolean_TrueValues() {
        String key = "BOOL_KEY";
        Boolean defaultValue = false;
        doReturn("true").when(daoSpy).getConfigValue(key+"_t1", null);
        assertTrue(daoSpy.getConfigValueAsBoolean(key+"_t1", defaultValue));
        doReturn("TRUE").when(daoSpy).getConfigValue(key+"_t2", null);
        assertTrue(daoSpy.getConfigValueAsBoolean(key+"_t2", defaultValue));
        doReturn("1").when(daoSpy).getConfigValue(key+"_t3", null);
        assertTrue(daoSpy.getConfigValueAsBoolean(key+"_t3", defaultValue));
         doReturn("yes").when(daoSpy).getConfigValue(key+"_t4", null);
        assertTrue(daoSpy.getConfigValueAsBoolean(key+"_t4", defaultValue));
         doReturn("YES").when(daoSpy).getConfigValue(key+"_t5", null);
        assertTrue(daoSpy.getConfigValueAsBoolean(key+"_t5", defaultValue));
    }
    
     @Test
    void getConfigValueAsBoolean_FalseValues() {
        String key = "BOOL_KEY_F";
        Boolean defaultValue = true; // Use true default to ensure it returns false
        doReturn("false").when(daoSpy).getConfigValue(key+"_f1", null);
        assertFalse(daoSpy.getConfigValueAsBoolean(key+"_f1", defaultValue));
        doReturn("0").when(daoSpy).getConfigValue(key+"_f2", null);
        assertFalse(daoSpy.getConfigValueAsBoolean(key+"_f2", defaultValue));
        doReturn("no").when(daoSpy).getConfigValue(key+"_f3", null);
        assertFalse(daoSpy.getConfigValueAsBoolean(key+"_f3", defaultValue));
         doReturn("other").when(daoSpy).getConfigValue(key+"_f4", null);
        assertFalse(daoSpy.getConfigValueAsBoolean(key+"_f4", defaultValue));
    }
    
    @Test
    void getConfigValueAsBoolean_NotFound() {
        String key = "BOOL_KEY_NOTFOUND";
        doReturn(null).when(daoSpy).getConfigValue(key, null);
        assertTrue(daoSpy.getConfigValueAsBoolean(key, true)); // Test true default
        assertFalse(daoSpy.getConfigValueAsBoolean(key, false)); // Test false default
    }

    @Test
    void delete_Success() {
        // Arrange
        String key = "DELETE_KEY";
        when(mockSession.createQuery(eq("DELETE FROM SystemConfig WHERE configKey = :key"))).thenReturn(mockGenericQuery);
        when(mockGenericQuery.setParameter(eq("key"), eq(key))).thenReturn(mockGenericQuery);
        when(mockGenericQuery.executeUpdate()).thenReturn(1); // Simulate 1 row deleted

        // Act
        boolean result = systemConfigDAO.delete(key);

        // Assert
        assertTrue(result);
        verify(mockGenericQuery).executeUpdate();
        verify(mockTransaction).commit();
    }

    @Test
    void delete_NotFound() {
        // Arrange
        String key = "DELETE_KEY_NOTFOUND";
        when(mockSession.createQuery(eq("DELETE FROM SystemConfig WHERE configKey = :key"))).thenReturn(mockGenericQuery);
        when(mockGenericQuery.setParameter(eq("key"), eq(key))).thenReturn(mockGenericQuery);
        when(mockGenericQuery.executeUpdate()).thenReturn(0); // Simulate 0 rows deleted

        // Act
        boolean result = systemConfigDAO.delete(key);

        // Assert
        assertFalse(result);
        verify(mockGenericQuery).executeUpdate();
        verify(mockTransaction).commit(); // Commit happens even if 0 deleted
    }

    @Test
    void delete_Exception() {
        // Arrange
        String key = "DELETE_KEY_ERROR";
         when(mockSession.createQuery(eq("DELETE FROM SystemConfig WHERE configKey = :key"))).thenReturn(mockGenericQuery);
        when(mockGenericQuery.setParameter(eq("key"), eq(key))).thenReturn(mockGenericQuery);
        when(mockGenericQuery.executeUpdate()).thenThrow(new RuntimeException("DB Delete Error"));

        // Act
        boolean result = systemConfigDAO.delete(key);

        // Assert
        assertFalse(result);
        verify(mockGenericQuery).executeUpdate();
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void getAll_Success() {
        // Arrange
        List<SystemConfig> expectedList = Collections.singletonList(new SystemConfig());
        when(mockConfigQuery.list()).thenReturn(expectedList);

        // Act
        List<SystemConfig> result = systemConfigDAO.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM SystemConfig ORDER BY configKey", SystemConfig.class);
        verify(mockConfigQuery).list();
    }
    
     @Test
    void getAll_Exception() {
        // Arrange
        when(mockConfigQuery.list()).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<SystemConfig> result = systemConfigDAO.getAll();

        // Assert
        assertNull(result);
    }
    
    @Test
    void initDefaultConfigs_KeyExists() {
         // Arrange
         String key = "MIN_PRESCRIPTION_AMOUNT";
         // Use the spy to mock getByKey returning an existing config
         doReturn(new SystemConfig(key, "200.00", "Existing Desc")).when(daoSpy).getByKey(key);
         
         // Act
         daoSpy.initDefaultConfigs();
         
         // Assert
         verify(daoSpy).getByKey(key); // Verify getByKey was called
         // Verify saveOrUpdate was NOT called because the key exists
         verify(daoSpy, never()).saveOrUpdate(eq(key), anyString(), anyString()); 
    }
    
     @Test
    void initDefaultConfigs_KeyNotFound() {
         // Arrange
         String key = "MIN_PRESCRIPTION_AMOUNT";
         String defaultValue = "250.00";
         String defaultDesc = "Monto mínimo para la aprobación de recetas en quetzales";
         // Use the spy to mock getByKey returning null
         doReturn(null).when(daoSpy).getByKey(key);
         // Also need to stub the saveOrUpdate call that will happen internally
         doReturn(new SystemConfig(key, defaultValue, defaultDesc)).when(daoSpy).saveOrUpdate(key, defaultValue, defaultDesc);
         
         // Act
         daoSpy.initDefaultConfigs();
         
         // Assert
         verify(daoSpy).getByKey(key); // Verify getByKey was called
         // Verify saveOrUpdate WAS called because the key was not found
         verify(daoSpy).saveOrUpdate(key, defaultValue, defaultDesc); 
    }
    
    @Test
    void initDefaultConfigs_GetByKeyException() {
         // Arrange
         String key = "MIN_PRESCRIPTION_AMOUNT";
         // Use the spy to make getByKey throw an exception
         doThrow(new RuntimeException("DB Error on Get")).when(daoSpy).getByKey(key);
         
         // Act
         daoSpy.initDefaultConfigs(); // Should proceed without error, but not call saveOrUpdate
         
         // Assert
         verify(daoSpy).getByKey(key); // Verify getByKey was called
         // Verify saveOrUpdate was NOT called
         verify(daoSpy, never()).saveOrUpdate(anyString(), anyString(), anyString()); 
         // Note: The original DAO doesn't explicitly handle exceptions from getByKey within initDefaultConfigs
         // It would print stack trace and continue. This test reflects that behavior.
    }

} 