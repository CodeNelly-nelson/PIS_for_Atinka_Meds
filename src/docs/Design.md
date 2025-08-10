# Atinka Meds — DSA Design Notes

## Core Data Structures
- `Vec<T>`: dynamic array; O(1) amortized append; used as primary in-memory store.
- `HashMapOpen<K,V>` / `HashSetOpen`: open addressing; O(1) avg lookup; indexes for drugs by code & supplier IDs per drug.
- `MinHeap<T>`: stock priority; O(log n) insert/extract; used to show low-stock Top N.
- `LinkedStack` / `LinkedQueue`: O(1) push/pop; conceptual basis for sales/purchase streams (persisted to CSV).

## Algorithms
- Sorting: `MergeSort` (stable, O(n log n) time, O(n) space) for lists; `InsertionSort` for small/near-sorted cases.
- Searching: `BinarySearch` on sorted vectors for codes; linear scans for name/supplier contains.
- String search: custom case-insensitive substring match (no java.util.regex).

## Why these choices
- **MergeSort** over QuickSort: stability (keeps ties in input order) and predictable worst-case O(n log n).
- **HashMapOpen**: offline speed for lookups (code→drug), small memory footprint vs. chaining.
- **MinHeap**: “what’s running out first?” is natural as a priority problem.
- **CSV + atomic writes**: offline, auditable, simple to inspect/recover.

## Trade-offs
- MergeSort’s extra space vs. in-place quicksort; stability wins for UI.
- Open addressing degrades to O(n) on heavy collisions → mitigated with resize policy.
- CSV logs are append-only; report generation does O(n) scans — acceptable at this scale; can shard by day if needed.

## Complexity Summary
- Lookups: HashMap avg O(1); worst O(n) (rare).
- BinarySearch: O(log n) with sorted precondition.
- Linear name/supplier filter: O(n).
- Heap operations: O(log n).
