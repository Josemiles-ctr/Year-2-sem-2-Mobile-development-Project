# Firebase Seed Tool

Seeds Firebase Realtime Database with the app's former mock datasets and development users.

## What It Seeds

- `users` (includes PATIENT, DRIVER, HOSPITAL_ADMIN, SYSTEM_ADMIN)
- `hospitals`
- `ambulances`
- `emergencyRequests`
- `dashboardMetrics` (from activity mocks)

All seeded users receive the same development password.

## Prerequisites

1. Node.js 18+
2. Firebase service account credentials available as application default credentials.
3. `FIREBASE_DATABASE_URL` set to your target Realtime Database URL.

## Install

```sh
cd tools/firebase-seed
npm install
```

## Dry Run

```sh
cd tools/firebase-seed
FIREBASE_DATABASE_URL="https://<your-project>.firebaseio.com" npm run seed:dry
```

## Seed Database

```sh
cd tools/firebase-seed
FIREBASE_DATABASE_URL="https://<your-project>.firebaseio.com" DEV_USER_PASSWORD="password123" npm run seed
```

## Notes

- If a user is created without a role in the seed definitions, it defaults to `PATIENT`.
- This script is intended for development environments only.

