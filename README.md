# 🏛️ Angel Database Engine

> A high-performance, disk-based database storage engine written from scratch in pure Java.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![System Design](https://img.shields.io/badge/System_Design-High_Performance-blue?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

## 📖 Overview

**Angel DB** is a relational database engine implementation that manages data persistence manually without relying on external libraries (like SQL). It demonstrates core database concepts including **Disk I/O management**, **Page-based memory architecture**, **Caching**, and **B+ Tree Indexing**.

The goal of this project was to bridge the gap between high-level application development and low-level systems engineering by building the foundational components of a DBMS (Database Management System).

## 🚀 Key Features

* **B+ Tree Indexing**: Implements a self-balancing tree structure ($O(\log N)$) supporting efficient Inserts, Deletes, Point Queries, and Range Scans.
* **Disk Persistence**: Manages a custom binary file format using `RandomAccessFile`, handling data serialization and deserialization at the byte level.
* **Buffer Pool Manager**: Simulates a memory hierarchy with a "Write-Through" caching strategy and **FIFO** (First-In-First-Out) eviction policy to minimize expensive disk I/O.
* **Page Architecture**: Data is stored in fixed-size **4KB Pages**, mimicking real-world database page layouts (Header + Payload).
* **Robust Deletion Logic**: Features a complete implementation of B+ Tree deletion algorithms, including **Underflow handling**, **Redistribution (Borrowing)**, **Merging**, and **Internal Node Rotation**.
* **Interactive CLI**: A built-in shell (`AngelShell`) that supports SQL-like commands and bulk operations for stress testing.

## 🏗️ Architecture

The system is layered to separate concerns, mimicking professional database architectures (like SQLite or PostgreSQL):

1. **AngelShell (CLI)**: Parses user input and executes commands.
2. **BTree Layer**: Handles the logic of the index (Splitting, Merging, Searching).
3. **BufferPool Layer**: Acts as an intermediary, caching hot pages in RAM.
4. **DiskManager Layer**: The physical layer that reads/writes raw bytes to `angel.db`.

## 🛠️ Installation & Usage

### Prerequisites

* Java Development Kit (JDK) 21 or higher.

### Running the Engine

1. **Clone the repository:**

    ```bash
    git clone https://github.com/AngelPwG/AngelDB.git
    cd AngelDB
    ```

2. **Compile the project:**

    ```bash
    # For Linux/Mac
    javac -d bin $(find src -name "*.java")
    
    # For Windows (PowerShell)
    Get-ChildItem -Recurse -Filter *.java | ForEach-Object { javac -d bin $_.FullName }
    ```

3. **Run the Shell:**

    ```bash
    java -cp bin Main
    ```

### Supported Commands

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

## 🧠 Technical Deep Dive

### The B+ Tree Implementation

Angel DB uses a B+ Tree of minimum degree `t=39` (configurable). This allows each 4KB node to store up to **77 keys**.

* **Insertion**: Automatically splits nodes when full, promoting the median key to the parent (recursive split up to the root).
* **Deletion**: Detects underflow (nodes < 50% capacity). It attempts to **Borrow** keys from left/right siblings (Rotation). If siblings are also minimal, it **Merges** nodes and recursively deletes the separator key from the parent.

### Memory Management (Buffer Pool)

To prevent constant disk access, Titan DB uses a `BufferPool`.

* **Capacity**: Fixed at 10 Pages (configurable) to simulate constrained RAM.
* **Eviction**: Uses a FIFO queue to swap out old pages when the pool is full.
* **Persistence**: Implements a Write-Through policy ensures data is safe on disk immediately upon modification.

## 🔮 Future Roadmap

* [ ] Support for String keys (names) instead of just IDs.

## 👤 Author

### Jose Angel

* *Information Technologies Engineering Student @ UPSIN*
* [LinkedIn](https://www.linkedin.com/in/jose-angel-gp/)

---
*Built for educational purposes to demonstrate low-level systems programming concepts.*
