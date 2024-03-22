package com.homework

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

fun standardDeviation(numbers: MutableList<Double>): Double {
    val mean = numbers.average()
    val variance = numbers.map { (it - mean).pow(2) }.average()
    return sqrt(variance)
}

fun round(num: Double, decimals: Int = 5): Double =
    Math.round(num * 10.0.pow(decimals)) / 10.0.pow(decimals)

fun removeOutliers(inputArray: MutableList<Double>): MutableList<Double> {
    val sortedArray = inputArray.sorted()
    val q1 = percentile(sortedArray, 25.0)
    val q3 = percentile(sortedArray, 75.0)
    val iqr = q3 - q1
    val lowerBound = q1 - 1.5 * iqr
    val upperBound = q3 + 1.5 * iqr

    return inputArray.filter { it in lowerBound..upperBound }.toMutableList()
}

fun percentile(sortedArray: List<Double>, percentile: Double): Double {
    val index = percentile / 100.0 * (sortedArray.size - 1)
    return if (index % 1 == 0.0) {
        sortedArray[index.toInt()]
    } else {
        val lower = sortedArray[floor(index).toInt()]
        val upper = sortedArray[ceil(index).toInt()]
        (lower + upper) / 2
    }
}
