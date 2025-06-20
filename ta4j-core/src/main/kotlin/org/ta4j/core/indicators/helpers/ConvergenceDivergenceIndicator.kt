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
package org.ta4j.core.indicators.helpers

import kotlin.math.abs
import kotlin.math.min
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.bool.IsFallingIndicator
import org.ta4j.core.indicators.bool.IsRisingIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.indicators.numeric.statistics.CorrelationCoefficientIndicator
import org.ta4j.core.indicators.numeric.statistics.SimpleLinearRegressionIndicator
import org.ta4j.core.num.Num

/**
 * Select the type of convergence or divergence.
 */
enum class ConvergenceDivergenceType {
    /**
     * Returns true for **"positiveConvergent"** when the values of the
     * ref-[NumericIndicator] and the values of the other-[NumericIndicator]
     * increase within the barCount. In short: "other" and "ref" makes
     * higher highs.
     */
    POSITIVE_CONVERGENT,

    /**
     * Returns true for **"negativeConvergent"** when the values of the
     * ref-[NumericIndicator] and the values of the other-[NumericIndicator]
     * decrease within the barCount. In short: "other" and "ref" makes
     * lower lows.
     */
    NEGATIVE_CONVERGENT,

    /**
     * Returns true for **"positiveDivergent"** when the values of the
     * ref-[NumericIndicator] increase and the values of the
     * other-[NumericIndicator] decrease within a barCount. In short:
     * "other" makes lower lows while "ref" makes higher highs.
     */
    POSITIVE_DIVERGENT,

    /**
     * Returns true for **"negativeDivergent"** when the values of the
     * ref-[NumericIndicator] decrease and the values of the
     * other-[NumericIndicator] increase within a barCount. In short:
     * "other" makes higher highs while "ref" makes lower lows.
     */
    NEGATIVE_DIVERGENT
}

/**
 * Select the type of strict convergence or divergence.
 */
enum class ConvergenceDivergenceStrictType {
    /**
     * Returns true for **"positiveConvergentStrict"** when the values of the
     * ref-[NumericIndicator] and the values of the other-[NumericIndicator]
     * increase consecutively within a barCount. In short: "other" and
     * "ref" makes strict higher highs.
     */
    POSITIVE_CONVERGENT_STRICT,

    /**
     * Returns true for **"negativeConvergentStrict"** when the values of the
     * ref-[NumericIndicator] and the values of the other-[NumericIndicator]
     * decrease consecutively within a barCount. In short: "other" and
     * "ref" makes strict lower lows.
     */
    NEGATIVE_CONVERGENT_STRICT,

    /**
     * Returns true for **"positiveDivergentStrict"** when the values of the
     * ref-[NumericIndicator] increase consecutively and the values of the
     * other-[NumericIndicator] decrease consecutively within a barCount.
     * In short: "other" makes strict higher highs and "ref" makes strict lower
     * lows.
     */
    POSITIVE_DIVERGENT_STRICT,

    /**
     * Returns true for **"negativeDivergentStrict"** when the values of the
     * ref-[NumericIndicator] decrease consecutively and the values of the
     * other-[NumericIndicator] increase consecutively within a barCount.
     * In short: "other" makes strict lower lows and "ref" makes strict higher
     * highs.
     */
    NEGATIVE_DIVERGENT_STRICT
}

/**
 * Convergence-Divergence indicator.
 *
 * Note: This indicator is currently incomplete as the calculateSlopeRel method
 * is not implemented (returns null in the original Java version).
 */
class ConvergenceDivergenceIndicator : BooleanIndicator {

    private val ref: NumericIndicator
    private val other: NumericIndicator
    private val barCount: Int
    private val type: ConvergenceDivergenceType?
    private val strictType: ConvergenceDivergenceStrictType?
    private val minStrength: Num
    private val minSlope: Num

    // Linear regression indicator for slope calculation
    private val slopeRef: SimpleLinearRegressionIndicator
    private var processedBars = 0

    // Indicators for strict mode calculations
    private val refIsRising: IsRisingIndicator
    private val refIsFalling: IsFallingIndicator
    private val otherIsRising: IsRisingIndicator
    private val otherIsFalling: IsFallingIndicator

    // Correlation indicator for non-strict calculations
    private val correlationCoefficient: CorrelationCoefficientIndicator

    /**
     * Constructor.
     *
     * The **"minStrength"** is the minimum required strength for convergence or
     * divergence and must be a number between "0.1" and "1.0":
     *
     * 0.1: very weak
     * 0.8: strong (recommended)
     * 1.0: very strong
     *
     * The **"minSlope"** is the minimum required slope for convergence or
     * divergence and must be a number between "0.1" and "1.0":
     *
     * 0.1: very unstrict
     * 0.3: strict (recommended)
     * 1.0: very strict
     *
     * @param ref the indicator
     * @param other the other indicator
     * @param barCount the time frame
     * @param type of convergence or divergence
     * @param minStrength the minimum required strength for convergence or divergence
     * @param minSlope the minimum required slope for convergence or divergence
     */
    constructor(
        ref: NumericIndicator,
        other: NumericIndicator,
        barCount: Int,
        type: ConvergenceDivergenceType? = null,
        strictType: ConvergenceDivergenceStrictType? = null,
        minStrength: Double = 0.6,
        minSlope: Double = 0.1,
    ) : super() {
        this.ref = ref
        this.other = other
        this.barCount = barCount
        this.type = type
        this.strictType = strictType
        this.minStrength = ref.numFactory.numOf(min(1.0, abs(minStrength)))
        this.minSlope = ref.numFactory.numOf(minSlope)
        this.slopeRef =
            SimpleLinearRegressionIndicator(
                ref,
                barCount,
                SimpleLinearRegressionIndicator.SimpleLinearRegressionType.SLOPE
            )

        // Initialize strict mode indicators
        this.refIsRising = IsRisingIndicator(ref.numFactory, ref, barCount)
        this.refIsFalling = IsFallingIndicator(ref.numFactory, ref, barCount)
        this.otherIsRising = IsRisingIndicator(other.numFactory, other, barCount)
        this.otherIsFalling = IsFallingIndicator(other.numFactory, other, barCount)

        // Initialize correlation indicator
        this.correlationCoefficient = CorrelationCoefficientIndicator(ref, other, barCount)
    }

