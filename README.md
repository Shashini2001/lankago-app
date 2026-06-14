🚌 Lanka Go
A comprehensive public transportation application designed to revolutionize the bus travel experience in Sri Lanka.

Overview
Lanka Go bridges the gap between bus drivers and passengers by providing a unified, real-time ecosystem. Built natively for Android, the application solves everyday public transit frustrations by offering live GPS tracking, seamless digital seat booking, and direct communication channels. The platform dynamically adapts its interface based on the user's selected role (Driver or Passenger).

 Key Features
🧍 For Passengers
Smart Route Search: Find buses instantly by searching for a specific Route Number (e.g., "177") or City Destination (e.g., "Kaduwela").

Live GPS Tracking: Watch your specific bus moving in real-time on an embedded Google Map, complete with estimated arrival times and distance tracking.

Interactive Seat Booking: Select seats via a custom visual layout. White (Available), Ash (Preview), and Dark Blue (Selected).

Digital QR Ticketing: Generate a secure, downloadable digital bus ticket with a unique QR code upon booking confirmation.

Review System: Rate your journey (1-5 stars) and attach live photos to report feedback.

🚍 For Drivers
One-Tap Location Broadcasting: A background Kotlin service pushes live GPS coordinates to the Firebase database, updating all passengers on the route.

Ticket Scanning: Built-in QR scanner to verify passenger digital tickets during boarding.

Booking Management: View real-time seat capacities and passenger booking details for upcoming trips.

Duty Dashboard: Access daily driving schedules and notifications natively within the app.

 System-Wide
Secure Authentication: Passwordless, highly secure login using Firebase Phone Authentication (OTP SMS).

Localization: Full multilingual support including English, Sinhala, and Tamil.

Customization: Dedicated Dark Mode and Light Mode themes.

 Tech Stack
Front-End / UI: Android XML

Core Logic: Kotlin

Database: Firebase Realtime Database

Authentication: Firebase Auth (Phone/OTP)

Storage: Firebase Storage (Base64 / Image Uploads)

Mapping: Google Maps API / Location Services

 Database Structure Highlights
Lanka Go uses an optimized NoSQL structure to separate live tracking data from static profiles, ensuring low latency and reduced data usage.

JSON
{
  "ActiveBuses": {
    "bus_001": {
      "latitude": 6.9328,
      "longitude": 79.9845,
      "routeId": "177"
    }
  },
  "Users": {
     "uid": { "role": "Passenger" }
  },
  "Routes": { },
  "Bookings": { },
  "Reviews": { }
}

