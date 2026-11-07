/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.statistics

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Base for statistics over the last [barCount] pairs of values of two indicators.
 *
 * The value is NaN until the window is full and while it holds a NaN; otherwise subclasses compute it from the
 * window, oldest pair first.
 */
abstract class AbstractPairedWindowIndicator(
    protected val first: NumericIndicator,
    protected val second: NumericIndicator,
    protected val barCount: Int,
) : NumericIndicator(first.numFactory) {

    private val firstWindow = ArrayDeque<Num>(barCount)
    private val secondWindow = ArrayDeque<Num>(barCount)
    private var nanPairs = 0

    init {
        require(barCount >= 2) { "Bar count must be at least 2, but was: $barCount" }
    }

    protected abstract fun calculate(firstValues: List<Num>, secondValues: List<Num>): Num

    private fun isNaNPair(a: Num, b: Num) = a.isNaN || b.isNaN

    override fun updateState(bar: Bar) {
        first.onBar(bar)
        second.onBar(bar)
        if (firstWindow.size == barCount && isNaNPair(firstWindow.removeFirst(), secondWindow.removeFirst())) {
            --nanPairs
        }
        firstWindow.addLast(first.value)
        secondWindow.addLast(second.value)
        if (isNaNPair(first.value, second.value)) ++nanPairs

        value = if (firstWindow.size < barCount || nanPairs > 0) NaN else calculate(firstWindow, secondWindow)
    }

    override val lag: Int
        get() = barCount + maxOf(first.lag, second.lag)

    override val isStable: Boolean
        get() = firstWindow.size >= barCount && nanPairs == 0 && first.isStable && second.isStable
}

/**
 * Spearman's rank correlation: Pearson correlation of the ranks within the window, ties get the average rank.
 * NaN when either window is flat.
 */
class SpearmanRankCorrelationIndicator(first: NumericIndicator, second: NumericIndicator, barCount: Int) :
    AbstractPairedWindowIndicator(first, second, barCount) {

    private fun averageRanks(values: List<Num>): DoubleArray {
        val order = values.indices.sortedWith { a, b -> values[a].compareTo(values[b]) }
        val ranks = DoubleArray(values.size)
        var start = 0
        while (start < order.size) {
            var end = start
            while (end + 1 < order.size && values[order[end + 1]].compareTo(values[order[start]]) == 0) ++end
            val rank = (start + end) / 2.0 + 1
            for (k in start..end) ranks[order[k]] = rank
            start = end + 1
        }
        return ranks
    }

    override fun calculate(firstValues: List<Num>, secondValues: List<Num>): Num {
        val x = averageRanks(firstValues)
        val y = averageRanks(secondValues)
        val meanX = x.average()
        val meanY = y.average()
        var covariance = 0.0
        var varianceX = 0.0
        var varianceY = 0.0
        for (i in x.indices) {
            covariance += (x[i] - meanX) * (y[i] - meanY)
            varianceX += (x[i] - meanX) * (x[i] - meanX)
            varianceY += (y[i] - meanY) * (y[i] - meanY)
        }
        if (varianceX == 0.0 || varianceY == 0.0) return NaN
        // ranks are small rationals, so the double arithmetic is exact enough for any Num type
        return numFactory.numOf(covariance) / (numFactory.numOf(varianceX) * numFactory.numOf(varianceY)).sqrt()
    }

    override fun toString() = "Spearman($barCount) => $value"
}

/**
 * Kendall's tau-b rank correlation within the window: `(concordant - discordant) / sqrt(n1 * n2)`, where `n1` and
 * `n2` count the pairs that are not tied in the first and in the second indicator. NaN when either window is flat.
 */
class KendallTauIndicator(first: NumericIndicator, second: NumericIndicator, barCount: Int) :
    AbstractPairedWindowIndicator(first, second, barCount) {

    override fun calculate(firstValues: List<Num>, secondValues: List<Num>): Num {
        var concordant = 0L
        var discordant = 0L
        var firstTies = 0L
        var secondTies = 0L
        for (i in 0 until firstValues.size - 1) {
            for (j in i + 1 until firstValues.size) {
                val firstComparison = firstValues[i].compareTo(firstValues[j]).sign()
                val secondComparison = secondValues[i].compareTo(secondValues[j]).sign()
                when {
                    firstComparison == 0 && secondComparison == 0 -> Unit
                    firstComparison == 0 -> ++firstTies
                    secondComparison == 0 -> ++secondTies
                    firstComparison == secondComparison -> ++concordant
                    else -> ++discordant
                }
            }
        }
        val firstPairs = concordant + discordant + firstTies
        val secondPairs = concordant + discordant + secondTies
        if (firstPairs == 0L || secondPairs == 0L) return NaN
        return numFactory.numOf(concordant - discordant) /
            (numFactory.numOf(firstPairs) * numFactory.numOf(secondPairs)).sqrt()
    }

    private fun Int.sign() = Integer.signum(this)

    override fun toString() = "KendallTau($barCount) => $value"
}
