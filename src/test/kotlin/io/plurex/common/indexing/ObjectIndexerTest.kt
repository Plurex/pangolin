package io.plurex.common.indexing

import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random.Default.nextInt

internal class ObjectIndexerTest {

    val valueA_One = "One"
    val valueA_Two = "Two"

    val valueB_1 = 1
    val valueB_2 = 2

    val widget_one_1 = Widget(
        id = nextInt(),
        valueA = valueA_One,
        valueB = valueB_1
    )
    val widget_one_2 = Widget(
        id = nextInt(),
        valueA = valueA_One,
        valueB = valueB_2
    )
    val widget_two_1 = Widget(
        id = nextInt(),
        valueA = valueA_Two,
        valueB = valueB_1
    )
    val widget_two_2 = Widget(
        id = nextInt(),
        valueA = valueA_Two,
        valueB = valueB_2
    )
    val allWidgets = listOf(
        widget_one_1,
        widget_one_2,
        widget_two_1,
        widget_two_2
    )

    val definitionA = CustomIndexDefinition(
        name = "valueAIndex",
        valueAccessor = { widget: Widget ->
            widget.valueA
        }
    )

    val definitionB = CustomIndexDefinition(
        name = "valueBIndex",
        valueAccessor = { widget: Widget ->
            widget.valueB
        }
    )

    val primaryKeyAccessor: PrimaryValueAccessor<Widget, Int> = { widget: Widget ->
        widget.id
    }

    @Test
    fun `add - get - remove by primary key`() {

        val testObj = ObjectIndexer(
            primaryKeyAccessor = primaryKeyAccessor,
            emptyList()
        )
        allWidgets.forEach { testObj.add(it) }

        allWidgets.forEach { assertThat(testObj.getByPrimary(it.id)).isEqualTo(it) }

        allWidgets.forEach { testObj.removeByPrimary(it.id) }

        allWidgets.forEach { assertThat(testObj.getByPrimary(it.id)).isNull() }
    }

    @Test
    fun `add - cannot add duplicate primary key`() {

        val testObj = ObjectIndexer(
            primaryKeyAccessor = primaryKeyAccessor,
            emptyList()
        )
        testObj.add(widget_one_1)

        assertThat {
            testObj.add(widget_one_1)
        }.isFailure().isInstanceOf(DuplicateObjectPrimaryKeyException::class)
    }

    @Test
    fun `custom indexes - add and remove`() {
        val testObj = ObjectIndexer(
            primaryKeyAccessor = primaryKeyAccessor,
            listOf(definitionA, definitionB)
        )

        allWidgets.forEach { testObj.add(it) }

        assertThat(testObj.getBy(definitionA.name, valueA_One)).containsExactly(widget_one_1, widget_one_2)
        assertThat(testObj.getBy(definitionB.name, valueB_2)).containsExactly(widget_one_2, widget_two_2)

        testObj.remove(widget_one_2)

        assertThat(testObj.getBy(definitionA.name, valueA_One)).containsExactly(widget_one_1)
        assertThat(testObj.getBy(definitionB.name, valueB_2)).containsExactly(widget_two_2)

    }
}

internal data class Widget(
    val id: Int,
    val valueA: String,
    val valueB: Int
)