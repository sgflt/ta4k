/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 Ta4j Organization & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.backtest.criteria.helpers

import org.ta4j.core.backtest.Position
import org.ta4j.core.backtest.TradingRecord
import org.ta4j.core.backtest.criteria.AnalysisCriterion
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactoryProvider.defaultNumFactory

/**
 * Standard deviation criterion in percentage (also known as Coefficient of
 * Variation (CV)).
 *
 *
 *
 * Calculates the standard deviation in percentage for a Criterion.
 *
 * @see [https://www.investopedia.com/terms/c/coefficientofvariation.asp](https://www.investopedia.com/terms/c/coefficientofvariation.asp)
 */
class RelativeStandardDeviationCriterion : AnalysisCriterion {
    /**
     * If true, then the lower the criterion value the better, otherwise the higher
     * the criterion value the better. This property is only used for
     * [.betterThan].
     */
    private val lessIsBetter: Boolean

    private val standardDeviationCriterion: StandardDeviationCriterion
    private val averageCriterion: AverageCriterion


    /**
     * Constructor with [.lessIsBetter] = false.
     *
     * @param criterion the criterion from which the "relative standard deviation"
     * is calculated
     */
    constructor(criterion: AnalysisCriterion) {
        standardDeviationCriterion = StandardDeviationCriterion(criterion)
        averageCriterion = AverageCriterion(criterion)
        lessIsBetter = false
    }


    /**
     * Constructor.
     *
     * @param criterion the criterion from which the "relative standard
     * deviation" is calculated
     * @param lessIsBetter the [.lessIsBetter]
     */
    constructor(criterion: AnalysisCriterion, lessIsBetter: Boolean) {
        standardDeviationCriterion = StandardDeviationCriterion(criterion)
        averageCriterion = AverageCriterion(criterion)
        this.lessIsBetter = lessIsBetter
    }


    override fun calculate(position: Position): Num {
        val average = averageCriterion.calculate(position)
        return standardDeviationCriterion.calculate(position) / average
    }


    override fun calculate(tradingRecord: TradingRecord): Num {
        if (tradingRecord.positions.isEmpty()) {
            return defaultNumFactory.zero()
        }
        val average = averageCriterion.calculate(tradingRecord)
        return standardDeviationCriterion.calculate(tradingRecord) / average
    }
}
