package com.example.mobiledev.data.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppRole {
    GUEST,
    PATIENT,
    DRIVER,
    HOSPITAL_ADMIN,
    SYSTEM_ADMIN
}

enum class Permission {
    VIEW_SYSTEM_DATA,
    VIEW_HOSPITAL_DATA,
    VIEW_APPROVED_HOSPITALS,
    VIEW_AMBULANCES,
    VIEW_OWN_REQUESTS,
    CREATE_REQUEST,
    MANAGE_REQUESTS,
    MANAGE_AMBULANCES,
    MANAGE_USERS
}

data class AuthPrincipal(
    val userId: String? = null,
    val hospitalId: String? = null,
    val role: AppRole = AppRole.GUEST
)

class AuthSessionManager {
    private val _principal = MutableStateFlow(AuthPrincipal())
    val principal: StateFlow<AuthPrincipal> = _principal.asStateFlow()

    val currentPrincipal: AuthPrincipal
        get() = _principal.value

    fun setPrincipal(principal: AuthPrincipal) {
        _principal.value = principal
    }

    fun clear() {
        _principal.value = AuthPrincipal()
    }
}

object RbacPolicy {
    private val grants: Map<AppRole, Set<Permission>> = mapOf(
        AppRole.GUEST to emptySet(),
        AppRole.PATIENT to setOf(
            Permission.CREATE_REQUEST,
            Permission.VIEW_OWN_REQUESTS,
            Permission.VIEW_APPROVED_HOSPITALS,
            Permission.VIEW_AMBULANCES
        ),
        AppRole.DRIVER to setOf(
            Permission.VIEW_HOSPITAL_DATA,
            Permission.VIEW_APPROVED_HOSPITALS,
            Permission.VIEW_AMBULANCES,
            Permission.MANAGE_REQUESTS
        ),
        AppRole.HOSPITAL_ADMIN to setOf(
            Permission.VIEW_HOSPITAL_DATA,
            Permission.VIEW_APPROVED_HOSPITALS,
            Permission.VIEW_AMBULANCES,
            Permission.CREATE_REQUEST,
            Permission.MANAGE_REQUESTS,
            Permission.MANAGE_AMBULANCES
        ),
        AppRole.SYSTEM_ADMIN to Permission.entries.toSet()
    )

    fun hasPermission(role: AppRole, permission: Permission): Boolean {
        return grants[role]?.contains(permission) == true
    }

    fun requirePermission(principal: AuthPrincipal, permission: Permission) {
        if (!hasPermission(principal.role, permission)) {
            throw SecurityException("Access denied: ${principal.role} cannot $permission")
        }
    }

    fun requireHospitalScope(principal: AuthPrincipal, hospitalId: String) {
        if (principal.role == AppRole.SYSTEM_ADMIN) return
        if (principal.hospitalId != hospitalId) {
            throw SecurityException("Access denied: cross-hospital access is not allowed")
        }
    }

    fun requireUserScope(principal: AuthPrincipal, userId: String) {
        if (principal.role == AppRole.SYSTEM_ADMIN) return
        if (principal.userId != userId) {
            throw SecurityException("Access denied: cross-user access is not allowed")
        }
    }
}