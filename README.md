# Memory Allocation Strategy Simulation

A Java-based educational simulation of contiguous memory allocation, comparing **First Fit** and **Best Fit**.

## Overview

The project models a simplified memory manager that processes allocation and release requests over a contiguous memory space. It demonstrates how allocation policy influences placement and fragmentation.

## Features

- First Fit and Best Fit allocation strategies
- Allocation and release requests
- Tracking and locating allocated blocks
- Memory compaction when a request cannot be satisfied by the current free-space layout
- Text-based examples for reproducible walkthroughs

## Example observation

In the sample workload tested during project preparation, First Fit required one compaction, while Best Fit completed the sequence without compaction. This is a result from one sample only—not a general performance conclusion.

## Requirements

- Java Development Kit (JDK) 8 or later

## Running

The Java source file should be placed under `src/`. Compile and run using the actual class name in the source:

```bash
javac src/<MainClass>.java
java -cp src <MainClass>
```

See `examples/` for an illustrative workload and its observed summary. The example input may need adaptation to the parser format implemented by the submitted source.

## Limitations

- This is an educational simulation, not an operating-system memory allocator.
- The First Fit/Best Fit comparison is based on one sample workload.
- No broad benchmark or statistical performance evaluation is claimed.

## License

No open-source license has been added. All rights reserved unless the owner specifies otherwise.
