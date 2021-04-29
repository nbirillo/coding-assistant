package org.jetbrains.research.ml.coding.assistant.utils

fun <T, R : Comparable<R>> Iterable<T>.minElementsBy(selector: (T) -> R): List<T> {
    val weights = map(selector)
    val minValue = weights.minOrNull() ?: return emptyList()
    return filterIndexed { index, _ ->
        weights[index] == minValue
    }
}
