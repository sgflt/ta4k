/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective
 * authors (see AUTHORS)
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
package org.ta4j.core.indicators.numeric

import java.time.Instant
import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.helpers.CrossIndicator
import org.ta4j.core.indicators.helpers.DifferenceIndicator
import org.ta4j.core.indicators.helpers.previous.PreviousNumericValueIndicator
import org.ta4j.core.indicators.numeric.average.DoubleEMAIndicator
import org.ta4j.core.indicators.numeric.average.EMAIndicator
import org.ta4j.core.indicators.numeric.average.HMAIndicator
import org.ta4j.core.indicators.numeric.average.KAMAIndicator
import org.ta4j.core.indicators.numeric.average.LWMAIndicator
import org.ta4j.core.indicators.numeric.average.MMAIndicator
import org.ta4j.core.indicators.numeric.average.PPOIndicator
import org.ta4j.core.indicators.numeric.average.SMAIndicator
import org.ta4j.core.indicators.numeric.average.TripleEMAIndicator
import org.ta4j.core.indicators.numeric.average.WMAIndicator
import org.ta4j.core.indicators.numeric.average.ZLEMAIndicator
import org.ta4j.core.indicators.numeric.helpers.DistanceFromMAIndicator
import org.ta4j.core.indicators.numeric.helpers.GainIndicator
import org.ta4j.core.indicators.numeric.helpers.HighestValueIndicator
import org.ta4j.core.indicators.numeric.helpers.LossIndicator
import org.ta4j.core.indicators.numeric.helpers.LowestValueIndicator
import org.ta4j.core.indicators.numeric.helpers.RatioIndicator
import org.ta4j.core.indicators.numeric.helpers.RunningTotalIndicator
import org.ta4j.core.indicators.numeric.momentum.CoppockCurveIndicator
import org.ta4j.core.indicators.numeric.momentum.KSTIndicator
import org.ta4j.core.indicators.numeric.momentum.ROCIndicator
import org.ta4j.core.indicators.numeric.momentum.RSIIndicator
import org.ta4j.core.indicators.numeric.operation.BinaryOperation
import org.ta4j.core.indicators.numeric.operation.UnaryOperation
import org.ta4j.core.indicators.numeric.oscillators.CMOIndicator
import org.ta4j.core.indicators.numeric.oscillators.DPOIndicator
import org.ta4j.core.indicators.numeric.oscillators.MACDIndicator
import org.ta4j.core.indicators.numeric.oscillators.RAVIIndicator
import org.ta4j.core.indicators.numeric.oscillators.aroon.AroonDownIndicator
import org.ta4j.core.indicators.numeric.oscillators.aroon.AroonUpIndicator
import org.ta4j.core.indicators.numeric.statistics.CovarianceIndicator
import org.ta4j.core.indicators.numeric.statistics.MeanDeviationIndicator
import org.ta4j.core.indicators.numeric.statistics.SigmaIndicator
import org.ta4j.core.indicators.numeric.statistics.SimpleLinearRegressionIndicator
import org.ta4j.core.indicators.numeric.statistics.StandardDeviationIndicator
import org.ta4j.core.indicators.numeric.statistics.StandardErrorIndicator
import org.ta4j.core.indicators.numeric.statistics.VarianceIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import org.ta4j.core.strategy.rules.OverIndicatorRule
import org.ta4j.core.strategy.rules.UnderIndicatorRule

/**
 * NumericIndicator is a fluent class. It provides
 * methods to create rules and other "lightweight" indicators, using a
 * (hopefully) natural-looking and expressive series of method calls.
 *
 *
 *
 * Methods like plus(), minus() and sqrt() correspond directly to methods in the
 * `Num` interface. These methods create "lightweight" (not cached)
 * indicators to add, subtract, etc. Many methods are overloaded to accept
 * either `NumericIndicator` or `Number` arguments.
 *
 *
 *
 * Methods like sma() and ema() simply create the corresponding indicator
 * objects, (SMAIndicator or EMAIndicator, for example) with "this" as the first
 * argument. These methods usually instantiate cached objects.
 *
 *
 *
 * Another set of methods, like crossedOver() and isGreaterThan() create Rule
 * objects. These are also overloaded to accept both `NumericIndicator` and
 * `Number` arguments.
 */
