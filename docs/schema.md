# Database Schema - Emergency Ambulance Request System

This document describes the data model for the Emergency Ambulance Request System, including all entities, their attributes, relationships, and multiplicities.

---

## Entity-Relationship Diagram

```mermaid
erDiagram
    USER ||--o{ EMERGENCY_REQUEST : submits
    USER ||--o{ HOSPITAL : manages
    USER ||--o{ AMBULANCE : drives
    HOSPITAL ||--o{ AMBULANCE : owns
    HOSPITAL ||--o{ EMERGENCY_REQUEST : receives
    AMBULANCE ||--o{ EMERGENCY_REQUEST : responds

    USER {
        UUID id PK
        string name
        string email UK
        string phone
        string location
        string user_type
        datetime created_at
        datetime updated_at
    }

    HOSPITAL {
        UUID id PK
        UUID admin_id FK
        string name UK
        string email UK
        string phone
        string location
        float latitude
        float longitude
        int active_ambulances
        datetime created_at
        datetime updated_at
    }

    AMBULANCE {
        UUID id PK
        UUID hospital_id FK
        UUID driver_id FK
        string registration_no UK
        string license_no UK
        string status
        float latitude
        float longitude
        datetime created_at
        datetime updated_at
    }

    EMERGENCY_REQUEST {
        UUID id PK
        UUID user_id FK
        UUID hospital_id FK
        UUID ambulance_id FK
        string status
        string description
        string location
        float latitude
        float longitude
        string priority
        int estimated_time_mins
        datetime created_at
        datetime updated_at
        datetime completed_at
        boolean is_deleted
    }
```

---

## Database Table Schema (Detailed View)

### USER Table
| Column          | Type     | Constraint | Description                     |
|-----------------|----------|------------|---------------------------------|
| **id**          | UUID     | PK         | Patient/Hospital Admin ID       |
| **hospital_id** | UUID     | FK         | References HOSPITAL             |
| name            | String   | NOT NULL   | Full name                       |
| email           | String   | UNIQUE     | Email address                   |
| phone           | String   | NOT NULL   | Contact number                  |
| location        | String   | NOT NULL   | Home/Hospital address           |
| user_type       | Enum     | NOT NULL   | PATIENT, HOSPITAL_ADMIN, DRIVER |
| uuid            | String   | UNIQUE     | Application-level identifier    |
| created_at      | DateTime | NOT NULL   | Account creation                |
| updated_at      | DateTime | NOT NULL   | Last update                     |

### HOSPITAL Table
| Column            | Type     | Constraint | Description                   |
|-------------------|----------|------------|-------------------------------|
| **id**            | UUID     | PK         | Hospital ID                   |
| **admin_id**      | UUID     | FK         | References USER (admin)       |
| name              | String   | UNIQUE     | Hospital name                 |
| email             | String   | UNIQUE     | Hospital email                |
| phone             | String   | NOT NULL   | Hospital contact              |
| location          | String   | NOT NULL   | Hospital address              |
| latitude          | Float    | NULLABLE   | GPS latitude                  |
| longitude         | Float    | NULLABLE   | GPS longitude                 |
| uuid              | String   | UNIQUE     | Application-level identifier  |
| active_ambulances | Integer  | DEFAULT 0  | Count of available ambulances |
| created_at        | DateTime | NOT NULL   | Registration date             |
| updated_at        | DateTime | NOT NULL   | Last update                   |

### AMBULANCE Table
| Column          | Type     | Constraint | Description                      |
|-----------------|----------|------------|----------------------------------|
| **id**          | UUID     | PK         | Ambulance ID                     |
| **hospital_id** | UUID     | FK         | References HOSPITAL              |
| **driver_id**   | UUID     | FK         | References USER (driver)         |
| registration_no | String   | UNIQUE     | Vehicle plate number             |
| license_no      | String   | UNIQUE     | Service license number           |
| status          | Enum     | NOT NULL   | AVAILABLE, ON_EMERGENCY, OFFLINE |
| latitude        | Float    | NOT NULL   | Current GPS latitude             |
| longitude       | Float    | NOT NULL   | Current GPS longitude            |
| created_at      | DateTime | NOT NULL   | Registration date                |
| updated_at      | DateTime | NOT NULL   | Last location update             |

