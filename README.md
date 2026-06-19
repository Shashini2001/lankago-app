# 🚌 Lanka Go

**Lanka Go** is a comprehensive public transportation mobile application designed to modernize and simplify the bus travel experience in Sri Lanka. The platform connects passengers and bus drivers through a unified real-time ecosystem, providing live bus tracking, digital seat reservations, secure ticketing, and seamless communication.

Built natively for Android using Kotlin and Firebase technologies, Lanka Go aims to make public transportation more efficient, convenient, and user-friendly for everyone.

---

##  Overview

Public transportation users often face challenges such as uncertainty about bus arrival times, overcrowding, difficulty finding routes, and manual ticketing processes.

**Lanka Go** addresses these issues by offering:

* Real-time GPS bus tracking
* Smart route searching
* Digital seat reservation
* QR-based ticket verification
* Passenger feedback and review system
* Driver management tools

The application dynamically adapts its interface based on the selected user role:

* 🧍 Passenger
* 🚍 Driver

---

#  Features

##  Passenger Features

###  Smart Route Search

Passengers can quickly search for available buses by:

* Route Number (e.g., 177)
* City Destination (e.g., Kaduwela)

### 📍 Live GPS Tracking

Track buses in real-time using Google Maps integration.

Features include:

* Live bus location updates
* Estimated arrival times (ETA)
* Distance tracking
* Route visualization

### 💺 Interactive Seat Booking

Book seats through a visual seat selection interface.

Seat Status:

| Color        | Status    |
| ------------ | --------- |
| ⚪ White      | Available |
| ⚫ Ash        | Preview   |
| 🔵 Dark Blue | Selected  |

###  Digital QR Ticketing

After booking confirmation, passengers receive:

* Secure digital ticket
* Unique QR code
* Downloadable ticket format
* Easy boarding verification

### ⭐ Review & Feedback System

Passengers can:

* Rate trips from 1–5 stars
* Upload live photos
* Submit complaints and suggestions
* Improve service quality through feedback

---

## 🚍 Driver Features

<img width="180" height="250" alt="driver__2_-removebg-preview" src="https://github.com/user-attachments/assets/c5e14f78-8c02-4336-b342-ca0c324d2d14" />


###  One-Tap Location Broadcasting

A background Kotlin service continuously updates bus GPS coordinates to Firebase Realtime Database.

Benefits:

* Real-time passenger tracking
* Automatic location synchronization
* Low-latency updates

### QR Ticket Scanning

Drivers can verify passenger tickets instantly by scanning QR codes during boarding.

### Booking Management

Drivers can access:

* Current seat occupancy
* Passenger booking information
* Upcoming trip reservations

###  Duty Dashboard

A dedicated dashboard provides:

* Daily schedules
* Route assignments
* Important notifications
* Driver activity information

---

#  System-Wide Features

###  Secure Authentication

Passwordless login using Firebase Phone Authentication.

Features:

* OTP SMS Verification
* Secure user identity management
* Easy onboarding process

###  Multilingual Support

Supports multiple languages:

* English
* Sinhala
* Tamil

### Theme Customization

Users can personalize the application with:

* Light Mode
* Dark Mode

---

#  System Architecture

Lanka Go follows a cloud-based architecture powered by Firebase services.

### Core Components

* Android Mobile Application
* Firebase Authentication
* Firebase Realtime Database
* Firebase Storage
* Google Maps API
* Location Services

---

#  Technology Stack

| Component            | Technology                 |
| -------------------- | -------------------------- |
| Front-End UI         | Android XML                |
| Programming Language | Kotlin                     |
| Database             | Firebase Realtime Database |
| Authentication       | Firebase Auth (OTP)        |
| Cloud Storage        | Firebase Storage           |
| Maps & Navigation    | Google Maps API            |
| Location Tracking    | Android Location Services  |
| QR Generation        | ZXing QR Library           |
| QR Scanning          | CameraX + ZXing            |

---

# Database Structure Highlights

The application utilizes an optimized NoSQL architecture to maximize performance and minimize network usage.

### Design Principles

* Separation of static and dynamic data
* Efficient real-time synchronization
* Low-latency GPS updates
* Reduced bandwidth consumption
* Scalable cloud infrastructure

### Main Collections / Nodes

```text
Users
├── Passengers
└── Drivers

Routes
Buses
Bookings
Tickets
Reviews
LiveLocations
Notifications
```

---

#  Project Objectives

* Improve public transportation accessibility.
* Reduce passenger waiting uncertainty.
* Enable real-time bus tracking.
* Digitize ticketing and seat reservations.
* Enhance communication between passengers and drivers.
* Support smart transportation initiatives in Sri Lanka.

---

#  Future Enhancements

* Online payment integration
* AI-powered arrival prediction
* Traffic-aware route optimization
* Emergency SOS feature
* Push notification alerts
* Bus occupancy forecasting

---

#  Developed By

**Shashini Ruwanthika**

Third Year Undergraduate – Information Technology

Sri Lanka 🇱🇰

---

##  License

This project is developed for educational and academic purposes as a third-year project.
