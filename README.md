# Angel Database Engine

> A high-performance, disk-based database storage engine written from scratch in pure Java, now evolved into a thread-safe, RESTful backend service.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![System Design](https://img.shields.io/badge/System_Design-High_Performance-blue?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

## Overview

**Angel DB** is a custom row-oriented storage engine implementation that manages data persistence manually without relying on external libraries (like SQL). It demonstrates core database concepts including **Disk I/O management**, **Page-based memory architecture**, **Caching**, and **B+ Tree Indexing**.

With the v1.1 Update, the engine has been extended with Spring Boot to provide a full RESTful Web Service. This allows AngelDB to function not just as a local CLI tool, but as a standalone backend server capable of handling remote HTTP requests with concurrent read/write operations.

## IMPORTANT: Single Process Restriction

**DO NOT run the REST Server and the CLI Shell at the same time.**

Because the Server and the CLI run in separate Java Virtual Machines (JVMs), they instantiate their own independent BufferPool (Cache).

* If both processes write to angel.db simultaneously, the in-memory caches will de-sync.
* This WILL result in data corruption, lost writes, or inconsistent database states.
* Always ensure one process is fully terminated before starting the other.

## Key Features

### Core Engine

* **B+ Tree Indexing**: Implements a self-balancing tree structure ($O(\log N)$) supporting efficient Inserts, Deletes, Point Queries, and Range Scans.
* **Disk Persistence**: Manages a custom binary file format using `RandomAccessFile`, handling data serialization and deserialization at the byte level.
* **Buffer Pool Manager**: Simulates a memory hierarchy with a "Write-Through" caching strategy and **FIFO** (First-In-First-Out) eviction policy to minimize expensive disk I/O.
* **Page Architecture**: Data is stored in fixed-size **4KB Pages**, mimicking real-world database page layouts (Header + Payload).
* **Robust Deletion Logic**: Features a complete implementation of B+ Tree deletion algorithms, including **Underflow handling**, **Redistribution (Borrowing)**, **Merging**, and **Internal Node Rotation**.
* **Thread Safety**: Implements ReentrantReadWriteLock to safely handle concurrent requests from multiple web clients (within the same process).

### Connectivity

* **REST API**: A Spring Boot controller layer exposing database operations via JSON-based HTTP endpoints.
* **Interactive CLI**: A built-in shell (AngelShell) that supports SQL-like commands and bulk operations for stress testing.

## Architecture

The system is layered to separate concerns, mimicking professional database architectures:

1. **API Layer (New)**: `DBController` handles HTTP requests and maps JSON to internal objects.
2. **Service Layer (New)**: `DBService` executes business logic and validation.
3. **AngelShell (CLI)**: Parses user input and executes commands.
4. **BTree Layer**: Handles the logic of the index (Splitting, Merging, Searching).
5. **BufferPool Layer**: Acts as an intermediary, caching hot pages in RAM.
6. **DiskManager Layer**: The physical layer that reads/writes raw bytes to `angel.db`.

## Installation & Usage

### Prerequisites

* Java Development Kit (JDK) 21 or higher.
* Docker & Docker Compose (to run the server mode easily).
* Maven (if you want to run the server mode by yourself).

### Clone the repository

    ```bash
    git clone https://github.com/AngelPwG/AngelDB.git
    cd AngelDB
    ```
### Option 1: 🐳 Running the REST Server with Docker (Recommended)

The easiest way to run AngelDB Server is using Docker Compose. This ensures you have the correct environment and persistence without installing Java manually.

1. **Start the Server:**

    ```bash
   docker-compose up --build
   ```
    
    *The server will start on port 8080 and data will be persisted in the `./data` folder.*

2. **Stop the Server:** Press `Ctrl + C` or run:
    
    ```bash
   docker-compose down
   ```

### Option 2: Running the REST Server

1. **Start the Server:**

    ```bash
    mvn spring-boot:run
    ```

    *The server will start on port 8080.*

2. **Test with CURL:**

    ```bash
    curl -X POST http://localhost:8080/api/v1/insert \
     -H "Content-Type: application/json" \
     -d '{"id": 1, "name": "Bruce Wayne", "age": 35}'
    ```

### Option 3: Running the CLI Shell

1. **Compile the source:**

    ```bash
    # For Linux/Mac
    javac -d bin $(find src/main/java -name "*.java" -not -path "*/api/*")

    # For Windows (PowerShell)
    $files = Get-ChildItem -Recurse src\main\java\*.java | Where-Object { $_.FullName -notmatch "api" }
    ```

2. **Run the Shell:**

    ```bash
    java -cp bin Main
    ```

## Command & API Reference

Whether you are using the interactive CLI or the REST API, AngelDB only supports the following core operations.

### CLI Commands

Once inside the shell, you can run the following commands:

| Command | Description | Example |
| :--- | :--- | :--- |
| `insert <id> <name> <age>` | Inserts a new record. | `insert 10 Batman 35` |
| `select <id>` | Retrieves a record by ID. | `select 10` |
| `select range <start> <end>` | Retrieves a list of record between two IDs. | `select range 10 20` |
| `update <id> <name> <age>` | Updates an existing record. | `update 10 Bruce 36` |
| `delete <id>` | Deletes a record by ID. | `delete 10` |
| `bulk_insert <count>` | Inserts N records automatically (Stress Test). | `bulk_insert 500` |
| `bulk_delete <start> <count>` | Deletes range of records (Stress Test). | `bulk_delete 1 500` |
| `exit` | Closes the database and saves state. | `exit` |

### REST API Endpoints

Use these endpoints when running the application in **Server Mode**.
**Base URL**:`http://localhost:8080`

| Operation | Method | Endpoint | Request Body / Params | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Insert** | `POST` | `/api/v1/insert` | `{"id": 1, "name": "X", "age": 20}` | Creates a new record. |
| **Select** | `GET` | `/api/v1/select/{id}` | *Path Variable* | Retrieves a specific record. |
| **Select All** | `GET` | `/api/v1/selectAll` | *None* | Returns a JSON array of all records. |
| **Select Range** | `GET` | `/api/v1/selectRange` | `?start=1&end=50` | Returns records within the ID range. |
| **Update** | `PUT` | `/api/v1/update` | `{"id": 1, "name": "Y", "age": 21}` | Updates an existing record. |
| **Delete** | `DELETE` | `/api/v1/delete/{id}` | *Path Variable* | Deletes the specified record. |

#### Example API Request (cURL)

```bash
# Insert a new user
curl -X POST http://localhost:8080/api/v1/insert \
    -H "Content-Type: application/json" \
    -d '{"id": 100, "name": "Santiago Gonzalez", "age": 19}'
```

## Technical Deep Dive

### Concurrency Control(New)

To transition from a single-thread CLI to a multi-thread Web Service, the engine now implements a **Reader/Writer Locking Strategy**:

* `ReentrantReadWriteLock`: The B+ Tree uses this lock to maximize throughput.
  * **Shared Read Locks**: Multiple users can perform `SELECT` queries (Search/Range/All) simultaneously without blocking each other.
  * **Exclusive Write Locks**: Operations that modify the tree structure (`INSERT`, `DELETE`, `UPDATE`) acquire a write lock, momentarily blocking readers to ensure data consistency during Node Splits or Merges.

### The B+ Tree Implementation

Angel DB uses a B+ Tree of minimum degree `t=39` (configurable). This allows each 4KB node to store up to **77 keys**.

* **Insertion**: Automatically splits nodes when full, promoting the median key to the parent (recursive split up to the root).
* **Deletion**: Detects underflow (nodes < 50% capacity). It attempts to **Borrow** keys from left/right siblings (Rotation). If siblings are also minimal, it **Merges** nodes and recursively deletes the separator key from the parent.

### Memory Management (Buffer Pool)

To prevent constant disk access, Angel DB uses a `BufferPool`.

* **Capacity**: Fixed at 10 Pages (configurable) to simulate constrained RAM.
* **Eviction**: Uses a FIFO queue to swap out old pages when the pool is full.
* **Persistence**: Implements a Write-Through policy ensures data is safe on disk immediately upon modification.
* **Thread Safety**: The pool uses `synchronized` access to its internal HashMaps, ensuring that concurrent read threads don't corrupt the cache state when requesting pages.

## Running Tests

The project ensures reliability through a suite of automated tests using **TestNG** and **Mockito**.

### Unit Tests

To run the service layer validations and business logic tests:

```bash
mvn test
```

### Manual Stress Tests

Scripts for concurrency and load testing are located in `src/main/java/tests`.

## Author

### Jose Angel

* *Information Technologies Engineering Student @ UPSIN*
* [LinkedIn](https://www.linkedin.com/in/jose-angel-gp/)

---
*Built for educational purposes to bridge the gap between high-level application development and low-level systems programming.*
