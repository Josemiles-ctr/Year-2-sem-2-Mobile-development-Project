# Dependency Vulnerability Remediation Backlog

**Status:** Critical vulnerabilities suppressed, awaiting updates  
**Priority:** HIGH  
**Timeline:** Next 2 sprints  

---

## Summary

**209 vulnerabilities** found in dependency-check with **6 critical CVEs** (CVSS > 7.0).  
Temporary suppressions added to `app/dependency-check-suppressions.xml`.  
**Real fixes below:** Update dependencies to eliminate vulnerabilities entirely.

---

## DEPS-001: Update Netty to 4.1.112+ (CRITICAL)

**Priority:** CRITICAL  
**Estimated SP:** 3  

Netty 4.1.93 and 4.1.110 have multiple CVEs:
- CVE-2025-55163, CVE-2025-24970, CVE-2025-58057, CVE-2025-58056
- CVE-2023-44487 (HTTP/2 protocol)
- CVE-2023-34462, CVE-2024-47535, CVE-2024-29025

**Action:**
- Update all netty packages to 4.1.112 or latest stable
- Run tests to verify no breaking changes
- Remove suppression entries for Netty CVEs

**Location:** `gradle/libs.versions.toml` (netty version)

---

## DEPS-002: Update gRPC to 1.68.0+ (CRITICAL)

**Priority:** CRITICAL  
**Estimated SP:** 3  

gRPC 1.57.2 vulnerable to:
- CVE-2023-44487 (HTTP/2 stream multiplexing DoS)

**Action:**
- Update all grpc packages from 1.57.2 to 1.68.0+
- Verify API compatibility
- Run integration tests
- Remove gRPC CVE-2023-44487 suppression

**Location:** `gradle/libs.versions.toml` (grpc version)

---

## DEPS-003: Update Protobuf to 3.25.0+ (CRITICAL)

**Priority:** CRITICAL  
**Estimated SP:** 2  

Protobuf 3.22.3 and 3.24.4 vulnerable to:
- CVE-2024-7254 (JSON deserialization parsing issue)

**Action:**
- Update protobuf packages to 3.25.0+
- Check for any breaking changes in JSON serialization
- Run tests
- Remove protobuf CVE-2024-7254 suppression

**Location:** `gradle/libs.versions.toml` (protobuf-java version)

---

## DEPS-004: Update Kotlin to 2.0.21 (MEDIUM)

**Priority:** MEDIUM  
**Estimated SP:** 5  

Current Kotlin versions (1.6.10, 1.8.x, 1.9.0) vulnerable to:
- CVE-2020-29582 (reflection utility issue)

Also: libraries enforcing outdated Kotlin stdlib versions.

**Action:**
- Bump kotlin to 2.0.21 (already referenced in `libs.versions.toml`)
- Update all kotlin-* dependencies
- Recompile all modules
- Run full test suite
- Remove Kotlin CVE-2020-29582 suppression

**Location:** `gradle/libs.versions.toml`, `app/build.gradle.kts`

---

## DEPS-005: Update Commons Lang to 3.17.0+ (LOW)

**Priority:** LOW  
**Estimated SP:** 1  

Commons Lang 3.16.0 vulnerable to:
- CVE-2025-48924

**Action:**
- Update commons-lang3 to 3.17.0+
- Run tests
- Remove suppression

---

## DEPS-006: Replace HttpClient 4.5.6 with OkHttp3 (MEDIUM)

**Priority:** MEDIUM  
**Estimated SP:** 8  
**Future Sprint:** Sprint 8+

HttpClient 4.5.6 has:
- CVE-2020-13956 (GZIP body handling)
- Generally end-of-life status

**Action:** 
- Identify all HttpClient 4.5.6 usages
- Migrate to OkHttp3 (modern, maintained Android library)
- Update all API calls
- Run integration tests

**Note:** Low immediate urgency (mitigated by HTTPS/TLS in this context), but replacement recommended for long-term maintenance.

---

## Testing Plan

After each dependency update:

1. **Run dependency-check again:**
   ```bash
   ./gradlew :app:dependencyCheckAnalyze
   ```

2. **Run unit tests:**
   ```bash
   ./gradlew :app:testDebugUnitTest
   ```

3. **Run integration tests:**
   ```bash
   ./gradlew :app:connectedAndroidTest
   ```

4. **Build release APK:**
   ```bash
   ./gradlew :app:assembleRelease
   ```

5. **Verify no regressions** with manual testing

---

## Current State

**Suppressions file:** `app/dependency-check-suppressions.xml`  
**Total suppressed:** 209 vulnerabilities  
**Target of 0 suppressions** within 2 sprints  

---

## Priority Sequence

1. **DEPS-001** (Netty) - Fixes 10+ CVEs
2. **DEPS-002** (gRPC) - Fixes HTTP/2 vulnerability
3. **DEPS-003** (Protobuf) - Fixes JSON parsing
4. **DEPS-004** (Kotlin) - Upgrades language
5. **DEPS-005** (Commons Lang) - Minor update
6. **DEPS-006** (HttpClient → OkHttp) - Future refactoring

---

**Next Action:** Schedule DEPS-001 through DEPS-005 in next sprint.
