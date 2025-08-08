package com.sources.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.Date;

class TransactionsTest {

    private Transactions transaction;
    private final Long id = 1L;
    private final User user = mock(User.class);
    private final Hospital hospital = mock(Hospital.class);
    private final Date transDate = new Date();
    private final Double total = 250.0;
    private final Double copay = 25.0;
    private final String comment = "Standard procedure";
    private final String result = "Completed Successfully";
    private final Integer covered = 1;
    private final String auth = "AUTH-456";

    @BeforeEach
    void setUp() {
        transaction = new Transactions();
        // Set fields manually
        transaction.setIdTransaction(id);
        transaction.setUser(user);
        transaction.setHospital(hospital);
        transaction.setTransDate(transDate);
        transaction.setTotal(total);
        transaction.setCopay(copay);
        transaction.setTransactionComment(comment);
        transaction.setResult(result);
        transaction.setCovered(covered);
        transaction.setAuth(auth);
    }

    @Test
    void noArgsConstructor_InitializesCorrectly() {
        Transactions newTrans = new Transactions();
        assertNull(newTrans.getIdTransaction());
        assertNull(newTrans.getUser());
        assertNull(newTrans.getHospital());
        assertNull(newTrans.getTransDate());
        assertNull(newTrans.getTotal());
        assertNull(newTrans.getCopay());
        assertNull(newTrans.getTransactionComment());
        assertNull(newTrans.getResult());
        assertNull(newTrans.getCovered());
        assertNull(newTrans.getAuth());
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        // Assertions based on setUp values
        assertEquals(id, transaction.getIdTransaction());
        assertEquals(user, transaction.getUser());
        assertEquals(hospital, transaction.getHospital());
        assertEquals(transDate, transaction.getTransDate());
        assertEquals(total, transaction.getTotal());
        assertEquals(copay, transaction.getCopay());
        assertEquals(comment, transaction.getTransactionComment());
        assertEquals(result, transaction.getResult());
        assertEquals(covered, transaction.getCovered());
        assertEquals(auth, transaction.getAuth());

        // Test setters with new values
        Transactions testTrans = new Transactions();
        User newUser = mock(User.class);
        Hospital newHospital = mock(Hospital.class);
        Date newDate = new Date(System.currentTimeMillis() + 12345);

        testTrans.setIdTransaction(2L);
        assertEquals(2L, testTrans.getIdTransaction());

        testTrans.setUser(newUser);
        assertEquals(newUser, testTrans.getUser());

        testTrans.setHospital(newHospital);
        assertEquals(newHospital, testTrans.getHospital());

        testTrans.setTransDate(newDate);
        assertEquals(newDate, testTrans.getTransDate());

        testTrans.setTotal(300.0);
        assertEquals(300.0, testTrans.getTotal());

        testTrans.setCopay(30.0);
        assertEquals(30.0, testTrans.getCopay());

        testTrans.setTransactionComment("New comment here");
        assertEquals("New comment here", testTrans.getTransactionComment());

        testTrans.setResult("Pending Review");
        assertEquals("Pending Review", testTrans.getResult());

        testTrans.setCovered(0);
        assertEquals(0, testTrans.getCovered());

        testTrans.setAuth("NEW-AUTH-789");
        assertEquals("NEW-AUTH-789", testTrans.getAuth());
    }
    
    // Add equals/hashCode tests if implemented
} 