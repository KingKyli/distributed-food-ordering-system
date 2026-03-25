# ? Distributed Food Ordering System

> A full-stack distributed system simulating a real-world food delivery platform ? built with a custom TCP protocol, multi-threaded Java backend, and a native Android client.

**GitHub:** https://github.com/KingKyli/distributed-food-ordering-system

---

## ? Screenshots

| Home | Restaurant | Basket | Manager Dashboard |
|------|------------|--------|-------------------|
| *(Home screen with 25+ restaurants, quick filters, skeleton loading)* | *(Restaurant detail with product list, stock badges)* | *(Basket with order total, checkout, history link)* | *(Dashboard with inventory stats)* |

> Add screenshots to `docs/screenshots/` and replace the placeholders above.

---

## ? Key Highlights

| | |
|---|---|
| ? | **Custom TCP protocol** for real-time client?server communication |
| ? | **OTP-based partner authentication** with expiring 6-digit access codes |
| ? | **Multi-threaded backend** handling concurrent client connections safely |
| ? | **Android client** with Material 3 design, dark mode, skeleton loading |
| ? | **Thread-safe basket** enforcing single-store ordering constraints |
| ??  | **Favorites & Order History** stored locally with SharedPreferences |
| ?? | **Repository / Service / Protocol** layered architecture |
| ? | **Skeleton loading, empty states, quick filters** ? production-quality UX |

---

## ? Architecture

```
???????????????????????????????????????????????????????????????
?                   Android Client (UI Layer)                  ?
?  MainActivity  RestaurantDetailsActivity  BasketActivity     ?
?  OrderHistoryActivity  PartnerLoginActivity  ManagerConsole  ?
???????????????????????????????????????????????????????????????
                         ?  Repository / Service Layer
              ???????????????????????
              ?  RestaurantRepository?  OrderService
              ?  PartnerAuthService  ?  ProductMgmtService
              ???????????????????????
                         ?  Protocol / Network Layer
              ???????????????????????
              ?   MasterCommunicator ?  ? reconnect strategy
              ?   ServerConnection   ?  ? singleton, thread-safe
              ?   ProtocolUtils      ?  ? command serialization
              ???????????????????????
                         ?
              TCP Socket  (port 8765)
                         ?
???????????????????????????????????????????????????????????????
?               Java Backend (MockServer)                      ?
?                                                             ?
?  ????????????????   ????????????????   ??????????????????  ?
?  ? SEARCH       ?   ? BUY          ?   ? PARTNER_LOGIN  ?  ?
?  ? Returns JSON ?   ? Decrements   ?   ? OTP code flow  ?  ?
?  ? store list   ?   ? stock safely ?   ? 5-min expiry   ?  ?
?  ????????????????   ????????????????   ??????????????????  ?
?                                                             ?
?  ????????????????   ????????????????                       ?
?  ? ADD_PRODUCT  ?   ? UPDATE/REMOVE?                       ?
?  ? REMOVE_STORE ?   ? PRODUCT      ?                       ?
?  ????????????????   ????????????????                       ?
?                                                             ?
?   25 seed restaurants  ·  thread-safe synchronized state   ?
???????????????????????????????????????????????????????????????
```

---

## ? Request Flows

### Customer ? Search & Order
```
App opens ? WelcomeActivity tries IP list (127.0.0.1, 10.0.2.2, ...)
         ? TCP handshake (CLIENT_HELLO / SERVER_HELLO)
         ? SEARCH command ? JSON store list ? RecyclerView
         ? User taps restaurant ? RestaurantDetailsActivity
         ? User adds items ? Basket (thread-safe, single-store)
         ? Place order ? BUY:storeName:productName:qty (for each item)
         ? Server decrements stock ? SUCCESS ? History saved locally
```

### Partner ? Login & Inventory Management
```
PartnerLoginActivity ? select store
         ? REQUEST_PARTNER_ACCESS_CODE:storeName
         ? Server generates 6-digit OTP (5-min expiry) ? returns masked email + code
         ? User enters code ? PARTNER_LOGIN:storeName:code
         ? Server validates, removes code ? OK: Login successful
         ? ManagerConsoleActivity shows inventory snapshot
         ? ADD_PRODUCT / UPDATE_PRODUCT / REMOVE_PRODUCT
```

---

## ?? Tech Stack

| Layer | Technology |
|---|---|
| **Android Client** | Java, Android SDK 28?35 |
| **UI** | Material Design 3 (DayNight), ConstraintLayout, RecyclerView |
| **Backend** | Java 11, raw TCP Sockets, multi-threaded |
| **Serialization** | JSON (org.json) ? hand-crafted protocol |
| **Persistence** | SharedPreferences (favorites, order history, filters, session) |
| **Build** | Gradle Kotlin DSL |
| **VCS** | Git / GitHub |

---

## ? Why Custom TCP Instead of REST?

This is an intentional design choice:

| | Custom TCP | REST/HTTP |
|---|---|---|
| **Latency** | Lower (persistent connection, no HTTP overhead) | Higher (connection per request) |
| **Learning** | Forces understanding of protocol design | Abstracted away by libraries |
| **Concurrency** | Explicit thread-per-client model | Handled by framework |
| **Control** | Full control over wire format | Constrained by HTTP verbs/status codes |

