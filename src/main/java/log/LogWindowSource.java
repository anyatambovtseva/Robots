package log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Что починить:
 * 1. Этот класс порождает утечку ресурсов (связанные слушатели оказываются
 * удерживаемыми в памяти)
 * 2. Этот класс хранит активные сообщения лога, но в такой реализации он 
 * их лишь накапливает. Надо же, чтобы количество сообщений в логе было ограничено 
 * величиной m_iQueueLength (т.е. реально нужна очередь сообщений 
 * ограниченного размера) 
 */
public class LogWindowSource
{
    private final int queueLength;

    private final CircularBuffer<LogEntry> messages;
    private final List<LogChangeListener> listeners = new ArrayList<>();
    private final ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock();
    private volatile LogChangeListener[] activeListeners;
    
    public LogWindowSource(int queueLength)
    {
        this.queueLength = queueLength;
        this.messages = new CircularBuffer<>(queueLength);
    }
    
    public void registerListener(LogChangeListener listener)
    {
        listenersLock.writeLock().lock();
        try {
            listeners.add(listener);
            activeListeners = null;
        } finally {
            listenersLock.writeLock().unlock();
        }
    }
    
    public void unregisterListener(LogChangeListener listener)
    {
        listenersLock.writeLock().lock();
        try {
            listeners.remove(listener);
            activeListeners = null;
        } finally {
            listenersLock.writeLock().unlock();
        }
    }
    
    public void append(LogLevel logLevel, String strMessage)
    {
        LogEntry entry = new LogEntry(logLevel, strMessage);
        messages.add(entry);
        LogChangeListener [] currentListeners = getActiveListeners();

        for (LogChangeListener listener : currentListeners)
        {
            listener.onLogChanged();
        }
    }

    private LogChangeListener[] getActiveListeners() {
        LogChangeListener[] result = activeListeners;
        if (result == null) {
            listenersLock.readLock().lock();
            try {
                result = listeners.toArray(new LogChangeListener[0]);
                activeListeners = result;
            } finally {
                listenersLock.readLock().unlock();
            }
        }
        return result;
    }
    
    public int size()
    {
        return messages.size();
    }

    public Iterable<LogEntry> range(int startFrom, int count)
    {
        return messages.range(startFrom, count);
    }

    public Iterable<LogEntry> all()
    {
        return messages.all();
    }
}
