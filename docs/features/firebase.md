# Firebase Realtime Database Integration

## Overview

The app uses Firebase Realtime Database as the source of truth for users, hospitals,
ambulances, emergency requests, and dashboard metrics.

## Repository Layer

Main data access classes:

- `FirebaseUserRepository.kt`
- `FirebaseResQRepository.kt`
- `LocalEmergencyRepository.kt` (maps persisted entities into emergency dashboard models)

Repositories provide operations for:

- fetching and mutating users,
- hospital and ambulance reads/writes,
- emergency request lifecycle updates,
- sign-in checks for patient and hospital admin flows.

## Seed Workflow

Use the seed tool to populate development Firebase projects with the old mock data:

- `tools/firebase-seed/`

It writes:

- `users`
- `hospitals`
- `ambulances`
- `emergencyRequests`
- `dashboardMetrics`

All seeded development users share one password configured through `DEV_USER_PASSWORD`
(default `password123`).

## Current Data Approach

### User creation
When a user is added, the repository stores:

- id
- name
- email
- phone
- password
- normalized lookup keys for email and phone
- role/userType profile fields used by RBAC-aware repositories

### Authentication
Authentication validates against stored password hashes in Realtime Database for development flows.

## Security Notes

- The project now documents the current Firebase-related risk surface clearly.
- API keys and `google-services.json` were treated as sensitive and removed from tracked history.
- The current architecture still uses repository-side password checks; migrating to Firebase Auth remains the recommended long-term approach.

## Related Files

- `app/src/main/java/com/example/mobiledev/data/repository/FirebaseUserRepository.kt`
- `app/src/main/java/com/example/mobiledev/data/repository/FirebaseResQRepository.kt`
- `app/src/main/java/com/example/mobiledev/data/repository/UserRepository.kt`
- `app/src/main/java/com/example/mobiledev/data/repository/ResQRepository.kt`
- `app/src/main/java/com/example/mobiledev/data/model/User.kt`
- `tools/firebase-seed/README.md`
- `app/src/main/java/com/example/mobiledev/domain/validation/Validator.kt`
- `app/src/main/res/xml` (security and backup related app config)
- `app/dependency-check-suppressions.xml`

