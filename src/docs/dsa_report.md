
# **Pharmacy Inventory Management System — Data Structures & Algorithms Report**

## **1. Overview**
This report documents **all Data Structures and Algorithms** implemented in the *Atinka Meds* Pharmacy Inventory Management System, 
explaining where they are used in the code, their **Big O / Ω complexity**, and the **justification** for their choice.

---

## **2. Custom Data Structures**

### **2.1 Vec<T> (Dynamic Array)**
- **Location in Code:** `atinka.dsa.Vec`
- **Purpose:** Store drugs, suppliers, customers, purchases, and sales in contiguous memory for fast access.
- **Operations:**
  - `add()` → O(1) amortized, O(n) worst-case when resizing.
  - `get(index)` → O(1).
  - `remove(index)` → O(n) due to shifting.
  - `size()` → O(1).
- **Where Used:**
  - `DrugService` → holds all `Drug` objects.
  - `SupplierService` → holds all `Supplier` objects.
  - `CustomerService` → holds all `Customer` objects.
  - Purchase & sales logs before saving to file.
- **Justification:**  
  - Faster access compared to linked lists when traversing.  
  - Easy to implement sorting and searching.  
  - Predictable memory access → CPU cache-friendly.

---

### **2.2 CustomLinkedList<T>**
- **Location in Code:** `atinka.dsa.CustomLinkedList`
- **Purpose:** Maintain purchase history in chronological order.
- **Operations:**
  - `addLast()` → O(1).
  - `removeFirst()` → O(1).
  - `find()` → O(n).
- **Where Used:**
  - Purchase history queue.
  - Recent purchase retrieval (`last 5 purchases`).
- **Justification:**  
  - Maintains **insertion order**.  
  - No shifting cost like arrays for front removal.

---

### **2.3 Stack<T>**
- **Location in Code:** `atinka.dsa.CustomStack`
- **Purpose:** Track sales transactions for undo/cancellation feature.
- **Operations:**
  - `push()` → O(1).
  - `pop()` → O(1).
  - `peek()` → O(1).
- **Where Used:**
  - Sales logging system for LIFO retrieval.
- **Justification:**  
  - Matches sales reversal requirement perfectly.  
  - Simple to implement and efficient.

---

### **2.4 CustomHashMap<K,V>**
- **Location in Code:** `atinka.dsa.CustomHashMap`
- **Purpose:** Map supplier IDs and customer IDs to their profiles.
- **Operations:**
  - `put(key, value)` → O(1) average.
  - `get(key)` → O(1) average.
  - `remove(key)` → O(1) average.
- **Where Used:**
  - Supplier mapping.
  - Customer lookup.
- **Justification:**  
  - Direct access by unique IDs.  
  - No built-in Java HashMap allowed → implemented manually.

---

### **2.5 MinHeap<T>**
- **Location in Code:** `atinka.dsa.MinHeap`
- **Purpose:** Monitor and reorder drugs by stock level for restocking alerts.
- **Operations:**
  - `insert()` → O(log n).
  - `extractMin()` → O(log n).
- **Where Used:**
  - Stock monitoring and reorder suggestions.
- **Justification:**  
  - Priority queue behavior ensures lowest stock drugs get attention first.

---

## **3. Custom Algorithms**

### **3.1 MergeSort**
- **Location in Code:** `atinka.algorithms.MergeSort`
- **Purpose:** Sort drugs by price or name.
- **Complexity:**
  - Time: O(n log n) average/worst, Ω(n log n) best.
  - Space: O(n) auxiliary.
- **Justification:**  
  - Stable sorting → preserves order for equal keys.  
  - Works efficiently for large datasets.

---

### **3.2 InsertionSort**
- **Location in Code:** `atinka.algorithms.InsertionSort`
- **Purpose:** Sort small datasets like last 5 purchases.
- **Complexity:**
  - Time: O(n²) worst, Ω(n) best for nearly sorted lists.
- **Justification:**  
  - Minimal overhead for small N.  
  - Adaptive to already sorted sequences.

---

### **3.3 Linear Search**
- **Location in Code:** `atinka.algorithms.Search`
- **Purpose:** Search by supplier name, location, or customer details.
- **Complexity:**
  - Time: O(n), Ω(1) best-case.
- **Justification:**  
  - Simple to implement.  
  - Effective for small/medium-sized datasets.

---

### **3.4 Binary Search**
- **Location in Code:** `atinka.algorithms.Search`
- **Purpose:** Search by drug code when sorted.
- **Complexity:**
  - Time: O(log n), Ω(1) best-case.
- **Justification:**  
  - Reduces search time for large sorted datasets.

---

## **4. Performance Analysis**

### **Big O & Ω Notation Table**
| Operation              | DS/Algo          | Big O      | Ω         | Justification |
|------------------------|-----------------|------------|-----------|---------------|
| Access drug by index   | Vec              | O(1)       | O(1)      | Direct index access |
| Insert drug at end     | Vec              | O(1)*      | O(1)      | Amortized resizing |
| Remove from front      | LinkedList       | O(1)       | O(1)      | Pointer re-link |
| Push sale              | Stack            | O(1)       | O(1)      | LIFO structure |
| Lookup supplier        | HashMap          | O(1) avg   | O(1) avg  | Hash-based access |
| Restock alert          | MinHeap          | O(log n)   | O(1)      | Priority ordering |
| Sort large dataset     | MergeSort        | O(n log n) | Ω(n log n)| Stable, efficient |
| Sort small dataset     | InsertionSort    | O(n²)      | Ω(n)      | Low overhead |
| Search sorted dataset  | Binary Search    | O(log n)   | Ω(1)      | Divide and conquer |

---

## **5. Trade-offs**
- **Vec**: Fast random access but costly middle insert/removal.
- **LinkedList**: Good for frequent front removals, but slower random access.
- **Stack**: Perfect for undo, but can't access arbitrary elements.
- **HashMap**: Fast lookup but requires good hash function.
- **MinHeap**: Excellent for priority tasks but adds log n overhead.

---

## **6. Conclusion**
This system fully satisfies the **Data Structures & Algorithms** requirement by:
- Implementing **all core DSAs from scratch**.
- Matching each structure to a real pharmacy workflow.
- Balancing **performance** and **simplicity** for an offline-first system.
