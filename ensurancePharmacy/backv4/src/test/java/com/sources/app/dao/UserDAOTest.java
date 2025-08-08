package com.sources.app.dao;

import com.sources.app.entities.Policy;
import com.sources.app.entities.User;
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

import java.util.Date;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDAOTest {

    @Mock
    private SessionFactory mockSessionFactory;
    @Mock
    private Session mockSession;
    @Mock
    private Transaction mockTransaction;
    @Mock
    private Query<User> mockUserQuery;
    @Mock
    private Query<Long> mockLongQuery;
    @Mock
    private Policy mockPolicy;
    @Mock
    private TransactionStatus mockTransactionStatus;

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    // Use a spy for the DAO to test internal method calls like existsUserWithEmail
    // But be careful with spies, they execute real code unless stubbed.
    // Consider if mocking the dependency (the DAO itself in the handler test)
    // is better than spying here.
    // @Spy 
    @InjectMocks
    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        
        // Use lenient() for mocks whose interactions might not always happen in every test path
        lenient().doReturn(mockSession).when(mockSessionFactory).openSession();
        lenient().doReturn(mockTransaction).when(mockSession).beginTransaction();
        // Stub getStatus() globally
        lenient().doReturn(mockTransactionStatus).when(mockTransaction).getStatus();
        // Assume it can rollback by default for tests that might need it
        lenient().doReturn(true).when(mockTransactionStatus).canRollback(); 
    }

    @AfterEach
    void tearDown() {
        mockedHibernateUtil.close();
    }

    @Test
    void login_Success() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        User expectedUser = new User();
        when(mockSession.createQuery(anyString(), eq(User.class))).thenReturn(mockUserQuery);
        when(mockUserQuery.setParameter(eq("email"), eq(email))).thenReturn(mockUserQuery);
        when(mockUserQuery.setParameter(eq("password"), eq(password))).thenReturn(mockUserQuery);
        when(mockUserQuery.uniqueResult()).thenReturn(expectedUser);

        // Act
        User result = userDAO.login(email, password);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUser, result);
        verify(mockSession).createQuery("FROM User WHERE email = :email AND password = :password", User.class);
        verify(mockUserQuery).setParameter("email", email);
        verify(mockUserQuery).setParameter("password", password);
    }

    @Test
    void login_NotFound() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        when(mockSession.createQuery(anyString(), eq(User.class))).thenReturn(mockUserQuery);
        when(mockUserQuery.setParameter(anyString(), anyString())).thenReturn(mockUserQuery);
        when(mockUserQuery.uniqueResult()).thenReturn(null);

        // Act
        User result = userDAO.login(email, password);

        // Assert
        assertNull(result);
    }
    
     @Test
    void login_Exception() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        when(mockSession.createQuery(anyString(), eq(User.class))).thenThrow(new RuntimeException("DB error"));

        // Act
        User result = userDAO.login(email, password);

        // Assert
        assertNull(result);
    }

    @Test
    void existsUserWithEmail_Exists() {
        // Arrange
        String email = "exists@example.com";
        when(mockSession.createQuery(anyString(), eq(Long.class))).thenReturn(mockLongQuery);
        when(mockLongQuery.setParameter(eq("email"), eq(email))).thenReturn(mockLongQuery);
        when(mockLongQuery.uniqueResult()).thenReturn(1L);

        // Act
        boolean exists = userDAO.existsUserWithEmail(email);

        // Assert
        assertTrue(exists);
        verify(mockSession).createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
        verify(mockLongQuery).setParameter("email", email);
    }

    @Test
    void existsUserWithEmail_NotExists() {
       // Arrange
        String email = "new@example.com";
        when(mockSession.createQuery(anyString(), eq(Long.class))).thenReturn(mockLongQuery);
        when(mockLongQuery.setParameter(eq("email"), eq(email))).thenReturn(mockLongQuery);
        when(mockLongQuery.uniqueResult()).thenReturn(0L);

        // Act
        boolean exists = userDAO.existsUserWithEmail(email);

        // Assert
        assertFalse(exists);
    }
    
    @Test
    void existsUserWithEmail_Exception() {
       // Arrange
        String email = "error@example.com";
        when(mockSession.createQuery(anyString(), eq(Long.class))).thenThrow(new RuntimeException("DB error"));

        // Act
        boolean exists = userDAO.existsUserWithEmail(email);

        // Assert
        assertFalse(exists); // Method returns false on exception
    }

    @Test
    void existsUserWithCUI_Exists() {
         // Arrange
        Long cui = 12345L;
        when(mockSession.createQuery(anyString(), eq(Long.class))).thenReturn(mockLongQuery);
        when(mockLongQuery.setParameter(eq("cui"), eq(cui))).thenReturn(mockLongQuery);
        when(mockLongQuery.uniqueResult()).thenReturn(1L);

        // Act
        boolean exists = userDAO.existsUserWithCUI(cui);

        // Assert
        assertTrue(exists);
        verify(mockSession).createQuery("SELECT COUNT(u) FROM User u WHERE u.cui = :cui", Long.class);
        verify(mockLongQuery).setParameter("cui", cui);
    }

    @Test
    void existsUserWithCUI_NotExists() {
         // Arrange
        Long cui = 12345L;
        when(mockSession.createQuery(anyString(), eq(Long.class))).thenReturn(mockLongQuery);
        when(mockLongQuery.setParameter(eq("cui"), eq(cui))).thenReturn(mockLongQuery);
        when(mockLongQuery.uniqueResult()).thenReturn(0L);

        // Act
        boolean exists = userDAO.existsUserWithCUI(cui);

        // Assert
        assertFalse(exists);
    }
    
    @Test
    void existsUserWithCUI_Exception() {
         // Arrange
        Long cui = 12345L;
         when(mockSession.createQuery(anyString(), eq(Long.class))).thenThrow(new RuntimeException("DB error"));

        // Act
        boolean exists = userDAO.existsUserWithCUI(cui);

        // Assert
        assertFalse(exists);
    }

    @Test
    void create_Success() {
        // Arrange
        String name = "Test User";
        Long cui = 123456789L;
        String phone = "1234567890";
        String email = "newuser@example.com";
        Date birthdate = new Date();
        String address = "123 Test St";
        String password = "password";
        
        // Mock the existence checks to return false
        // Need to mock session creation again for these separate calls within create()
        Session mockSessionForCheck = mock(Session.class); 
        Query<Long> mockLongQueryForCheck = mock(Query.class);
        // Ensure mockSessionForCheck is returned for both check calls, then the main mockSession for the save operation
        lenient().when(mockSessionFactory.openSession())
                 .thenReturn(mockSessionForCheck) // For existsUserWithEmail
                 .thenReturn(mockSessionForCheck) // For existsUserWithCUI
                 .thenReturn(mockSession);        // For the actual save transaction
        when(mockSessionForCheck.createQuery(anyString(), eq(Long.class))).thenReturn(mockLongQueryForCheck);
        when(mockLongQueryForCheck.setParameter(anyString(), any())).thenReturn(mockLongQueryForCheck);
        when(mockLongQueryForCheck.uniqueResult()).thenReturn(0L); // Return 0 to indicate not exists
        
        // Act
        User result = userDAO.create(name, cui, phone, email, birthdate, address, password, mockPolicy);

        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(cui, result.getCui());
        assertEquals(email, result.getEmail());
        assertEquals(mockPolicy, result.getPolicy());
        assertEquals(" ", result.getRole()); // Default role
        assertEquals(0, result.getEnabled()); // Default enabled
        assertNull(result.getPaidService()); // Default
        assertNull(result.getExpirationDate()); // Default

        verify(mockSession).save(any(User.class));
        verify(mockTransaction).commit();
        // Verify checks were made on the dedicated check session
        verify(mockSessionForCheck, times(2)).createQuery(anyString(), eq(Long.class));
        verify(mockLongQueryForCheck).setParameter("email", email);
        verify(mockLongQueryForCheck).setParameter("cui", cui);
        verify(mockSessionForCheck, times(2)).close(); // Ensure check sessions are closed
    }
    
    @Test
    void create_EmailExists() {
        // Arrange
        String email = "existing@example.com";
                
        // Mock the email existence check to return true
        Session mockSessionForCheck = mock(Session.class); 
        Query<Long> mockLongQueryForCheck = mock(Query.class);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSessionForCheck); // Only need the check session
        when(mockSessionForCheck.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)).thenReturn(mockLongQueryForCheck);
        when(mockLongQueryForCheck.setParameter("email", email)).thenReturn(mockLongQueryForCheck);
        when(mockLongQueryForCheck.uniqueResult()).thenReturn(1L); // Email exists

        // Act
        User result = userDAO.create("Test", 1L, "1", email, new Date(), "addr", "pass", mockPolicy);

        // Assert
        assertNull(result); // Should return null if email exists
        verify(mockSession, never()).save(any(User.class));
        verify(mockTransaction, never()).commit();
         verify(mockSessionForCheck, times(1)).close();
    }
    
    @Test
    void create_CuiExists() {
         // Arrange
        Long cui = 98765L;
        String email = "newforCui@example.com";
                
        // Mock the email existence check to return false, CUI check true
        Session mockSessionForCheck = mock(Session.class); 
        Query<Long> mockLongQueryEmailCheck = mock(Query.class);
        Query<Long> mockLongQueryCuiCheck = mock(Query.class);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSessionForCheck); // Only need the check session
        // Email check mock
        when(mockSessionForCheck.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)).thenReturn(mockLongQueryEmailCheck);
        when(mockLongQueryEmailCheck.setParameter("email", email)).thenReturn(mockLongQueryEmailCheck);
        when(mockLongQueryEmailCheck.uniqueResult()).thenReturn(0L); // Email doesn't exist
        // CUI check mock
        when(mockSessionForCheck.createQuery("SELECT COUNT(u) FROM User u WHERE u.cui = :cui", Long.class)).thenReturn(mockLongQueryCuiCheck);
        when(mockLongQueryCuiCheck.setParameter("cui", cui)).thenReturn(mockLongQueryCuiCheck);
        when(mockLongQueryCuiCheck.uniqueResult()).thenReturn(1L); // CUI exists

        // Act
        User result = userDAO.create("Test", cui, "1", email, new Date(), "addr", "pass", mockPolicy);

        // Assert
        assertNull(result); // Should return null if CUI exists
        verify(mockSession, never()).save(any(User.class));
        verify(mockTransaction, never()).commit();
        verify(mockSessionForCheck, times(2)).close(); // Both checks close session
    }
    
     @Test
    void create_ExceptionDuringSave() {
        // Arrange
        String email = "exception@example.com";
        Long cui = 1111L;
       // Mock the existence checks to return false
        Session mockSessionForCheck = mock(Session.class); 
        Query<Long> mockLongQueryForCheck = mock(Query.class);
        lenient().when(mockSessionFactory.openSession()).thenReturn(mockSessionForCheck).thenReturn(mockSession); // First calls for checks, then main operation
        when(mockSessionForCheck.createQuery(anyString(), eq(Long.class))).thenReturn(mockLongQueryForCheck);
        when(mockLongQueryForCheck.setParameter(anyString(), any())).thenReturn(mockLongQueryForCheck);
        when(mockLongQueryForCheck.uniqueResult()).thenReturn(0L); // Return 0 to indicate not exists
        
        // Mock save to throw exception
        doThrow(new RuntimeException("DB Save Error")).when(mockSession).save(any(User.class));

        // Act
        User result = userDAO.create("Test", cui, "1", email, new Date(), "addr", "pass", mockPolicy);

        // Assert
        // The method catches the exception and returns the user object *before* the exception
        // This seems like a potential issue in the original DAO code, but we test the actual behavior.
        // If the intent was to return null on failure, the DAO needs adjustment.
        assertNotNull(result); // It returns the partially created user object in the DAO's current state
        verify(mockSession).save(any(User.class));
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<User> expectedList = Collections.singletonList(new User());
        when(mockSession.createQuery(anyString(), eq(User.class))).thenReturn(mockUserQuery);
        when(mockUserQuery.getResultList()).thenReturn(expectedList);

        // Act
        List<User> result = userDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(mockSession).createQuery("FROM User", User.class);
    }
    
    @Test
    void findAll_Exception() {
        // Arrange
        when(mockSession.createQuery(anyString(), eq(User.class))).thenThrow(new RuntimeException("DB error"));

        // Act
        List<User> result = userDAO.findAll();

        // Assert
        assertNull(result);
    }

    @Test
    void findById_Found() {
        // Arrange
        Long id = 1L;
        User expectedUser = new User();
        when(mockSession.get(User.class, id)).thenReturn(expectedUser);

        // Act
        User result = userDAO.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUser, result);
        verify(mockSession).get(User.class, id);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        Long id = 1L;
        when(mockSession.get(User.class, id)).thenReturn(null);

        // Act
        User result = userDAO.findById(id);

        // Assert
        assertNull(result);
    }
    
     @Test
    void findById_Exception() {
        // Arrange
        Long id = 1L;
         when(mockSession.get(User.class, id)).thenThrow(new RuntimeException("DB error"));

        // Act
        User result = userDAO.findById(id);

        // Assert
        assertNull(result);
    }

    @Test
    void update_Success() {
        // Arrange
        Long userId = 1L;
        User userToUpdate = new User();
        userToUpdate.setIdUser(userId);
        userToUpdate.setName("Updated Name");
        userToUpdate.setEmail("update@example.com");
        userToUpdate.setCui(123L);
        userToUpdate.setPhone("111");
        userToUpdate.setAddress("addr");
        userToUpdate.setBirthDate(new Date());
        userToUpdate.setRole("ADMIN");
        userToUpdate.setEnabled(1);
        userToUpdate.setPassword("newpass");
        userToUpdate.setPaidService(true);
        userToUpdate.setExpirationDate(new Date(System.currentTimeMillis() + 86400000)); // Expires tomorrow
        userToUpdate.setPolicy(mockPolicy);
        
        User existingUser = new User(); // User fetched from DB
        existingUser.setIdUser(userId);
        
        when(mockSession.get(User.class, userId)).thenReturn(existingUser);

        // Act
        User result = userDAO.update(userToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getIdUser());
        assertEquals(userToUpdate.getName(), result.getName());
        assertEquals(userToUpdate.getEmail(), result.getEmail());
        assertEquals(userToUpdate.getPaidService(), result.getPaidService());
        assertEquals(userToUpdate.getExpirationDate(), result.getExpirationDate()); // checkServiceExpiration shouldn't modify it here
        assertEquals(mockPolicy, result.getPolicy());
        
        verify(mockSession).get(User.class, userId);
        verify(mockSession).update(existingUser); // Ensure it updates the fetched entity
        verify(mockTransaction).commit();
    }
    
    @Test
    void update_ClearExpirationDateWhenNotPaid() {
        // Arrange
        Long userId = 1L;
        User userToUpdate = new User();
        userToUpdate.setIdUser(userId);
        userToUpdate.setPaidService(false); // Service not paid
        userToUpdate.setExpirationDate(new Date()); // Has an expiration date set (should be cleared)
        userToUpdate.setPolicy(null);

        User existingUser = new User();
        existingUser.setIdUser(userId);
        existingUser.setPaidService(true); // Was previously paid
        existingUser.setExpirationDate(new Date()); // Had expiration
        
        when(mockSession.get(User.class, userId)).thenReturn(existingUser);

        // Act
        User result = userDAO.update(userToUpdate);

        // Assert
        assertNotNull(result);
        assertFalse(result.getPaidService());
        assertNull(result.getExpirationDate()); // Should be cleared
        assertNull(result.getPolicy());
        verify(mockSession).update(existingUser);
        verify(mockTransaction).commit();
    }
    
     @Test
    void update_ServiceExpiredCheck() {
        // Arrange
        Long userId = 1L;
        User userToUpdate = new User(); // Input data for update
        userToUpdate.setIdUser(userId);
        userToUpdate.setPaidService(true); // Still marked as paid in input
        userToUpdate.setExpirationDate(new Date(System.currentTimeMillis() - 86400000)); // Expired yesterday
        userToUpdate.setPolicy(mockPolicy);

        User existingUser = new User(); // Data from DB before update
        existingUser.setIdUser(userId);
        existingUser.setPaidService(true);
        existingUser.setExpirationDate(new Date(System.currentTimeMillis() - 86400000)); // Expired yesterday
        existingUser.setPolicy(mockPolicy);
        
        when(mockSession.get(User.class, userId)).thenReturn(existingUser);

        // Act
        User result = userDAO.update(userToUpdate);

        // Assert
        assertNotNull(result);
        // checkServiceExpiration should run during update and set paidService to false
        assertFalse(result.getPaidService(), "Service should be marked as not paid due to expiration"); 
        assertNull(result.getPolicy(), "Policy should be removed when service expires");
        assertEquals(userToUpdate.getExpirationDate(), result.getExpirationDate()); // Date itself isn't cleared, just flag
        
        verify(mockSession).update(existingUser);
        verify(mockTransaction).commit();
    }
    
    @Test
    void update_SetPolicyNullWhenNotPaid() {
        // Arrange
        Long userId = 1L;
        User userToUpdate = new User(); 
        userToUpdate.setIdUser(userId);
        userToUpdate.setPaidService(false); // Service explicitly set to not paid
        userToUpdate.setPolicy(mockPolicy); // Policy provided in input (should be ignored)

        User existingUser = new User(); 
        existingUser.setIdUser(userId);
        existingUser.setPaidService(true);
        existingUser.setPolicy(mockPolicy); // Had a policy before
        
        when(mockSession.get(User.class, userId)).thenReturn(existingUser);

        // Act
        User result = userDAO.update(userToUpdate);

        // Assert
        assertNotNull(result);
        assertFalse(result.getPaidService()); 
        assertNull(result.getPolicy(), "Policy should be set to null when service is not paid");
        
        verify(mockSession).update(existingUser);
        verify(mockTransaction).commit();
    }

    @Test
    void update_UserNotFound() {
        // Arrange
        Long userId = 1L;
        User userToUpdate = new User();
        userToUpdate.setIdUser(userId);
        when(mockSession.get(User.class, userId)).thenReturn(null);

        // Act
        User result = userDAO.update(userToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).get(User.class, userId);
        verify(mockSession, never()).update(any(User.class));
        verify(mockTransaction, never()).commit();
    }

    @Test
    void update_Exception() {
        // Arrange
        Long userId = 1L;
        User userToUpdate = new User();
        userToUpdate.setIdUser(userId);

        // Mock findById to return a user, then update to fail
        User existingUser = new User(); 
        existingUser.setIdUser(userId);
        when(mockSession.get(User.class, userId)).thenReturn(existingUser);
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(User.class));

        // Act
        User result = userDAO.update(userToUpdate);

        // Assert
        assertNull(result);
        verify(mockSession).get(User.class, userId);
        verify(mockSession).update(existingUser);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void checkAllUsersServiceExpiration_Success() {
        // Arrange
        Date yesterday = new Date(System.currentTimeMillis() - 86400000);
        User expiredUser1 = new User();
        expiredUser1.setIdUser(1L);
        expiredUser1.setPaidService(true);
        expiredUser1.setExpirationDate(yesterday);
        expiredUser1.setPolicy(mockPolicy); // Has policy
        
        User expiredUser2 = new User();
        expiredUser2.setIdUser(2L);
        expiredUser2.setPaidService(true);
        expiredUser2.setExpirationDate(yesterday);
        expiredUser2.setPolicy(mockPolicy); // Has policy
        
        List<User> expiredList = List.of(expiredUser1, expiredUser2);
        
        when(mockSession.createQuery(anyString(), eq(User.class))).thenReturn(mockUserQuery);
        when(mockUserQuery.setParameter(eq("today"), any(Date.class))).thenReturn(mockUserQuery);
        when(mockUserQuery.getResultList()).thenReturn(expiredList);

        // Act
        int updatedCount = userDAO.checkAllUsersServiceExpiration();

        // Assert
        assertEquals(2, updatedCount);
        verify(mockSession).createQuery("FROM User WHERE paidService = true AND expirationDate < :today", User.class);
        verify(mockUserQuery).setParameter(eq("today"), any(Date.class));
        verify(mockSession, times(2)).update(any(User.class));
        verify(mockSession).update(expiredUser1);
        verify(mockSession).update(expiredUser2);
        assertFalse(expiredUser1.getPaidService()); // Should be updated
        assertNull(expiredUser1.getPolicy()); // Policy should be removed
        assertFalse(expiredUser2.getPaidService()); // Should be updated
        assertNull(expiredUser2.getPolicy()); // Policy should be removed
        verify(mockTransaction).commit();
    }
    
     @Test
    void checkAllUsersServiceExpiration_NoExpiredUsers() {
        // Arrange
        List<User> emptyList = Collections.emptyList();
        
        when(mockSession.createQuery(anyString(), eq(User.class))).thenReturn(mockUserQuery);
        when(mockUserQuery.setParameter(eq("today"), any(Date.class))).thenReturn(mockUserQuery);
        when(mockUserQuery.getResultList()).thenReturn(emptyList);

        // Act
        int updatedCount = userDAO.checkAllUsersServiceExpiration();

        // Assert
        assertEquals(0, updatedCount);
        verify(mockSession).createQuery("FROM User WHERE paidService = true AND expirationDate < :today", User.class);
        verify(mockUserQuery).setParameter(eq("today"), any(Date.class));
        verify(mockSession, never()).update(any(User.class));
        verify(mockTransaction).commit();
    }
    
    @Test
    void checkAllUsersServiceExpiration_ExceptionDuringQuery() {
        // Arrange
        when(mockSession.createQuery(anyString(), eq(User.class))).thenThrow(new RuntimeException("DB Query Error"));

        // Act
        userDAO.checkAllUsersServiceExpiration();

        // Assert
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }
    
    @Test
    void checkAllUsersServiceExpiration_ExceptionDuringUpdate() {
        // Arrange
        User expiredUser = new User();
        expiredUser.setIdUser(1L);
        expiredUser.setPaidService(true);
        expiredUser.setExpirationDate(new Date(System.currentTimeMillis() - 100000)); // Expired
        expiredUser.setPolicy(mockPolicy);
        
        List<User> userList = Collections.singletonList(expiredUser);
        when(mockSession.createQuery(anyString(), eq(User.class))).thenReturn(mockUserQuery);
        when(mockUserQuery.getResultList()).thenReturn(userList);
        
        // Throw exception during the update call within the loop
        doThrow(new RuntimeException("DB Update Error")).when(mockSession).update(any(User.class));

        // Act
        userDAO.checkAllUsersServiceExpiration();

        // Assert
        verify(mockSession).update(expiredUser);
        verify(mockTransaction).rollback();
        verify(mockTransaction, never()).commit();
    }

    @Test
    void findByEmail_Found() {
        // Arrange
        String email = "findme@example.com";
        User expectedUser = new User();
        when(mockSession.createQuery(anyString(), eq(User.class))).thenReturn(mockUserQuery);
        when(mockUserQuery.setParameter(eq("email"), eq(email))).thenReturn(mockUserQuery);
        when(mockUserQuery.uniqueResult()).thenReturn(expectedUser);

        // Act
        User result = userDAO.findByEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUser, result);
        verify(mockSession).createQuery("FROM User WHERE email = :email", User.class);
        verify(mockUserQuery).setParameter("email", email);
    }

    @Test
    void findByEmail_NotFound() {
       // Arrange
        String email = "notfound@example.com";
        when(mockSession.createQuery(anyString(), eq(User.class))).thenReturn(mockUserQuery);
        when(mockUserQuery.setParameter(eq("email"), eq(email))).thenReturn(mockUserQuery);
        when(mockUserQuery.uniqueResult()).thenReturn(null);

        // Act
        User result = userDAO.findByEmail(email);

        // Assert
        assertNull(result);
    }
    
    @Test
    void findByEmail_Exception() {
       // Arrange
        String email = "exception@example.com";
        when(mockSession.createQuery(anyString(), eq(User.class))).thenThrow(new RuntimeException("DB error"));

        // Act
        User result = userDAO.findByEmail(email);

        // Assert
        assertNull(result);
    }
} 