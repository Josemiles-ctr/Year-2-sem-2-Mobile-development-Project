package com.example.mobiledev.data.model

import org.junit.Assert.*
import org.junit.Test

class UserTest {

    @Test
    fun `user holds correct field values`() {
        val user = User(id = 1, name = "Alice", email = "alice@example.com", phone = "0712345678")
        assertEquals(1, user.id)
        assertEquals("Alice", user.name)
        assertEquals("alice@example.com", user.email)
        assertEquals("0712345678", user.phone)
    }

    @Test
    fun `users with identical fields are equal`() {
        val u1 = User(1, "Alice", "alice@example.com", "0712345678")
        val u2 = User(1, "Alice", "alice@example.com", "0712345678")
        assertEquals(u1, u2)
    }

    @Test
    fun `users with different ids are not equal`() {
        val u1 = User(1, "Alice", "alice@example.com", "0712345678")
        val u2 = User(2, "Alice", "alice@example.com", "0712345678")
        assertNotEquals(u1, u2)
    }

    @Test
    fun `copy produces independent instance with updated field`() {
        val original = User(1, "Alice", "alice@example.com", "0712345678")
        val copy = original.copy(name = "Bob")
        assertEquals("Bob", copy.name)
        assertEquals(original.id, copy.id)
        assertEquals(original.email, copy.email)
        assertEquals(original.phone, copy.phone)
    }

    @Test
    fun `toString contains all fields`() {
        val user = User(1, "Alice", "alice@example.com", "0712345678")
        val str = user.toString()
        assertTrue(str.contains("Alice"))
        assertTrue(str.contains("alice@example.com"))
        assertTrue(str.contains("0712345678"))
    }
}

