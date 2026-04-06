# Firebase Realtime Database Integration

## Overview

The app uses Firebase Realtime Database as the data source for user accounts and authentication checks.
The repository layer is responsible for reading and writing user records.

## Repository Layer

The main data access class is:

- `FirebaseUserRepository.kt`

It provides the following operations:

- fetch users,
- add a user,
- authenticate a user,
- remove a user.

## Current Data Approach

### User creation
When a user is added, the repository stores:

- id
- name
- email
- phone
- password
- normalized lookup keys for email and phone

### Authentication
Authentication uses indexed Realtime Database queries instead of downloading the full user list.
This improves performance and avoids exposing the entire dataset to the client during sign-in.

## Security Notes

- The project now documents the current Firebase-related risk surface clearly.
- API keys and `google-services.json` were treated as sensitive and removed from tracked history.
- The current architecture still uses password checks in the repository layer; moving authentication fully to Firebase Auth would be a stronger long-term security design.

## Related Files

- `app/src/main/java/com/example/mobiledev/data/repository/FirebaseUserRepository.kt`
- `app/src/main/java/com/example/mobiledev/data/repository/UserRepository.kt`
- `app/src/main/java/com/example/mobiledev/data/model/User.kt`
- `app/src/main/java/com/example/mobiledev/domain/validation/Validator.kt`
- `app/src/main/res/xml` (security and backup related app config)
- `app/dependency-check-suppressions.xml`

