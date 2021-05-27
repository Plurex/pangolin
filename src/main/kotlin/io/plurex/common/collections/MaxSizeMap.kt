package io.plurex.common.collections

class MaxSizeMap<K, V>(val max: Int) : MutableMap<K, V> {

    private val backingMap = mutableMapOf<K, V>()

    override val size
        get() = backingMap.size
    override val entries
        get() = backingMap.entries
    override val keys
        get() = backingMap.keys
    override val values
        get() = backingMap.values

    override fun containsKey(key: K) = backingMap.containsKey(key)

    override fun containsValue(value: V) = backingMap.containsValue(value)

    override fun get(key: K) = backingMap.get(key)

    override fun isEmpty() = backingMap.isEmpty()

    override fun clear() = backingMap.clear()

    override fun put(key: K, value: V): V? {
        if (size == max && !backingMap.contains(key)) {
            backingMap.remove(backingMap.keys.random())
        }
        return backingMap.put(key, value)
    }

    override fun putAll(from: Map<out K, V>) {
        from.entries.forEach{
            put(it.key, it.value)
        }
    }

    override fun remove(key: K): V? = backingMap.remove(key)


}