abstract class NumericIndicator protected constructor(
    /**
     * Factory used for creation of this indicator that will be propagated to child indicators.
     */
    val numFactory: NumFactory,
) : Indicator<Num> {
    private var currentBeginTime: Instant = Instant.MIN

    override var value: Num = NaN
        protected set


    fun aroonUp(barCount: Int) = AroonUpIndicator(numFactory, this, barCount)

    fun aroonDown(barCount: Int) = AroonDownIndicator(numFactory, this, barCount)

    fun difference() = DifferenceIndicator(this)

    fun gain() = GainIndicator(this)

    fun loss() = LossIndicator(this)

    fun rsi(barCount: Int) = RSIIndicator(this, barCount)

    fun roc(barCount: Int) = ROCIndicator(numFactory, this, barCount)

    fun coppock(
        longRoCBarCount: Int = 14,
        shortRoCBarCount: Int = 11,
        wmaBarCount: Int = 10,
    ) = CoppockCurveIndicator(
        numFactory,
        this,
        longRoCBarCount,
        shortRoCBarCount,
        wmaBarCount,
    )

    fun ppo(shortBarCount: Int, longBarCount: Int) = PPOIndicator(
        numFactory = numFactory,
        indicator = this,
        shortBarCount = shortBarCount,
        longBarCount = longBarCount
    )

    fun cmo(barCount: Int) = CMOIndicator(numFactory, this, barCount)


    /**
     * @param other the other indicator
     *
     * @return `this + other`, rounded as necessary
     */
    fun plus(other: NumericIndicator) = BinaryOperation.sum(this, other)


    /**
     * @param n the other number
     *
     * @return `this + n`, rounded as necessary
     */
    fun plus(n: Number) = plus(createConstant(n))


    /**
     * @param other the other indicator
     *
     * @return `this - other`, rounded as necessary
     */
    fun minus(other: NumericIndicator) = BinaryOperation.difference(this, other)


    /**
     * @param n the other number
     *
     * @return `this - n`, rounded as necessary
     */
    fun minus(n: Number) = minus(createConstant(n))


    /**
     * @param other the other indicator
     *
     * @return `this * other`, rounded as necessary
     */
    fun multipliedBy(other: NumericIndicator) = BinaryOperation.product(this, other)


    /**
     * @param n the other number
     *
     * @return `this * n`, rounded as necessary
     */
    fun multipliedBy(n: Number) = multipliedBy(createConstant(n))


    /**
     * @param other the other indicator
     *
     * @return `this / other`, rounded as necessary
     */
    fun dividedBy(other: NumericIndicator) = BinaryOperation.quotient(this, other)


    /**
     * @param n the other number
     *
     * @return `this / n`, rounded as necessary
     */
    fun dividedBy(n: Number) = dividedBy(createConstant(n))


    /**
     * @param other the other indicator
     *
     * @return the smaller of `this` and `other`; if they are equal,
     * `this` is returned.
     */
    fun min(other: NumericIndicator) = BinaryOperation.min(this, other)


    /**
     * @param n the other number
     *
     * @return the smaller of `this` and `n`; if they are equal,
     * `this` is returned.
     */
    fun min(n: Number) = min(createConstant(n))


    /**
     * @param other the other indicator
     *
     * @return the greater of `this` and `other`; if they are equal,
     * `this` is returned.
     */
    fun max(other: NumericIndicator) = BinaryOperation.max(this, other)


    /**
     * @param n the other number
     *
     * @return the greater of `this` and `n`; if they are equal,
     * `this` is returned.
     */
    fun max(n: Number) = max(createConstant(n))


    /**
     * Returns an Indicator whose values are the absolute values of `this`.
     *
     * @return `abs(this)`
     */
    fun abs() = UnaryOperation.abs(this)


    /**
     * Returns an Indicator whose values are √(this).
     *
     * @return `√(this)`
     */
    fun sqrt() = UnaryOperation.sqrt(this)


    /**
     * Returns an Indicator whose values are `this * this`.
     *
     * @return `this * this`
     */
    fun squared() = this.multipliedBy(this)


    /**
     * @param barCount the time frame
     *
     * @return the [SMAIndicator] of `this`
     */
    fun sma(barCount: Int) = SMAIndicator(this, barCount)


    /**
     * @param barCount the time frame
     *
     * @return the [EMAIndicator] of `this`
     */
    fun ema(barCount: Int) = EMAIndicator(this, barCount)


    fun doubleEMA(barCount: Int) = DoubleEMAIndicator(this, barCount)


    fun tripleEma(barCount: Int) = TripleEMAIndicator(this, barCount)


    fun zlema(barCount: Int) = ZLEMAIndicator(this, barCount)


    fun mma(barCount: Int) = MMAIndicator(this, barCount)


    fun hma(barCount: Int) = HMAIndicator(this, barCount)


    fun lwma(barCount: Int) = LWMAIndicator(this, barCount)


    fun wma(barCount: Int) = WMAIndicator(this, barCount)


    fun kama(
        barCountEffectiveRatio: Int,
        barCountFast: Int,
        barCountSlow: Int,
    ) = KAMAIndicator(numFactory, this, barCountEffectiveRatio, barCountFast, barCountSlow)

    fun macd(shortBarCount: Int = 12, longBarCount: Int = 26) =
        MACDIndicator(numFactory, this, shortBarCount, longBarCount)


    fun kst(
        rcma1SMABarCount: Int = 10,
        rcma1ROCBarCount: Int = 10,
        rcma2SMABarCount: Int = 10,
        rcma2ROCBarCount: Int = 15,
        rcma3SMABarCount: Int = 10,
        rcma3ROCBarCount: Int = 20,
        rcma4SMABarCount: Int = 15,
        rcma4ROCBarCount: Int = 30,
    ) = KSTIndicator(
        numFactory,
        this,
        rcma1SMABarCount,
        rcma1ROCBarCount,
        rcma2SMABarCount,
        rcma2ROCBarCount,
        rcma3SMABarCount,
        rcma3ROCBarCount,
        rcma4SMABarCount,
        rcma4ROCBarCount,
    )

    /**
     * @param barCount the time frame
     *
     * @return the [StandardDeviationIndicator] of `this`
     */
    fun stddev(barCount: Int) = StandardDeviationIndicator(this, barCount)


    fun stderr(barCount: Int) = StandardErrorIndicator(this, barCount)


    fun meanDeviation(barCount: Int) = MeanDeviationIndicator(this, barCount)


    fun variance(barCount: Int) = VarianceIndicator(this, barCount)


    fun sigma(barCount: Int) = SigmaIndicator(this, barCount)


    fun covariance(indicator: NumericIndicator, barCount: Int) = CovarianceIndicator(this, indicator, barCount)

    fun distance() = DistanceFromMAIndicator(numFactory, this)

    fun ravi(shortSmaBarCount: Int, longSmaBarCount: Int) =
        RAVIIndicator(numFactory, this, shortSmaBarCount, longSmaBarCount)

    /**
     * @param barCount the time frame
     *
     * @return the [HighestValueIndicator] of `this`
     */
    fun highest(barCount: Int) = HighestValueIndicator(numFactory, this, barCount)


    /**
     * @param barCount the time frame
     *
     * @return the [LowestValueIndicator] of `this`
     */
    fun lowest(barCount: Int) = LowestValueIndicator(numFactory, this, barCount)


    /**
     * @return the [PreviousNumericValueIndicator] of `this` with
     * `barCount=1`
     */
    @JvmOverloads
    fun previous(barCount: Int = 1) = PreviousNumericValueIndicator(numFactory, this, barCount)

    fun dpo(barCount: Int) = DPOIndicator(numFactory, this, barCount)

    fun ratio() = RatioIndicator(this)

    /**
     * Returns sum of last barCount values
     *
     * @param barCount how many values sum up
     *
     * @return sum indicator
     */
    fun runningTotal(barCount: Int) = RunningTotalIndicator(numFactory, this, barCount)


    /**
     * Whether cross over occured in last 5 bars.
     *
     * @param other the other indicator
     *
     * @return the [CrossIndicator] of `this` and `other`
     */
    @JvmOverloads
    fun crossedOver(other: NumericIndicator, barCount: Int = 1) = CrossIndicator(this, other, barCount)


    /**
     * @param n the other number
     *
     * @return the [CrossIndicator] of `this` and `n`
     */
    fun crossedOver(n: Number) = crossedOver(createConstant(n))


    /**
     * @param other the other indicator
     *
     * @return the [CrossIndicator] of `this` and
     * `other`
     */
    fun crossedUnder(other: NumericIndicator) = CrossIndicator(other, this, 1)


    /**
     * @param n the other number
     *
     * @return he current comparison of `this` and `n`
     */
    fun crossedUnder(n: Number) = crossedUnder(createConstant(n))


    /**
     * @param other the other indicator
     *
     * @return he current comparison of `this` and `other`
     */
    fun isGreaterThan(other: NumericIndicator) = value > other.value


    /**
     * @param n the other number
     *
     * @return the current comparison of `this` and `n`
     */
    fun isGreaterThan(n: Number) = isGreaterThan(createConstant(n))


    fun isGreaterThanRule(other: NumericIndicator) = OverIndicatorRule(this, other)

    fun isGreaterThanRule(n: Number) = isGreaterThanRule(createConstant(n))


    /**
     * @param other the other indicator
     *
     * @return he current comparison of `this` and `other`
     */
    fun isLessThan(other: NumericIndicator) = value < other.value


    fun isLessThanRule(other: NumericIndicator) = UnderIndicatorRule(this, other)

    fun isLessThanRule(n: Number) = isLessThanRule(createConstant(n))


    /**
     * @param n the other number
     *
     * @return the [UnderIndicatorRule] of `this` and `n`
     */
    fun isLessThan(n: Number) = isLessThan(createConstant(n))


    private fun createConstant(n: Number) = ConstantNumericIndicator(numFactory.numOf(n))


    fun simpleLinearRegression(barCount: Int) = SimpleLinearRegressionIndicator(this, barCount)


    @Synchronized
    override fun onBar(bar: Bar) {
        if (bar.beginTime.isAfter(currentBeginTime)) {
            updateState(bar)
            currentBeginTime = bar.beginTime
        }
    }


    /**
     * Called by this class when it is the right time to update internal state.
     *
     * @param bar new [Bar] received
     */
    protected abstract fun updateState(bar: Bar)
}