### EMERGENCY_REQUEST Table
| Column | Type | Constraint | Description |

|--------|------|-----------|-------------|
| **id** | UUID | PK | Request ID |
| **user_id** | UUID | FK | References USER (patient) |
| **hospital_id** | UUID | FK | References HOSPITAL |
| **ambulance_id** | UUID | FK | References AMBULANCE (nullable) |
| status | Enum | NOT NULL | PENDING, ASSIGNED, ON_WAY, ARRIVED, COMPLETED |
| description | String | NOT NULL | Medical emergency details |
| location | String | NOT NULL | Emergency location address |
| latitude | Float | NULLABLE | Emergency GPS latitude |
| longitude | Float | NULLABLE | Emergency GPS longitude |
| priority | Enum | NOT NULL | LOW, MEDIUM, HIGH, CRITICAL |
| estimated_time_mins | Integer | NULLABLE | ETA in minutes |
| created_at | DateTime | NOT NULL | Request submission |
| updated_at | DateTime | NOT NULL | Last status update |
| completed_at | DateTime | NULLABLE | Completion time |
| is_deleted | Boolean | DEFAULT false | Soft delete flag |

---

## Relationships Visualization

```mermaid
graph LR
    U["USER<br/>(Patients, Admins & Drivers)"]
    H["HOSPITAL<br/>(Emergency Centers)"]
    A["AMBULANCE<br/>(Vehicles)"]
    R["EMERGENCY_REQUEST<br/>(Requests)"]
    
    U -->|"1 : Many"| R
    U -->|"1 : Many"| H
    U -->|"1 : Many"| A
    H -->|"1 : Many"| R
    H -->|"1 : Many"| A
    A -->|"1 : Many"| R
    
    style U fill:#bbdefb,stroke:#1976d2,stroke-width:2px,color:#000
    style H fill:#ffe0b2,stroke:#f57c00,stroke-width:2px,color:#000
    style A fill:#f8bbd0,stroke:#c2185b,stroke-width:2px,color:#000
    style R fill:#e1bee7,stroke:#7b1fa2,stroke-width:2px,color:#000
```

---

## Field Definitions and Scope

### Application-Level Identifiers
The `uuid` field is an **application-level identifier** (not the primary database key). It provides a secondary unique identifier for:
- External API integrations
- Data synchronization across systems
- Human-readable references

The actual database primary key is the `id` field (UUID data type).

---

## Detailed Entity Definitions

### 1. USER (Patients, Admins & Drivers)

**Purpose:** Represents all system users - patients who submit emergency requests, hospital admins who manage operations, and ambulance drivers who respond to emergencies.

**User Types:**
- `PATIENT` - End-users who submit emergency requests
- `HOSPITAL_ADMIN` - Hospital staff who manage ambulances and requests
- `DRIVER` - Ambulance drivers who operate vehicles and respond to emergencies

| Attribute | Type | Constraints | Description |

|-----------|------|-------------|-------------|
| `id` | UUID | PK | Unique identifier for the user |
| `name` | String | NOT NULL | Full name of the user |
| `email` | String | NOT NULL, UNIQUE | Email address for authentication |
| `phone` | String | NOT NULL | Contact phone number |
| `location` | String | NOT NULL | Home or residential address |
| `user_type` | Enum | NOT NULL | PATIENT, HOSPITAL_ADMIN, DRIVER |
| `uuid` | String | UNIQUE | Alternative unique identifier |
| `createdAt` | DateTime | NOT NULL | Account creation timestamp |
| `updatedAt` | DateTime | NOT NULL | Last update timestamp |

**Constraints:**
- Email must be unique across all users
- Phone number format validation (international)
- Location must not be empty
- User type must be one of the defined enum values

---

### 2. HOSPITAL

**Purpose:** Represents hospitals registered in the system that manage ambulances and respond to emergency requests.

