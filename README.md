# Memory Allocation Strategy Simulation

A Java simulation comparing two contiguous memory allocation strategies: **First Fit** and **Best Fit**.

## Overview

The program models a fixed-size memory array and processes allocation and release operations. It runs the same sequence independently through both strategies, then reports the final memory layout, compaction count, and failed requests.

## Features

- First Fit: selects the first free contiguous block large enough for the request.
- Best Fit: selects the smallest free contiguous block that can satisfy the request.
- Process allocation and release operations.
- Compaction when total free memory is sufficient but no single free block is large enough.
- Rejection of invalid, duplicate, or unsatisfiable allocation requests.
- Input-file parsing with comments and line-specific error messages.

## Requirements

- Java 11 or later.

## Project structure

```text
.
├── README.md
├── src/
│   └── MemoryAllocator.java
└── examples/
    └── operations.txt
```

## Run

From the repository root:

```bash
javac -d out src/MemoryAllocator.java
java -cp out MemoryAllocator 16 examples/operations.txt
```

The first argument is the memory size. The second is the path to the operations file. If omitted, the program defaults to a memory size of 16 and `examples/operations.txt`.

## Input format

Each non-comment line must be one of:

```text
REQUEST <process-name> <size>
RELEASE <process-name>
```

Lines may contain comments beginning with `#`.

## Sample observation

For the included 16-cell workload, the tested run produced:

- First Fit: one compaction and zero failed requests.
- Best Fit: zero compactions and zero failed requests.

This is an observation for the included example only. It does not establish that Best Fit is generally superior; outcomes depend on workload and allocation sequence.

## Limitations

- This is an educational simulation, not a production operating-system memory manager.
- The implementation models contiguous allocation in a one-dimensional array.
- The example workload is small and is not a comprehensive benchmark.
- No generalized performance claim is made from the sample result.

## License

No open-source license has been specified.
