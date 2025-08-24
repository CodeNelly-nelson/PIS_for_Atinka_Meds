# 🏥 Atinka Meds — Pharmacy Inventory System (Console App)

Atinka Meds is a **console-based pharmacy inventory management system** built in Java.  
It demonstrates **data structures and algorithms** in a practical pharmacy context: inventory, suppliers, customers, purchases, sales, and reporting.

---

## ✨ Features

- **Drugs Management**
  - Add / edit / remove drugs
  - List drugs (by name, price, stock)
  - Search by name (linear scan)
  - Low-stock alerts
  - Top-N lowest stock (MinHeap)

- **Suppliers Management**
  - Add / edit / link/unlink suppliers
  - Supplier-based filtering

- **Customers Management**
  - CRUD operations on customers

- **Sales & Purchases**
  - Record purchases (restock)
  - Record sales
  - Track latest 5 purchases
  - Generate daily/monthly totals

- **Reports**
  - Performance summaries
  - Sales by period

---

## 📂 Project Structure

```
AtinkaMeds/
├─ dist/
│  └─ AtinkaMeds.jar          # Compiled Java program
├─ data/                      # CSV files for persistence
│  ├─ drugs.csv
│  ├─ suppliers.csv
│  ├─ customers.csv
│  ├─ purchases.csv
│  └─ sales.csv
├─ src/                       # Source code
├─ Run AtinkaMeds.command     # macOS launcher script
├─ Run-AtinkaMeds.bat         # Windows launcher script
└─ README.md
```

---

## 🚀 How to Run the Program

### 🔹 On macOS
1. Ensure you have **Java 17 or later** installed. Check with:
   ```bash
   java -version
   ```
2. Make the script executable (first time only):
   ```bash
   chmod +x "Run AtinkaMeds.command"
   ```
3. Double-click `Run AtinkaMeds.command` in Finder.  
   - It will open Terminal, run the JAR, and display the Atinka Meds console menu.  
   - If blocked, right-click → **Open** → **Open**.  
   - Data will be stored in the `data/` folder beside the app.

### 🔹 On Windows
1. Ensure you have **Java 17 or later** installed. Verify:
   ```bat
   java -version
   ```
2. Double-click `Run-AtinkaMeds.bat`.  
   - It will open Command Prompt, run the JAR, and display the menu.  
   - Data is stored in the `data\` folder beside the app.

### 🔹 On Linux
```bash
cd AtinkaMeds
mkdir -p data
java -jar dist/AtinkaMeds.jar
```

---

## ⚙️ Data Structures & Algorithms (Detailed)

The program is designed as a **DSA showcase**. Every major feature is backed by a specific data structure or algorithm:

### 1. **Vec (Custom Dynamic Array)**
- Similar to `ArrayList` in Java.  
- Used for storing drugs, suppliers, and customers in memory after loading from CSV.  
- Provides fast random access (`O(1)` for `get(i)`), dynamic resizing, and sequential scans.

---

### 2. **HashMapOpen (Custom Hash Map with Open Addressing)**
- Used to index drugs and suppliers by their unique codes.  
- Provides **O(1) average time** for lookups, inserts, and updates.  
- Applied in:
  - Editing/updating drugs (`Edit / update`)
  - Linking/unlinking suppliers
  - Fast removal by code (linear scan for Vec + HashMap index)

---

### 3. **Sorting Algorithms (MergeSort)**
- **MergeSort** is used for listing and ordering data:
  - By **name** and **price**: stable `O(n log n)` sorting.  
  - By **time**: latest purchases are sorted descending.  
- Chosen for stability and efficiency even on larger datasets.

---

### 4. **Searching Algorithms (Linear Scan)**
- **Linear Scan** is used for:
  - Searching drug names by substring.  
  - Low-stock alerts.  
- Substring matching cannot be indexed efficiently with a basic hash map, so sequential search is used.  

---

### 5. **MinHeap**
- Used for **Top N lowest stock drugs**.  
- Finds N smallest elements without fully sorting the dataset.  
- Complexity: `O(n log k)` for n drugs and heap size k.  

---

### 6. **CSV Append-Only Logs**
- Instead of a database, persistence is implemented with **CSV files**.  
- Every update is:
  - Written to the CSV (append mode).  
  - Reflected in memory (`Vec`, `HashMapOpen`).  
- Guarantees consistency:
  - Restock → `O(1)` stock adjust + append to `purchases.csv`.  
  - Sale → `O(1)` adjust + append to `sales.csv`.  

---

### 7. **Algorithm/Operation Summary**

```
 1) List all (by name)           — MergeSort
 2) List all (by price)          — MergeSort
 3) Search name contains         — Linear scan
 4) Edit / update                — HashMapOpen index
 5) Link / unlink supplier       — HashMapOpen
 6) Restock (record purchase)    — O(1) stock adjust; append CSV
 7) Latest 5 purchases (by time) — MergeSort (time desc)
 8) Remove by code               — Linear scan + HashMapOpen
 9) Low-stock alerts (<= thr)    — Linear scan
10) Top N lowest stock           — MinHeap
```

---

## 🔄 Data Flow Overview

The architecture connects storage → in-memory data structures → services → CLI:

```mermaid
flowchart TD
    subgraph Storage[💾 CSV Storage]
        D1[drugs.csv]
        D2[suppliers.csv]
        D3[customers.csv]
        D4[purchases.csv]
        D5[sales.csv]
    end

    subgraph Memory[📚 In-Memory Structures]
        V1[Vec: Drugs]
        V2[Vec: Suppliers]
        V3[Vec: Customers]
        H1[HashMapOpen: Drug Index]
        H2[HashMapOpen: Supplier Index]
    end

    subgraph Services[⚙️ Services]
        S1[DrugService]
        S2[SupplierService]
        S3[CustomerService]
        S4[InventoryService]
    end

    subgraph UI[🖥️ CLI (AtinkaCLI)]
        U1[Main Menu]
        U2[Drugs Menu]
        U3[Suppliers Menu]
        U4[Customers Menu]
        U5[Sales & Reports]
    end

    Storage --> Memory
    Memory --> Services
    Services --> UI
    UI --> Services
    Services --> Storage
```

---

## 🏗️ Building from Source

If you need to recompile from `src/`:

```bash
rm -rf out dist
mkdir -p out dist
find src -name "*.java" > sources.list
javac -d out @sources.list
echo "Main-Class: atinka.Main" > MANIFEST.MF
jar cfm dist/AtinkaMeds.jar MANIFEST.MF -C out .
```

Run with:

```bash
java -jar dist/AtinkaMeds.jar
```

---

## 💡 Tips

- Enter **`0`** or **`cancel`** at any input to abort that operation.  
- **Colors:** The UI uses ANSI colors; if not supported, it falls back to ASCII.  
- **Persistence:** Data is auto-saved in the `data/` folder as CSV files.  
- **Cross-platform:** Works on macOS, Windows, and Linux with Java 17+.  

---

## 📜 License

This project was built for **academic and educational purposes** (DSA course).  
You are free to use, extend, and learn from it.  