| Attribute           | Type     | Constraints      | Description                               |
|---------------------|----------|------------------|-------------------------------------------|
| `id`                | UUID     | PK               | Unique identifier for the hospital        |
| `admin_id`          | UUID     | FK, NOT NULL     | References the USER hospital admin        |
| `name`              | String   | NOT NULL, UNIQUE | Official hospital name                    |
| `email`             | String   | NOT NULL, UNIQUE | Hospital contact email for authentication |
| `phone`             | String   | NOT NULL         | Hospital main phone number                |
| `location`          | String   | NOT NULL         | Full address of the hospital              |
| `latitude`          | Float    | NULLABLE         | GPS latitude coordinate                   |
| `longitude`         | Float    | NULLABLE         | GPS longitude coordinate                  |
| `uuid`              | String   | UNIQUE           | Alternative unique identifier             |
| `active_ambulances` | Integer  | DEFAULT 0        | Count of currently available ambulances   |
| `createdAt`         | DateTime | NOT NULL         | Hospital registration timestamp           |
| `updatedAt`         | DateTime | NOT NULL         | Last update timestamp                     |

**Constraints:**
- `admin_id` must reference an existing USER with `user_type = HOSPITAL_ADMIN`
- Hospital name must be unique
- Email must be unique across all hospitals
- If GPS coordinates provided, both latitude and longitude must be present
- Location must not be empty

---

### 3. AMBULANCE

**Purpose:** Represents ambulances managed by hospitals. Each ambulance is assigned to a driver and receives emergency requests while updating its availability status.

| Attribute | Type | Constraints | Description |

|-----------|------|-------------|-------------|
| `id` | UUID | PK | Unique identifier for the ambulance |
| `hospitalId` | UUID | FK, NOT NULL | References the managing HOSPITAL |
| `driverId` | UUID | FK, NOT NULL | References the DRIVER (USER with type=DRIVER) |
| `registrationNo` | String | NOT NULL, UNIQUE | Vehicle registration/license plate number |
| `licenseNo` | String | NOT NULL, UNIQUE | Ambulance service license number |
| `status` | Enum | NOT NULL | Current status of the ambulance |
| `latitude` | Float | NOT NULL | Current GPS latitude |
| `longitude` | Float | NOT NULL | Current GPS longitude |
| `createdAt` | DateTime | NOT NULL | Ambulance registration timestamp |
| `updatedAt` | DateTime | NOT NULL | Last update timestamp |

**Status Values:**
- `AVAILABLE` - Ambulance is ready to respond
- `ON_EMERGENCY` - Currently responding to a request
- `OFFLINE` - Not operational

**Constraints:**
- `hospitalId` must reference an existing HOSPITAL
- `driverId` must reference an existing USER with user_type = DRIVER
- Registration number and license number must be unique
- Status must be one of the defined enum values
- GPS coordinates must be valid decimal values

**Relationships:**
- `Many : 1` with HOSPITAL (Multiple ambulances managed by one hospital)
- `Many : 1` with USER (Multiple ambulances can be assigned to different drivers)
- `1 : Many` with EMERGENCY_REQUEST (One ambulance can respond to multiple requests over time, but only one active request)

---

### 4. EMERGENCY_REQUEST

**Purpose:** Represents emergency requests submitted by users and tracked through to completion.

| Attribute | Type | Constraints | Description |

|-----------|------|-------------|-------------|
| `id` | UUID | PK | Unique identifier for the request |
| `userId` | UUID | FK, NOT NULL | References the USER who submitted the request |
| `hospitalId` | UUID | FK, NOT NULL | References the HOSPITAL receiving the request |
| `ambulanceId` | UUID | FK, NULLABLE | References the AMBULANCE assigned (NULL until assigned) |
| `status` | Enum | NOT NULL | Current status of the request |
| `description` | String | NOT NULL | Medical emergency description |
| `location` | String | NOT NULL | Location of the emergency |
| `latitude` | Float | NULLABLE | GPS latitude of emergency location |
| `longitude` | Float | NULLABLE | GPS longitude of emergency location |
| `priority` | Enum | NOT NULL | Severity level of the emergency |
| `createdAt` | DateTime | NOT NULL | Request submission timestamp |
| `updatedAt` | DateTime | NOT NULL | Last status update timestamp |
| `completedAt` | DateTime | NULLABLE | Request completion timestamp (NULL if incomplete) |

