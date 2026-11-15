import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simulates contiguous memory allocation with First Fit and Best Fit.
 * If a request cannot fit in a single free block but total free memory is
 * sufficient, memory is compacted and the request is retried.
 */
public final class MemoryAllocator {
    private enum Strategy { FIRST_FIT, BEST_FIT }

    private static final class Simulator {
        private final String[] memory;
        private final Strategy strategy;
        private int compactionCount;
        private int failedRequests;

        Simulator(int memorySize, Strategy strategy) {
            if (memorySize <= 0) {
                throw new IllegalArgumentException("Memory size must be positive.");
            }
            this.memory = new String[memorySize];
            this.strategy = strategy;
        }

        boolean request(String processName, int size) {
            if (processName == null || processName.isBlank()) {
                throw new IllegalArgumentException("Process name must not be empty.");
            }
            if (size <= 0) {
                throw new IllegalArgumentException("Process size must be positive.");
            }
            if (containsProcess(processName)) {
                System.out.printf("%s: REJECT REQUEST %s (%d) — process already allocated%n",
                        strategy, processName, size);
                failedRequests++;
                return false;
            }

            int start = findFreeBlock(size);
            if (start < 0) {
                if (countFreeCells() < size) {
                    System.out.printf("%s: REJECT REQUEST %s (%d) — insufficient total free memory%n",
                            strategy, processName, size);
                    failedRequests++;
                    return false;
                }
                compact();
                compactionCount++;
                start = findFreeBlock(size);
            }

            if (start < 0) {
                System.out.printf("%s: REJECT REQUEST %s (%d) — no contiguous block available%n",
                        strategy, processName, size);
                failedRequests++;
                return false;
            }

            Arrays.fill(memory, start, start + size, processName);
            System.out.printf("%s: ALLOCATE %s (%d) at address %d%n",
                    strategy, processName, size, start);
            return true;
        }

        boolean release(String processName) {
            int released = 0;
            for (int i = 0; i < memory.length; i++) {
                if (processName.equals(memory[i])) {
                    memory[i] = null;
                    released++;
                }
            }
            if (released == 0) {
                System.out.printf("%s: RELEASE %s — process not found%n", strategy, processName);
                return false;
            }
            System.out.printf("%s: RELEASE %s (%d cells)%n", strategy, processName, released);
            return true;
        }

        private boolean containsProcess(String name) {
            for (String cell : memory) {
                if (name.equals(cell)) return true;
            }
            return false;
        }

        private int countFreeCells() {
            int count = 0;
            for (String cell : memory) if (cell == null) count++;
            return count;
        }

        private int findFreeBlock(int requiredSize) {
            int chosenStart = -1;
            int chosenLength = Integer.MAX_VALUE;
            int i = 0;
            while (i < memory.length) {
                if (memory[i] != null) {
                    i++;
                    continue;
                }
                int start = i;
                while (i < memory.length && memory[i] == null) i++;
                int length = i - start;
                if (length >= requiredSize) {
                    if (strategy == Strategy.FIRST_FIT) return start;
                    if (length < chosenLength) {
                        chosenLength = length;
                        chosenStart = start;
                    }
                }
            }
            return chosenStart;
        }

        private void compact() {
            int write = 0;
            for (int read = 0; read < memory.length; read++) {
                if (memory[read] != null) {
                    memory[write++] = memory[read];
                }
            }
            Arrays.fill(memory, write, memory.length, null);
            System.out.printf("%s: COMPACTION performed%n", strategy);
        }

        void printSummary() {
            System.out.println("\n" + strategy + " final memory:");
            List<String> segments = new ArrayList<>();
            int i = 0;
            while (i < memory.length) {
                String owner = memory[i];
                int start = i;
                while (i < memory.length && (owner == null ? memory[i] == null : owner.equals(memory[i]))) {
                    i++;
                }
                int length = i - start;
                segments.add((owner == null ? "FREE" : owner) + ":" + length);
            }
            System.out.println(String.join(" | ", segments));
            System.out.println("Compactions: " + compactionCount);
            System.out.println("Failed requests: " + failedRequests);
        }
    }

    private static final class Operation {
        final String command;
        final String process;
        final int size;
        Operation(String command, String process, int size) {
            this.command = command;
            this.process = process;
            this.size = size;
        }
    }

    private static List<Operation> readOperations(Path file) throws IOException {
        List<Operation> operations = new ArrayList<>();
        int lineNumber = 0;
        for (String raw : Files.readAllLines(file)) {
            lineNumber++;
            String line = raw.split("#", 2)[0].trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            try {
                if (parts[0].equalsIgnoreCase("REQUEST") && parts.length == 3) {
                    operations.add(new Operation("REQUEST", parts[1], Integer.parseInt(parts[2])));
                } else if (parts[0].equalsIgnoreCase("RELEASE") && parts.length == 2) {
                    operations.add(new Operation("RELEASE", parts[1], 0));
                } else {
                    throw new IllegalArgumentException("Expected REQUEST name size or RELEASE name");
                }
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Invalid command at " + file + ":" + lineNumber + " — " + ex.getMessage(), ex);
            }
        }
        return operations;
    }

    public static void main(String[] args) throws IOException {
        int memorySize = args.length >= 1 ? Integer.parseInt(args[0]) : 16;
        Path operationsFile = args.length >= 2 ? Path.of(args[1]) : Path.of("examples/operations.txt");
        List<Operation> operations = readOperations(operationsFile);

        Simulator firstFit = new Simulator(memorySize, Strategy.FIRST_FIT);
        Simulator bestFit = new Simulator(memorySize, Strategy.BEST_FIT);

        for (Operation op : operations) {
            System.out.println("\nOperation: " + op.command + " " + op.process
                    + (op.command.equals("REQUEST") ? " " + op.size : ""));
            if (op.command.equals("REQUEST")) {
                firstFit.request(op.process, op.size);
                bestFit.request(op.process, op.size);
            } else {
                firstFit.release(op.process);
                bestFit.release(op.process);
            }
        }
        firstFit.printSummary();
        bestFit.printSummary();
    }
}
