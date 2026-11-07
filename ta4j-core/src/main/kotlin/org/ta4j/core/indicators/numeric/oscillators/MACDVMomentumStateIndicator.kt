/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.oscillators

import java.time.Instant
import org.ta4j.core.api.Indicator
import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.bool.BooleanIndicator
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.Num

/** Momentum lifecycle zones of a volatility-normalized MACD. */
enum class MACDVMomentumState {
    /** Above +150: overbought, risk of a pullback. */
    HIGH_RISK,

    /** +50..+150 */
    RALLYING_OR_RETRACING,

    /** -50..+50 */
    RANGING,

    /** -150..-50 */
    REBOUNDING_OR_REVERSING,

    /** Below -150: oversold. */
    LOW_RISK,

    /** No value yet (NaN). */
    UNDEFINED,
}

/** Zone boundaries for [MACDVMomentumState]. */
data class MACDVMomentumProfile(
    val highRisk: Number = 150,
    val rallying: Number = 50,
    val rebounding: Number = -50,
    val lowRisk: Number = -150,
) {
    fun classify(value: Num): MACDVMomentumState {
        val f = value.numFactory
        return when {
            value.isNaN -> MACDVMomentumState.UNDEFINED
            value > f.numOf(highRisk) -> MACDVMomentumState.HIGH_RISK
            value >= f.numOf(rallying) -> MACDVMomentumState.RALLYING_OR_RETRACING
            value >= f.numOf(rebounding) -> MACDVMomentumState.RANGING
            value >= f.numOf(lowRisk) -> MACDVMomentumState.REBOUNDING_OR_REVERSING
            else -> MACDVMomentumState.LOW_RISK
        }
    }
}

/**
 * Classifies a MACD-V value into a [MACDVMomentumState]; UNDEFINED while the source is unstable.
 */
class MACDVMomentumStateIndicator(
    private val macdV: NumericIndicator,
    private val profile: MACDVMomentumProfile = MACDVMomentumProfile(),
) : Indicator<MACDVMomentumState> {

    private var currentBeginTime: Instant = Instant.MIN

    override var value = MACDVMomentumState.UNDEFINED
        private set

    override fun onBar(bar: Bar) {
        if (!bar.beginTime.isAfter(currentBeginTime)) return
        currentBeginTime = bar.beginTime
        macdV.onBar(bar)
        value = if (macdV.isStable) profile.classify(macdV.value) else MACDVMomentumState.UNDEFINED
    }

    /** True while the momentum is in [state], usable as a rule via `toRule()`. */
    fun isIn(state: MACDVMomentumState): BooleanIndicator {
        val source = this
        return object : BooleanIndicator() {
            override fun updateState(bar: Bar) {
                source.onBar(bar)
                value = source.value == state
            }

            override val lag: Int
                get() = source.lag

            override val isStable: Boolean
                get() = source.isStable

            override fun toString() = "MACDVState is $state => $value"
        }
    }

    override val lag: Int
        get() = macdV.lag

    override val isStable: Boolean
        get() = macdV.isStable

    override fun toString() = "MACDVMomentumState => $value"
}