**Status Progression:**
- `PENDING` - Submitted but not yet assigned to an ambulance
- `ASSIGNED` - Ambulance has been assigned
- `ON_WAY` - Ambulance is en route to patient location
- `ARRIVED` - Ambulance has arrived at emergency location
- `COMPLETED` - Emergency response completed

**Priority Levels:**
- `LOW` - Non-urgent (triage, minor injuries)
- `MEDIUM` - Moderate urgency (chest pain, moderate injuries)
- `HIGH` - Severe/urgent (severe bleeding, difficulty breathing)
- `CRITICAL` - Life-threatening (cardiac arrest, severe trauma)

**Constraints:**
- `userId` must reference an existing USER
- `hospitalId` must reference an existing HOSPITAL
- `ambulanceId` can be NULL (during PENDING status)
- Status must follow the defined progression
- Priority must be one of the defined enum values
- GPS coordinates must be valid if provided
- `completedAt` should only be set when status is COMPLETED

**Relationships:**
- `Many : 1` with USER (A user can submit multiple requests)
- `Many : 1` with HOSPITAL (A hospital receives multiple requests)
- `Many : 1` with AMBULANCE (An ambulance responds to multiple requests over time)

---

---

## Data Flow / Process Timeline

### Emergency Request Lifecycle

```mermaid
graph TD
    A["1. USER submits<br/>EMERGENCY_REQUEST"] --> B["2. Request created<br/>status = PENDING<br/>ambulanceId = NULL"]
    B --> C["3. HOSPITAL receives<br/>notification"]
    C --> D["4. HOSPITAL assigns<br/>available AMBULANCE<br/>status = ASSIGNED"]
    D --> E["5. AMBULANCE status<br/>changes to ON_EMERGENCY"]
    E --> F["6. AMBULANCE en route<br/>GPS updates<br/>status = ON_WAY"]
    F --> G["7. AMBULANCE arrives<br/>at location<br/>status = ARRIVED"]
    G --> H["8. Response completed<br/>status = COMPLETED<br/>ambulance status = AVAILABLE"]
    
    style A fill:#e1f5ff
    style B fill:#b3e5fc
    style C fill:#81d4fa
    style D fill:#4fc3f7
    style E fill:#29b6f6
    style F fill:#03a9f4
    style G fill:#039be5
    style H fill:#0288d1
```

---

## Database Indexes (Performance Optimization)

**Recommended Indexes:**

```sql
-- User searches
CREATE INDEX idx_user_email ON USER(email);
CREATE INDEX idx_user_phone ON USER(phone);

-- Hospital searches
CREATE INDEX idx_hospital_email ON HOSPITAL(email);
CREATE INDEX idx_hospital_location ON HOSPITAL(location);

-- Ambulance lookups
CREATE INDEX idx_ambulance_hospitalId ON AMBULANCE(hospitalId);
CREATE INDEX idx_ambulance_status ON AMBULANCE(status);
CREATE INDEX idx_ambulance_registrationNo ON AMBULANCE(registrationNo);

-- Emergency request queries
CREATE INDEX idx_request_userId ON EMERGENCY_REQUEST(userId);
CREATE INDEX idx_request_hospitalId ON EMERGENCY_REQUEST(hospitalId);
CREATE INDEX idx_request_ambulanceId ON EMERGENCY_REQUEST(ambulanceId);
CREATE INDEX idx_request_status ON EMERGENCY_REQUEST(status);
CREATE INDEX idx_request_priority ON EMERGENCY_REQUEST(priority);
CREATE INDEX idx_request_createdAt ON EMERGENCY_REQUEST(createdAt DESC);
CREATE INDEX idx_request_userId_status ON EMERGENCY_REQUEST(userId, status);
CREATE INDEX idx_request_hospitalId_status ON EMERGENCY_REQUEST(hospitalId, status);
```

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-03-26 | Initial schema definition with 4 core entities |

