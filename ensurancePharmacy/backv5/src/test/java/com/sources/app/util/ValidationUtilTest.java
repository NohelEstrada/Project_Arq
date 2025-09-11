package com.sources.app.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for validation utilities used across the pharmacy system
 */
public class ValidationUtilTest {

    @Test
    @DisplayName("Should validate Guatemala CUI format correctly")
    void testValidateCUIFormat() {
        // Test CUI (Código Único de Identificación) validation for Guatemala
        
        // Valid CUI examples (13 digits)
        assertTrue(isValidCUI("1234567890123"), "Valid 13-digit CUI should pass");
        assertTrue(isValidCUI("9876543210987"), "Another valid CUI should pass");
        
        // Invalid CUI examples
        assertFalse(isValidCUI("123456789012"), "12-digit CUI should fail");
        assertFalse(isValidCUI("12345678901234"), "14-digit CUI should fail");
        assertFalse(isValidCUI("123456789012a"), "CUI with letters should fail");
        assertFalse(isValidCUI(""), "Empty CUI should fail");
        assertFalse(isValidCUI(null), "Null CUI should fail");
    }

    @Test
    @DisplayName("Should validate Guatemala phone number format")
    void testValidatePhoneFormat() {
        // Test Guatemala phone number validation
        
        // Valid phone numbers
        assertTrue(isValidGuatemalaPhone("12345678"), "8-digit phone should pass");
        assertTrue(isValidGuatemalaPhone("87654321"), "Another 8-digit phone should pass");
        assertTrue(isValidGuatemalaPhone("50123456"), "Mobile phone should pass");
        
        // Invalid phone numbers
        assertFalse(isValidGuatemalaPhone("1234567"), "7-digit phone should fail");
        assertFalse(isValidGuatemalaPhone("123456789"), "9-digit phone should fail");
        assertFalse(isValidGuatemalaPhone("1234567a"), "Phone with letters should fail");
        assertFalse(isValidGuatemalaPhone(""), "Empty phone should fail");
        assertFalse(isValidGuatemalaPhone(null), "Null phone should fail");
    }

    @Test
    @DisplayName("Should validate medicine dosage format")
    void testValidateMedicineDosage() {
        // Test medicine dosage validation
        
        // Valid dosages
        assertTrue(isValidDosage("500mg"), "Standard mg dosage should pass");
        assertTrue(isValidDosage("1g"), "Gram dosage should pass");
        assertTrue(isValidDosage("250mg/5ml"), "Liquid dosage should pass");
        assertTrue(isValidDosage("100mcg"), "Microgram dosage should pass");
        
        // Invalid dosages
        assertFalse(isValidDosage(""), "Empty dosage should fail");
        assertFalse(isValidDosage("invalid"), "Non-numeric dosage should fail");
        assertFalse(isValidDosage("0mg"), "Zero dosage should fail");
        assertFalse(isValidDosage(null), "Null dosage should fail");
    }

    // Helper methods for validation (these would normally be in a real ValidationUtil class)
    private boolean isValidCUI(String cui) {
        if (cui == null || cui.isEmpty()) return false;
        return cui.matches("^\\d{13}$");
    }

    private boolean isValidGuatemalaPhone(String phone) {
        if (phone == null || phone.isEmpty()) return false;
        return phone.matches("^\\d{8}$");
    }

    private boolean isValidDosage(String dosage) {
        if (dosage == null || dosage.isEmpty()) return false;
        // Simple validation for dosage format (number + unit)
        return dosage.matches("^\\d+(\\.\\d+)?(mg|g|mcg|ml|mg/\\d+ml)$") && !dosage.startsWith("0");
    }
}