> In a production system, I would use REST with Retrofit + MVVM + Coroutines. The TCP approach here is a deliberate academic choice to demonstrate distributed systems fundamentals.

---

## ? Why `ServerConnection.requireCommunicator()` & `AppResult<T>`?

- `requireCommunicator()` ? single point of truth for connection state. Any service that needs the network calls this; it either returns the communicator or an error wrapped in `AppResult<T>`. No null checks scattered across activities.
- `AppResult<T>` ? typed result monad (success + data, or error + message). Forces callers to handle both branches. Eliminates try/catch at the UI layer.
- `MasterCommunicator` reconnect strategy ? exponential back-off reconnect loop runs on a background thread. The UI layer only sees `ServerConnection.isReady()`.

---

## ? Why Thread-Safe Basket?

The basket is a **singleton** (`Basket.getInstance()`) modified from multiple threads (UI thread adds/removes; background thread reads for order submission). Key constraints enforced:

1. **Single-store rule** ? adding a product from a different store is silently rejected (returns `false`).
2. **Synchronized methods** ? `addProduct`, `removeProduct`, `clear` are all `synchronized`.
3. **Stable IDs** ? `BasketItem.buildStableId(storeName, productName, productType)` for `DiffUtil` in the adapter.

---

## ? Repository / Service Architecture

```
Activity  ?  Service  ?  Repository  ?  MasterCommunicator
             (business     (data            (network)
              rules)        access)
```

| Class | Responsibility |
|---|---|
| `RestaurantRepository` | Fetches/parses store list and individual stores |
| `OrderService` | Submits basket items, saves to order history |
| `ProductManagementService` | Add/update/delete products via partner session |
| `PartnerAuthService` | OTP request + login flow |
| `MasterCommunicator` | Raw TCP send/receive with reconnect |
| `ServerConnection` | Singleton wrapper, `requireCommunicator()` |
| `ProtocolUtils` | OK/ERROR response parsing |

---

## ? Running the Project

### 1. Start the backend
```bash
cd distributed-food-ordering-system
javac MockServer.java && java MockServer
```

### 2. Connect from Android Emulator
The app auto-connects to `10.0.2.2:8765` (Android emulator loopback to host).

### 3. Connect from a physical device (USB)
```bash
adb reverse tcp:8765 tcp:8765
```
Then use `127.0.0.1` as server IP in the app's Settings screen.

### 4. Build the Android app
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## ? Partner Login (Demo)

1. Open the app ? tap **Partner Login**
2. Select any store from the dropdown
3. Tap **Request Access Code** ? the 6-digit code appears in the server console
4. Enter the code ? you are logged into the Manager Dashboard

---

## ? Store Catalog (25 restaurants)

| Stars | Stores | Price |
|---|---|---|
| ????? | Sushi Sun, Pasta Paradise, Ramen Republic, Le Bistro Paris, Greek Tavern, Indian Spice Garden, Poke Bowl Bar | $$?$$$ |
| ???? | Pizza Palace, Burger Barn, Gyros Express, Souvlaki Square, Noodle Nook, BBQ Nation, Thai Palace, Dim Sum Dragon, The Vegan Garden, Steakhouse Seven, Brunch Club, Crepe Cafe | $?$$$ |
| ??? | Taco Town, Kebab House, Falafel House, Tex Mex Grill, The Sandwich Bar, Cookie Corner | $?$$ |

---

## ? CV Entry (Copy?Paste Ready)

```
Distributed Food Ordering System  |  Java · Android · TCP Sockets  |  github.com/KingKyli/distributed-food-ordering-system

? Designed and implemented a distributed client?server food ordering system using Java and Android
? Built a custom TCP-based communication protocol supporting search, ordering, and inventory management commands
? Developed a multi-threaded backend server handling concurrent client requests with thread-safe shared state
? Implemented OTP-style partner authentication system with expiring 6-digit access codes
? Engineered a thread-safe basket system enforcing single-store ordering constraints across concurrent operations
? Designed advanced filtering functionality (location, cuisine, price, rating) with persistent user preferences
? Applied layered architecture (UI / Service / Repository / Protocol) with typed AppResult<T> error handling
? Built native Android client with Material Design 3, dark mode, skeleton loading, favorites, and order history
```

---

## ? Interview Answers

**"Tell me about your project"**
> *"I built a distributed food ordering system consisting of an Android client and a multi-threaded Java backend. The system communicates over a custom TCP protocol, supporting real-time restaurant search, ordering, and inventory management. I also implemented an OTP-based authentication mechanism for partners and designed the system to handle concurrent users safely through thread-safe shared state."*

**"What was the hardest part?"**
> *"Designing the communication layer and ensuring thread-safe operations on the backend, especially when handling concurrent order requests that modify shared inventory state. I also had to think carefully about the reconnect strategy on the client side so the UI never blocks the main thread."*

**"What would you improve?"**
> *"I would migrate the communication layer to a REST API with Retrofit, refactor the Android app to MVVM with ViewModels and LiveData/StateFlow, and replace the in-memory server state with a real database like PostgreSQL."*

**"What did you learn?"**
> *"I learned how to design systems end-to-end ? not just UI code ? including networking, concurrency, protocol design, and how to structure scalable, testable components using repository and service layers."*

---

*Built with Java · Android SDK · Material Design 3 · TCP Sockets · Gradle*
