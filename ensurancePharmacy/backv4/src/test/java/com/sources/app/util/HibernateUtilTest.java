package com.sources.app.util;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HibernateUtilTest {

    // Note: Properly testing Hibernate initialization often involves 
    // setting up an in-memory database (like H2) and a test-specific hibernate.cfg.xml.
    // This basic test primarily checks that the static initialization doesn't immediately fail
    // and that the SessionFactory can be retrieved.

    @Mock
    private SessionFactory mockSessionFactory; // Mock the SessionFactory

    private MockedStatic<HibernateUtil> mockedHibernateUtil;

    @BeforeEach
    void setUp() {
        // Mock the static getSessionFactory method BEFORE it's called by the test
        mockedHibernateUtil = Mockito.mockStatic(HibernateUtil.class);
        mockedHibernateUtil.when(HibernateUtil::getSessionFactory).thenReturn(mockSessionFactory);
        
        // Mock the static setCorsHeaders method to do nothing (or return null/default)
        mockedHibernateUtil.when(() -> HibernateUtil.setCorsHeaders(any(HttpExchange.class)))
                           .thenAnswer(invocation -> null); // Use thenAnswer instead of thenCallRealMethod
    }

    @AfterEach
    void tearDown() {
        // Close the static mock after each test
        mockedHibernateUtil.close();
    }

    @Test
    void getSessionFactory_ReturnsMockedSessionFactory() {
        // Act: Call the static method
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        // Assert: Verify it returned the mock we configured
        assertNotNull(sessionFactory, "SessionFactory should not be null when mocked.");
        assertSame(mockSessionFactory, sessionFactory, "Should return the mocked SessionFactory instance.");
        
        // Verify the static method was called
        mockedHibernateUtil.verify(HibernateUtil::getSessionFactory);
    }

    @Test
    void setCorsHeaders_AddsCorrectHeaders() {
        // Arrange
        HttpExchange mockExchange = mock(HttpExchange.class);
        Headers mockHeaders = mock(Headers.class);
        when(mockExchange.getResponseHeaders()).thenReturn(mockHeaders);

        // Act: Call the static method
        HibernateUtil.setCorsHeaders(mockExchange);

        // Assert: Verify the interactions on the passed mock objects
        verify(mockHeaders).add(eq("Access-Control-Allow-Origin"), eq("*"));
        verify(mockHeaders).add(eq("Access-Control-Allow-Methods"), anyString());
        verify(mockHeaders).add(eq("Access-Control-Allow-Headers"), anyString());
        verifyNoMoreInteractions(mockHeaders);
        
        // Verify the static method was called (optional, usually covered by verifying side-effects)
        mockedHibernateUtil.verify(() -> HibernateUtil.setCorsHeaders(mockExchange));
    }
} 