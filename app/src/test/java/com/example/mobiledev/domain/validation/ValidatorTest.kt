package com.example.mobiledev.domain.validation

import org.junit.Assert.*
import org.junit.Test

class ValidatorTest {

    // ── isValidEmail ──────────────────────────────────────────────────────────

    @Test
    fun `valid standard email returns true`() {
        assertTrue(Validator.isValidEmail("user@example.com"))
    }

    @Test
    fun `valid email with subdomain returns true`() {
        assertTrue(Validator.isValidEmail("user@mail.example.co.uk"))
    }

    @Test
    fun `valid email with plus alias returns true`() {
        assertTrue(Validator.isValidEmail("user+tag@example.com"))
    }

    @Test
    fun `email missing at-sign returns false`() {
        assertFalse(Validator.isValidEmail("userexample.com"))
    }

    @Test
    fun `email missing domain returns false`() {
        assertFalse(Validator.isValidEmail("user@"))
    }

    @Test
    fun `email missing local part returns false`() {
        assertFalse(Validator.isValidEmail("@example.com"))
    }

    @Test
    fun `blank email returns false`() {
        assertFalse(Validator.isValidEmail(""))
    }

    @Test
    fun `whitespace-only email returns false`() {
        assertFalse(Validator.isValidEmail("   "))
    }

    @Test
    fun `email with spaces inside returns false`() {
        assertFalse(Validator.isValidEmail("user name@example.com"))
    }

    // ── isValidName ───────────────────────────────────────────────────────────

    @Test
    fun `name with two characters returns true`() {
        assertTrue(Validator.isValidName("Al"))
    }

    @Test
    fun `name with more than two characters returns true`() {
        assertTrue(Validator.isValidName("Alice"))
    }

    @Test
    fun `single character name returns false`() {
        assertFalse(Validator.isValidName("A"))
    }

    @Test
    fun `blank name returns false`() {
        assertFalse(Validator.isValidName(""))
    }

    @Test
    fun `whitespace-only name returns false`() {
        assertFalse(Validator.isValidName("   "))
    }

    @Test
    fun `name with surrounding whitespace is trimmed and valid`() {
        assertTrue(Validator.isValidName("  Alice  "))
    }

    // -- isValidPhone ----------------------------------------------------------

    @Test
    fun `phone with ten digits returns true`() {
        assertTrue(Validator.isValidPhone("0712345678"))
    }

    @Test
    fun `phone with formatting chars still validates on digits`() {
        assertTrue(Validator.isValidPhone("+94 71-234-5678"))
    }

    @Test
    fun `phone with fewer than ten digits returns false`() {
        assertFalse(Validator.isValidPhone("12345"))
    }

    // -- isValidPassword -------------------------------------------------------

    @Test
    fun `password with upper lower and number returns true`() {
        assertTrue(Validator.isValidPassword("Secure123"))
    }

    @Test
    fun `password missing uppercase returns false`() {
        assertFalse(Validator.isValidPassword("secure123"))
    }

    @Test
    fun `password mismatch returns false`() {
        assertFalse(Validator.passwordsMatch("Secure123", "Secure124"))
    }
}

