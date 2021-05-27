package io.plurex.pangolin.indexing

/**
 * Provides an in memory "Table" of Objects.
 *
 * An object must have a primary key, and you can define custom indexes for quickly doing lookups of matching members.
 */
class ObjectIndexer<MEMBER : Any, PRIMARY_KEY_VALUE : Any>(
    private val primaryKeyAccessor: PrimaryValueAccessor<MEMBER, PRIMARY_KEY_VALUE>,
    private val indexes: List<CustomIndexDefinition<MEMBER>>
) {

    private val primaryIndex = mutableMapOf<PRIMARY_KEY_VALUE, MEMBER>()
    private val indexesDefinitionsByName = indexes.map { it.name to it }.toMap()
    private val indexesByName = indexes.map { it.name to mutableMapOf<Any, MutableList<MEMBER>>() }.toMap()

    fun add(member: MEMBER) {
        val primaryKey = primaryKeyAccessor(member)
        if (primaryIndex.containsKey(primaryKey)) throw DuplicateObjectPrimaryKeyException()
        primaryIndex[primaryKey] = member
        indexesDefinitionsByName.values.forEach {
            val key = it.valueAccessor(member)
            val memberList = indexesByName[it.name]!!.getOrPut(key) { mutableListOf() }
            memberList.add(member)
        }
    }

    fun getBy(indexName: String, keyValue: Any): List<MEMBER> {
        return indexesByName[indexName]?.get(keyValue)?.toList() ?: emptyList()
    }

    fun remove(member: MEMBER) {
        removeByPrimary(primaryKeyAccessor(member))
    }

    fun removeByPrimary(primary: PRIMARY_KEY_VALUE) {
        val removed = primaryIndex.remove(primary)
        removed?.let {
            indexesDefinitionsByName.values.forEach { definition ->
                val key = definition.valueAccessor(removed)
                indexesByName[definition.name]?.get(key)?.removeIf { member ->
                    primaryKeyAccessor(member) == primary
                }
            }
        }
    }

    fun getByPrimary(primary: PRIMARY_KEY_VALUE): MEMBER? {
        return primaryIndex[primary]
    }

    fun clear(){
        primaryIndex.clear()
        indexesByName.values.forEach { it.clear() }
    }

}

data class CustomIndexDefinition<MEMBER : Any>(
    val name: String,
    val valueAccessor: ValueAccessor<MEMBER>
)

typealias ValueAccessor<MEMBER> = (member: MEMBER) -> Any

/**
 * Gets the value of a primary key for a member.
 */
typealias PrimaryValueAccessor<MEMBER, PRIMARY_KEY_VALUE> = (member: MEMBER) -> PRIMARY_KEY_VALUE

class DuplicateObjectPrimaryKeyException : Exception()