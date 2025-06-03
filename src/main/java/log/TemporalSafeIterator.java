package log;

import java.util.Iterator;

public class TemporalSafeIterator<E, T> implements Iterator<E> {
    private final TemporalCircularBuffer<T> buffer;
    private final int endIndex;
    private int currentIndex;
    private final int maxIndex;

    public TemporalSafeIterator(TemporalCircularBuffer<T> buffer, int startFrom, int count) {
        this.buffer = buffer;
        int currentSize = buffer.size();
        this.currentIndex = startFrom;
        this.maxIndex = Math.min(startFrom + count, currentSize);
        this.endIndex = currentSize;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < maxIndex && currentIndex < endIndex;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E next() {
        if (!hasNext()) {
            throw new java.util.NoSuchElementException();
        }
        E item = (E) buffer.get(currentIndex);
        currentIndex++;
        return item;
    }
}