    override fun updateState(bar: Bar) {
        ref.onBar(bar)
        other.onBar(bar)
        slopeRef.onBar(bar)

        // Update strict mode indicators
        refIsRising.onBar(bar)
        refIsFalling.onBar(bar)
        otherIsRising.onBar(bar)
        otherIsFalling.onBar(bar)

        // Update correlation indicator
        correlationCoefficient.onBar(bar)

        processedBars++
        value = calculate()
    }

    private fun calculate(): Boolean {
        // Check if we have enough data (need at least barCount bars)
        if (processedBars <= barCount) {
            return false
        }

        if (minStrength.isZero) {
            return false
        }

        if (type != null) {
            return when (type) {
                ConvergenceDivergenceType.POSITIVE_CONVERGENT -> calculatePositiveConvergence()
                ConvergenceDivergenceType.NEGATIVE_CONVERGENT -> calculateNegativeConvergence()
                ConvergenceDivergenceType.POSITIVE_DIVERGENT -> calculatePositiveDivergence()
                ConvergenceDivergenceType.NEGATIVE_DIVERGENT -> calculateNegativeDivergence()
            }
        } else if (strictType != null) {
            return when (strictType) {
                ConvergenceDivergenceStrictType.POSITIVE_CONVERGENT_STRICT -> calculatePositiveConvergenceStrict()
                ConvergenceDivergenceStrictType.NEGATIVE_CONVERGENT_STRICT -> calculateNegativeConvergenceStrict()
                ConvergenceDivergenceStrictType.POSITIVE_DIVERGENT_STRICT -> calculatePositiveDivergenceStrict()
                ConvergenceDivergenceStrictType.NEGATIVE_DIVERGENT_STRICT -> calculateNegativeDivergenceStrict()
            }
        }

        return false
    }

    /**
     * @return true, if strict positive convergent
     */
    private fun calculatePositiveConvergenceStrict(): Boolean {
        return refIsRising.value && otherIsRising.value
    }

    /**
     * @return true, if strict negative convergent
     */
    private fun calculateNegativeConvergenceStrict(): Boolean {
        return refIsFalling.value && otherIsFalling.value
    }

    /**
     * @return true, if positive divergent
     */
    private fun calculatePositiveDivergenceStrict(): Boolean {
        return refIsRising.value && otherIsFalling.value
    }

    /**
     * @return true, if negative divergent
     */
    private fun calculateNegativeDivergenceStrict(): Boolean {
        return refIsFalling.value && otherIsRising.value
    }

    /**
     * @return true, if positive convergent
     */
    private fun calculatePositiveConvergence(): Boolean {
        val correlation = correlationCoefficient.value
        val isConvergent = correlation >= minStrength

        val slope = calculateSlopeRel()
        val isPositive = slope >= minSlope.abs()


        return isConvergent && isPositive
    }

    /**
     * @return true, if negative convergent
     */
    private fun calculateNegativeConvergence(): Boolean {
        val isConvergent = correlationCoefficient.value >= minStrength

        val slope = calculateSlopeRel()
        val isNegative = slope <= (minSlope.abs() * ref.numFactory.minusOne())

        return isConvergent && isNegative
    }

    /**
     * @return true, if positive divergent
     */
    private fun calculatePositiveDivergence(): Boolean {
        val isDivergent = correlationCoefficient.value <= (minStrength * ref.numFactory.minusOne())

        if (isDivergent) {
            // If "isDivergent" and "ref" is positive, then "other" must be negative.
            val slope = calculateSlopeRel()
            return slope >= minSlope.abs()
        }

        return false
    }

    /**
     * @return true, if negative divergent
     */
    private fun calculateNegativeDivergence(): Boolean {
        val isDivergent = correlationCoefficient.value <= (minStrength * ref.numFactory.minusOne())

        if (isDivergent) {
            // If "isDivergent" and "ref" is positive, then "other" must be negative.
            val slope = calculateSlopeRel()
            return slope <= (minSlope.abs() * ref.numFactory.minusOne())
        }

        return false
    }

    /**
     * @return the slope from linear regression
     */
    private fun calculateSlopeRel(): Num {
        return slopeRef.value
    }

    override val lag get() = barCount

    override val isStable: Boolean
        get() = ref.isStable && other.isStable && slopeRef.isStable

    override fun toString(): String {
        return "ConvergenceDivergence($type$strictType) => $value"
    }
}
