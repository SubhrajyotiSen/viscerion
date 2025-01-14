/*
 * Copyright © 2017-2019 WireGuard LLC.
 * Copyright © 2018-2019 Harsh Shandilya <msfjarvis@gmail.com>. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.wireguard.android.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableArrayList;
import com.wireguard.util.Keyed;
import java.util.Collection;
import java.util.ListIterator;
import java.util.Objects;

/**
 * ArrayList that allows looking up elements by some key property. As the key property must always
 * be retrievable, this list cannot hold {@code null} elements. Because this class places no
 * restrictions on the order or duplication of keys, lookup by key, as well as all list modification
 * operations, require O(n) time.
 */
public class ObservableKeyedArrayList<K, E extends Keyed<? extends K>>
        extends ObservableArrayList<E> implements ObservableKeyedList<K, E> {
    @Override
    public boolean add(@Nullable final E e) {
        if (e == null) throw new NullPointerException("Trying to add a null element");
        return super.add(e);
    }

    @Override
    public void add(final int index, @Nullable final E e) {
        if (e == null) throw new NullPointerException("Trying to add a null element");
        super.add(index, e);
    }

    @Override
    public boolean addAll(@NonNull final Collection<? extends E> elements) {
        if (elements.contains(null))
            throw new NullPointerException("Trying to add a collection with null element(s)");
        return super.addAll(elements);
    }

    @Override
    public boolean addAll(final int index, @NonNull final Collection<? extends E> elements) {
        if (elements.contains(null))
            throw new NullPointerException("Trying to add a collection with null element(s)");
        return super.addAll(index, elements);
    }

    @Override
    public boolean containsKey(final K key) {
        return indexOfKey(key) >= 0;
    }

    @Override
    @Nullable
    public E get(final K key) {
        final int index = indexOfKey(key);
        return index >= 0 ? get(index) : null;
    }

    @Override
    @Nullable
    public E getLast(final K key) {
        final int index = lastIndexOfKey(key);
        return index >= 0 ? get(index) : null;
    }

    @Override
    public int indexOfKey(final K key) {
        final ListIterator<E> iterator = listIterator();
        while (iterator.hasNext()) {
            final int index = iterator.nextIndex();
            if (Objects.equals(iterator.next().getKey(), key)) return index;
        }
        return -1;
    }

    @Override
    public int lastIndexOfKey(final K key) {
        final ListIterator<E> iterator = listIterator(size());
        while (iterator.hasPrevious()) {
            final int index = iterator.previousIndex();
            if (Objects.equals(iterator.previous().getKey(), key)) return index;
        }
        return -1;
    }

    @Override
    public E set(final int index, @Nullable final E e) {
        if (e == null) throw new NullPointerException("Trying to set a null key");
        return super.set(index, e);
    }

    @Override
    public boolean containsAllKeys(@NonNull Collection<? extends K> keys) {
        for (final K key : keys) if (!containsKey(key)) return false;
        return true;
    }
}
