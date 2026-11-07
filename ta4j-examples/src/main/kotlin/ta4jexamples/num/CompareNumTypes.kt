/*
 * SPDX-License-Identifier: MIT
 */
package ta4jexamples.num

import java.math.MathContext
import java.time.ZonedDateTime
import kotlin.random.Random
import org.ta4j.core.api.series.Symbol
import org.ta4j.core.backtest.BacktestBarSeries
import org.ta4j.core.backtest.BacktestBarSeriesBuilder
import org.ta4j.core.num.DecimalNum
import org.ta4j.core.num.DecimalNumFactory
import org.ta4j.core.num.DoubleNumFactory
import org.ta4j.core.num.Num

object CompareNumTypes {
    private const val NUM_BARS = 10000

    @JvmStatic
    fun main(args: Array<String>) {
        val barSeriesBuilder = BacktestBarSeriesBuilder()

        val seriesD = barSeriesBuilder
            .withSymbol(Symbol("Sample Series Double    "))
            .withNumFactory(DoubleNumFactory)
            .build()

        val seriesP = barSeriesBuilder
            .withSymbol(Symbol("Sample Series DecimalNum 32"))
            .withNumFactory(DecimalNumFactory.getInstance())
            .build()

        val seriesPH = barSeriesBuilder
            .withSymbol(Symbol("Sample Series DecimalNum 256"))
            .withNumFactory(DecimalNumFactory.getInstance(256))
            .build()

        val randoms = IntArray(NUM_BARS) { Random.nextInt(80, 100) }

        randoms.forEachIndexed { i, random ->
            val date = ZonedDateTime.now().minusSeconds((NUM_BARS - i).toLong())

            listOf(seriesD, seriesP, seriesPH).forEach { series ->
                val numFactory = series.numFactory
                val bar = series.barBuilder()
                    .startTime(date.toInstant())
                    .endTime(date.plusMinutes(1).toInstant())
                    .openPrice(numFactory.numOf(random))
                    .closePrice(numFactory.numOf(random + 21))
                    .highPrice(numFactory.numOf(random + 21))
                    .lowPrice(numFactory.numOf(random - 5))
                    .build()
                series.addBar(bar)
            }
        }

        val testResultD = test(seriesD)
        val testResultP = test(seriesP)
        val testResultPH = test(seriesPH)

        val mathContext = MathContext(256)
        val d = DecimalNum.valueOf(testResultD.toString(), mathContext)
        val p = DecimalNum.valueOf(testResultP.toString(), mathContext)
        val standard = DecimalNum.valueOf(testResultPH.toString(), mathContext)
        val hundred = DecimalNum.valueOf(100, mathContext)

        println("${seriesD.symbol} error: ${(d - standard) / standard * hundred}")
        println("${seriesP.symbol} error: ${(p - standard) / standard * hundred}")
    }

    fun test(series: BacktestBarSeries): Num {
        val start = System.currentTimeMillis()

        // Simple calculation to demonstrate precision differences
        // Calculate average close price using each Num type
        var sum = series.numFactory.zero()
        for (i in 0 until series.barCount) {
            val closePrice = series.getBar(i).closePrice
            sum = sum + closePrice
        }
        val average = sum / series.numFactory.numOf(series.barCount)

        val end = System.currentTimeMillis()

        println(
            String.format(
                "[%s]\n    -Time:   %s ms.\n    -Average: %s \n    -Bars:   %s\n ",
                series.symbol, (end - start), average, series.barCount
            )
        )

        return average
    }
}
