package com.vityarthi.inventory.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Custom bounded Circular Buffer (Ring Buffer) data structure.
 * Provides O(1) appending and fixed-memory sliding window storage for recent audit transactions.
 *
 * @param <E> The element type stored in the circular buffer
 */
public class CircularAuditBuffer<E> implements Iterable<E>, Serializable {
    private static final long serialVersionUID = 1L;

    private final Object[] elements;
    private final int capacity;
    private int head = 0;
    private int tail = 0;
    private int size = 0;

    public CircularAuditBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Buffer capacity must be strictly positive.");
        }
        this.capacity = capacity;
        this.elements = new Object[capacity];
    }

    public synchronized void add(E element) {
        if (element == null) {
            throw new IllegalArgumentException("Cannot insert null into audit buffer.");
        }
        elements[tail] = element;
        tail = (tail + 1) % capacity;

        if (size < capacity) {
            size++;
        } else {
            head = (head + 1) % capacity;
        }
    }

    public synchronized int size() {
        return size;
    }

    public int capacity() {
        return capacity;
    }

    public synchronized boolean isFull() {
        return size == capacity;
    }

    public synchronized void clear() {
        for (int i = 0; i < capacity; i++) {
            elements[i] = null;
        }
        head = 0;
        tail = 0;
        size = 0;
    }

    @SuppressWarnings("unchecked")
    public synchronized List<E> toList() {
        List<E> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int index = (head + i) % capacity;
            list.add((E) elements[index]);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public synchronized List<E> getRecent(int count) {
        int take = Math.min(count, size);
        List<E> list = new ArrayList<>(take);
        for (int i = 0; i < take; i++) {
            int index = (tail - 1 - i + capacity) % capacity;
            list.add((E) elements[index]);
        }
        return list;
    }

    @Override
    public synchronized Iterator<E> iterator() {
        return toList().iterator();
    }
}
