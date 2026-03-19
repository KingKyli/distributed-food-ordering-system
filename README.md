# Distributed Food Ordering System

A distributed restaurant ordering system developed in **Java and Android** as part of a university software engineering project.

The application simulates a food ordering platform where users can browse restaurants, manage their basket, and place orders while the system handles communication between different components using distributed system principles.

---

## Overview

This project demonstrates the implementation of a **client–server based food ordering system**.  
The Android application acts as the client interface, communicating with a backend server responsible for handling restaurant data, product management, and order processing.

The goal of the project was to explore **distributed systems concepts**, application architecture, and mobile development.

---

## Features

- **Restaurant browsing** – browse a live list of restaurants fetched from the server
- **Advanced filtering** – filter by cuisine, stars, price range, and location
- **Product listing** – view menu items per restaurant with prices and availability
- **Basket management** – add/remove items, enforces single-store ordering
- **Order placement** – submits purchase requests to the server in real-time
- **Partner / restaurant management interface** – secure login for restaurant managers
- **Product management** – add, edit, and delete menu items via the manager console
- **Configurable server** – set the server IP and port directly from the Settings screen (no code changes needed)
- **Connection timeout** – 5-second socket timeout prevents the app from hanging

---

## Architecture

The system follows a **client–server architecture**:

```
┌─────────────────────────────────────────────┐
│              Android Client App             │
│                                             │
│  WelcomeActivity ──► MainActivity           │
│        │               │                   │
│        ▼               ▼                   │
│  SettingsActivity  RestaurantDetailsActivity│
│                        │                   │
│                        ▼                   │
│                  BasketActivity             │
│                        │                   │
│  PartnerLoginActivity──► ManagerConsole     │
└──────────────┬──────────────────────────────┘
               │ TCP Socket (JSON protocol)
               ▼
┌──────────────────────────────┐
│        Backend Server        │
│  (handles SEARCH, BUY,       │
│   ADD/REMOVE/UPDATE product, │
│   ADD/REMOVE store)          │
└──────────────────────────────┘
```

- The **Android application** acts as the client.
- **MasterCommunicator** manages the persistent TCP socket connection with a 5-second connect timeout.
- **ServerConnection** is a thread-safe singleton wrapper around the communicator.
- Communication uses a **custom text protocol** over TCP (e.g. `SEARCH:lat:lon:category:stars:price`).
- **Basket** is a thread-safe singleton that enforces single-store ordering.

---

## Setup & Running

### Prerequisites
- Android Studio (Electric Eel or newer recommended)
- A running backend server (provided separately)
- An Android device or emulator on the **same network** as the server

### Local Mock Server (recommended for demos)
This repository includes a lightweight mock backend you can run locally.

- Start it on your PC:
    - `javac MockServer.java`
    - `java MockServer`
- Then in the Android emulator, connect to: `10.0.2.2:8765`

This is great for quick demos and consistent screenshots/videos for your CV.

### Configuration
1. Clone the repository and open it in Android Studio.
2. Build and run the app on your device/emulator.
3. On the **Welcome** screen, if no server is configured you will be prompted to go to **Settings**.
4. In **Settings → Server Configuration**, enter the server's **IP address** and **port** (default: 5000).
5. Tap **Save Server Settings**, then return to the Welcome screen and tap **Get Started**.

> **Note:** The server must be reachable from the Android device. If using a physical device and a local server, both must be on the same Wi-Fi network. Update the IP whenever you switch networks — no code changes needed.

---

## Key Components

| Component | Description |
|---|---|
| `WelcomeActivity` | Entry point; reads server config from SharedPreferences and connects |
| `MainActivity` | Home screen; fetches and displays restaurant list from server |
| `FiltersActivity` | Advanced search filters (cuisine, distance, stars, price, location) |
| `RestaurantDetailsActivity` | Shows menu items for a selected restaurant |
| `BasketActivity` | Manages the shopping basket and submits purchase orders |
| `SettingsActivity` | App settings including server IP/port configuration |
| `PartnerLoginActivity` | Secure login screen for restaurant partner managers |
| `ManagerConsoleActivity` | Dashboard showing inventory summary for the manager |
| `AddProductActivity` | Form to add new menu items to the server |
| `EditProductActivity` | List of products with edit/delete actions |
| `ProductEditActivity` | Form to edit an existing product's price/stock |
| `ServerConnection` | Thread-safe singleton managing the server connection |
| `MasterCommunicator` | Handles all TCP socket communication with the backend |
| `Basket` | Thread-safe singleton storing the user's current order |
| `Store` | Data model for a restaurant, including JSON serialization |
| `Product` | Data model for a menu item, including JSON serialization |

---

## Technologies Used

- **Java** – primary language
- **Android SDK** – UI and application lifecycle
- **Gradle** (Kotlin DSL) – build system
- **TCP Sockets** – real-time client-server communication
- **JSON** – data serialization format (`org.json`)
- **SharedPreferences** – persistent local storage for settings and filters
- **RecyclerView** – efficient list rendering

---

## Academic Context

Developed as part of a **Computer Science course at the Athens University of Economics and Business (AUEB)**.

The project focuses on applying distributed systems concepts within a mobile application environment, including:
- Custom application-level protocol design over TCP
- Concurrent request handling from multiple clients
- Real-time inventory and order management

---

## Author

**Sotiris Kylintireas**  
Computer Science Student – Athens University of Economics and Business
