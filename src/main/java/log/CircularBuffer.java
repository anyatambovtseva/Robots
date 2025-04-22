package log;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CircularBuffer<T> {
    private final Object[] buffer;
    private final int capacity;
    private final AtomicInteger size = new AtomicInteger(0);
    private final AtomicInteger startIndex = new AtomicInteger(0);
    private final ReentrantReadWriteLock bufferLock = new ReentrantReadWriteLock();

    public CircularBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new Object[capacity];
    }

    public void add(T item) {
        bufferLock.writeLock().lock();
        try {
            int currentSize = size.get();
            if (currentSize < capacity) {
                buffer[(startIndex.get() + currentSize) % capacity] = item;
                size.incrementAndGet();
            } else {
                buffer[startIndex.get()] = item;
                startIndex.set((startIndex.get() + 1) % capacity);
            }
        } finally {
            bufferLock.writeLock().unlock();
        }
    }

    public int size() {
        return size.get();
    }

    public Iterable<T> range(int startFrom, int count) {
        if (startFrom < 0 || startFrom >= size.get()) {
            return Collections.emptyList();
        }

        return () -> new SafeIterator<>(this, startFrom, count);
    }

    public Iterable<T> all() {
        return range(0, size.get());
    }

    @SuppressWarnings("unchecked")
    T get(int index) {
        bufferLock.readLock().lock();
        try {
            if (index < 0 || index >= size.get()) {
                throw new IndexOutOfBoundsException();
            }
            return (T) buffer[(startIndex.get() + index) % capacity];
        } finally {
            bufferLock.readLock().unlock();
        }
    }
}