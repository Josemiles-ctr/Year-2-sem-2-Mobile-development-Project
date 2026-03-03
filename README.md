# Emergency Ambulance Request System

## Project Overview

The Emergency Ambulance Request System is an Android mobile application developed using Jetpack Compose.  

The purpose of this application is to improve emergency response efficiency by digitizing ambulance requests and providing real-time information on ambulance availability.

---

## Problem Statement

Currently:

- Individuals contact hospitals manually during emergencies.
- Many people do not have hospital contact information readily available.
- Ambulance availability is unknown prior to calling.
- Response times are slow and unpredictable.

These challenges result in delayed emergency response and lack of coordination between patients and hospitals.

---

## Proposed Solution

This mobile application introduces a digital emergency coordination system where:

- Hospitals register ambulances within the system.
- Ambulances update their availability status.
- Users can view registered hospitals.
- Users can send emergency requests digitally.
- Emergency request status can be tracked in real time.

---

## System Users

1. User (Patient)
2. Hospital
3. Ambulance

---

## Key Features

### Hospital Features
- Account registration and authentication
- Add ambulances
- Edit ambulance details
- Remove ambulances
- View incoming emergency requests

### Ambulance Features
- Update availability status:
  - Available
  - On Emergency
  - Offline
- Provide real-time status updates

### User Features
- Account registration and authentication
- View list of registered hospitals
- View available ambulances
- Submit emergency requests
- Track request status

---

## Emergency Request Flow

1. User selects a hospital.
2. User submits an emergency request.
3. Hospital receives the request notification.
4. Hospital assigns an available ambulance.
5. Ambulance status updates to "On Emergency".
6. User views updated request status.

---

## Technologies Used

- Kotlin
- Jetpack Compose
- Navigation Compose
- ViewModel (MVVM Architecture)
- Backend or Realtime Database (e.g., Firebase)

---

## Architecture

The application follows the MVVM (Model-View-ViewModel) architectural pattern to ensure separation of concerns, maintainability, and scalability.

- Model – Handles data and business logic
- View – Jetpack Compose user interface
- ViewModel – Manages state and application logic

---

## Minimum Viable Product (MVP)

- User authentication
- Hospital registration
- Ambulance registration
- Ambulance availability management
- Emergency request submission
- Request status tracking

---

## Future Improvements

- GPS-based hospital discovery
- Live ambulance tracking
- Push notifications
- Emergency request history
- Administrative dashboard

---

## Project Goal

The goal of this project is to reduce emergency response delays by creating a structured and coordinated digital system for ambulance management and emergency communication.

---

## AS PROPOSED BY THE TEAM
1. OTAI JOSEPH
2. ABUREK EMMANUEL
3. AKATUKUNDA PRECIOUS PRAISE
4. AGABA DORECK
5. KIBENGE VICTOR