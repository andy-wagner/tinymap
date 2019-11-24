package net.intelie.tinymap;

import net.intelie.tinymap.util.Preconditions;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;

public abstract class ListMapBase<K, V> implements ListMap<K, V> {
    private static final Object SENTINEL = new Object();

    @SuppressWarnings("unchecked")
    @Override
    public V getOrDefault(Object key, V defaultValue) {
        int index = getIndex(key);
        if (index < 0) return defaultValue;
        return getValueAt(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        int index = getIndex(key);
        if (index < 0) return null;
        return getValueAt(index);
    }

    @Override
    public boolean containsKey(Object key) {
        return getUnsafe(key, SENTINEL) != SENTINEL;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsValue(Object value) {
        for (int i = 0; i < rawSize(); i++)
            if (!isRemoved(i) && Objects.equals(getValueAt(i), value))
                return true;
        return false;
    }

    @Override
    public Entry<K, V> getEntryAt(int index) {
        Preconditions.checkElementIndex(index, rawSize());
        return new ListEntry(index);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        int size = rawSize();
        for (int i = 0; i < size; i++)
            if (!isRemoved(i))
                action.accept(getKeyAt(i), getValueAt(i));
    }

    @Override
    public V removeAt(int index) {
        throw new UnsupportedOperationException("modification not supported: " + this);
    }

    @Override
    public V setValueAt(int index, V value) {
        throw new UnsupportedOperationException("modification not supported: " + this);
    }

    @Override
    public boolean isRemoved(int index) {
        return false;
    }

    @Override
    public int rawSize() {
        return size();
    }

    @Override
    public Object getUnsafe(Object key, Object defaultValue) {
        int index = getIndex(key);
        if (index < 0) return defaultValue;
        return getValueAt(index);
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("modification not supported: " + this);
    }

    @Override
    public V remove(Object key) {
        int index = getIndex(key);
        if (index < 0) return null;
        return removeAt(index);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("modification not supported: " + this);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public ListSet<K> keySet() {
        return new KeysView();
    }

    @Override
    public Collection<V> values() {
        return new ValuesView();
    }

    @Override
    public ListSet<Entry<K, V>> entrySet() {
        return new EntriesView();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Map) || size() != ((Map) o).size()) return false;

        for (Entry<?, ?> entry : ((Map<?, ?>) o).entrySet())
            if (!Objects.equals(getUnsafe(entry.getKey(), SENTINEL), entry.getValue()))
                return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < rawSize(); i++)
            if (!isRemoved(i))
                hash += Objects.hashCode(getKeyAt(i)) ^ Objects.hashCode(getValueAt(i));
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append('{');
        boolean first = true;
        for (int i = 0; i < rawSize(); i++) {
            if (isRemoved(i)) continue;

            if (!first)
                sb.append(", ");
            first = false;
            sb.append(getKeyAt(i)).append('=').append(getValueAt(i));
        }
        return sb.append('}').toString();
    }

    private class ValuesView extends ListCollectionBase<V> implements Serializable {
        @Override
        public V getEntryAt(int index) {
            return getValueAt(index);
        }

        @Override
        public void clear() {
            ListMapBase.this.clear();
        }

        @Override
        public void removeAt(int index) {
            ListMapBase.this.removeAt(index);
        }

        @Override
        public boolean isRemoved(int index) {
            return ListMapBase.this.isRemoved(index);
        }

        @Override
        public int rawSize() {
            return ListMapBase.this.rawSize();
        }

        @Override
        public int size() {
            return ListMapBase.this.size();
        }
    }

    private class KeysView extends ListSetBase<K> implements Serializable {
        @Override
        public int getIndex(Object key) {
            return ListMapBase.this.getIndex(key);
        }

        @Override
        public K getEntryAt(int index) {
            return getKeyAt(index);
        }

        @Override
        public void clear() {
            ListMapBase.this.clear();
        }

        @Override
        public void removeAt(int index) {
            ListMapBase.this.removeAt(index);
        }

        @Override
        public boolean isRemoved(int index) {
            return ListMapBase.this.isRemoved(index);
        }

        @Override
        public int rawSize() {
            return ListMapBase.this.rawSize();
        }

        @Override
        public int size() {
            return ListMapBase.this.size();
        }
    }

    private class EntriesView extends ListSetBase<Entry<K, V>> implements Serializable {
        @Override
        public int getIndex(Object key) {
            if (!(key instanceof Entry<?, ?>))
                return -1;
            Entry<?, ?> entry = (Entry<?, ?>) key;
            int index = ListMapBase.this.getIndex(entry.getKey());
            if (index < 0 || Objects.equals(getValueAt(index), entry.getValue()))
                return index;
            return -1;
        }

        @Override
        public Entry<K, V> getEntryAt(int index) {
            return ListMapBase.this.getEntryAt(index);
        }

        @Override
        public void clear() {
            ListMapBase.this.clear();
        }

        @Override
        public void removeAt(int index) {
            ListMapBase.this.removeAt(index);
        }

        @Override
        public boolean isRemoved(int index) {
            return ListMapBase.this.isRemoved(index);
        }

        @Override
        public int rawSize() {
            return ListMapBase.this.rawSize();
        }

        @Override
        public int size() {
            return ListMapBase.this.size();
        }
    }

    private class ListEntry implements Entry<K, V>, Serializable {
        private final int index;

        public ListEntry(int index) {
            this.index = index;
        }

        @Override
        public K getKey() {
            return getKeyAt(index);
        }

        @Override
        public V getValue() {
            return getValueAt(index);
        }

        @Override
        public V setValue(V value) {
            return setValueAt(index, value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entry<?, ?>)) return false;
            Entry<?, ?> that = (Entry<?, ?>) o;
            return Objects.equals(getKey(), that.getKey()) && Objects.equals(getValue(), that.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getKeyAt(index)) ^ Objects.hashCode(getValueAt(index));
        }

        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }
    }
}