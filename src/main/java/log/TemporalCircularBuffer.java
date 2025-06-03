package log;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TemporalCircularBuffer<T> {
    private final Object[] buffer;
    private final TemporalEntry[] temporalEntries;
    private final int capacity;
    private final AtomicInteger size = new AtomicInteger(0);
    private final AtomicInteger startIndex = new AtomicInteger(0);
    private final ReentrantReadWriteLock bufferLock = new ReentrantReadWriteLock();
    private final TemporalAmount maxAge;

    private static class TemporalEntry {
        final Instant timestamp;

        TemporalEntry() {
            this.timestamp = Instant.now();
        }

        boolean isExpired(TemporalAmount maxAge) {
            return timestamp.plus(maxAge).isBefore(Instant.now());
        }
    }

    public TemporalCircularBuffer(int capacity, TemporalAmount maxAge) {
        this.capacity = capacity;
        this.buffer = new Object[capacity];
        this.temporalEntries = new TemporalEntry[capacity];
        this.maxAge = maxAge;
    }

    public void add(T item) {
        bufferLock.writeLock().lock();
        try {
            removeExpiredEntries();

            int currentSize = size.get();
            if (currentSize < capacity) {
                int index = (startIndex.get() + currentSize) % capacity;
                buffer[index] = item;
                temporalEntries[index] = new TemporalEntry();
                size.incrementAndGet();
            } else {
                int index = startIndex.get();
                buffer[index] = item;
                temporalEntries[index] = new TemporalEntry();
                startIndex.set((index + 1) % capacity);
            }
        } finally {
            bufferLock.writeLock().unlock();
        }
    }

    private void removeExpiredEntries() {
        if (maxAge == null) return;

        while (size.get() > 0) {
            int firstIndex = startIndex.get();
            TemporalEntry firstEntry = temporalEntries[firstIndex];

            if (firstEntry != null && firstEntry.isExpired(maxAge)) {
                startIndex.set((firstIndex + 1) % capacity);
                size.decrementAndGet();
            } else {
                break;
            }
        }
    }

    public int size() {
        bufferLock.readLock().lock();
        try {
            removeExpiredEntries();
            return size.get();
        } finally {
            bufferLock.readLock().unlock();
        }
    }

    public Iterable<T> range(int startFrom, int count) {
        if (startFrom < 0 || startFrom >= size.get()) {
            return Collections.emptyList();
        }

        return () -> new TemporalSafeIterator<>(this, startFrom, count);
    }

    public Iterable<T> all() {
        return range(0, size.get());
    }

    @SuppressWarnings("unchecked")
    T get(int index) {
        bufferLock.readLock().lock();
        try {
            removeExpiredEntries();

            if (index < 0 || index >= size.get()) {
                throw new IndexOutOfBoundsException();
            }
            return (T) buffer[(startIndex.get() + index) % capacity];
        } finally {
            bufferLock.readLock().unlock();
        }
    }
}