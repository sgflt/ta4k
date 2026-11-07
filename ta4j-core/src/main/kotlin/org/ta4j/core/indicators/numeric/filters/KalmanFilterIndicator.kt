/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.filters

import org.ta4j.core.api.series.Bar
import org.ta4j.core.indicators.numeric.NumericIndicator
import org.ta4j.core.num.NaN
import org.ta4j.core.num.Num

/**
 * Scalar Kalman filter with a random-walk state model, used to smooth an indicator.
 *
 * ```
 * P⁻ = P + Q;  K = P⁻ / (P⁻ + R);  x = x + K (y - x);  P = (1 - K) P⁻
 * ```
 * The first finite value seeds `x = y, P = 1` and is then corrected like any other. A NaN input gives NaN and keeps
 * the state for later bars.
 *
 * @param processNoise Q, how fast the true level may move
 * @param measurementNoise R, how noisy the input is
 */
class KalmanFilterIndicator(
    private val indicator: NumericIndicator,
    processNoise: Number = 1e-4,
    measurementNoise: Number = 1e-3,
) : NumericIndicator(indicator.numFactory) {

    private val q = numFactory.numOf(processNoise)
    private val r = numFactory.numOf(measurementNoise)
    private var estimate: Num = NaN
    private var errorCovariance = numFactory.one()

    init {
        require(q.isPositive) { "Process noise must be positive, but was: $processNoise" }
        require(r.isPositive) { "Measurement noise must be positive, but was: $measurementNoise" }
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        val measurement = indicator.value
        if (measurement.isNaN) {
            value = NaN
            return
        }
        if (estimate.isNaN) estimate = measurement
        val predicted = errorCovariance + q
        val gain = predicted / (predicted + r)
        estimate += gain * (measurement - estimate)
        errorCovariance = (numFactory.one() - gain) * predicted
        value = estimate
    }

    override val lag = 1

    override val isStable: Boolean
        get() = !estimate.isNaN && indicator.isStable

    override fun toString() = "Kalman => $value"
}

/**
 * Constant-velocity Kalman filter: tracks position and velocity, observes position only.
 * The value is the corrected position; [velocity] is the estimated change per bar.
 *
 * The first finite value seeds position at the measurement and velocity at zero. A NaN input gives NaN and keeps the
 * state. Stable after two observations, when the velocity has been observed once.
 */
class KinematicKalmanFilterIndicator(
    private val indicator: NumericIndicator,
    processNoise: Number = 1e-4,
    measurementNoise: Number = 1e-3,
) : NumericIndicator(indicator.numFactory) {

    private val q = numFactory.numOf(processNoise)
    private val r = numFactory.numOf(measurementNoise)
    private var position: Num = NaN
    private var positionVariance: Num = NaN
    private var covariance: Num = NaN
    private var velocityVariance: Num = NaN
    private var observations = 0

    /** Estimated velocity (change per bar), NaN before the first observation. */
    var velocity: Num = NaN
        private set

    init {
        require(q.isPositive) { "Process noise must be positive, but was: $processNoise" }
        require(r.isPositive) { "Measurement noise must be positive, but was: $measurementNoise" }
    }

    private fun seed(measurement: Num) {
        val one = numFactory.one()
        val prior = one + q
        val gain = prior / (prior + r)
        position = measurement
        velocity = numFactory.zero()
        positionVariance = (one - gain) * prior
        covariance = numFactory.zero()
        velocityVariance = prior
    }

    private fun update(measurement: Num) {
        val one = numFactory.one()
        val two = numFactory.two()
        val predictedPosition = position + velocity
        val predictedPositionVariance = positionVariance + covariance * two + velocityVariance + q
        val predictedCovariance = covariance + velocityVariance
        val predictedVelocityVariance = velocityVariance + q

        val innovationVariance = predictedPositionVariance + r
        val positionGain = predictedPositionVariance / innovationVariance
        val velocityGain = predictedCovariance / innovationVariance
        val innovation = measurement - predictedPosition
        position = predictedPosition + positionGain * innovation
        velocity += velocityGain * innovation

        // Joseph form keeps the covariance symmetric and positive
        val oneMinusGain = one - positionGain
        positionVariance = oneMinusGain * oneMinusGain * predictedPositionVariance + positionGain * positionGain * r
        covariance = oneMinusGain * (predictedCovariance - velocityGain * predictedPositionVariance) +
            positionGain * velocityGain * r
        velocityVariance = velocityGain * velocityGain * predictedPositionVariance -
            velocityGain * predictedCovariance * two + predictedVelocityVariance + velocityGain * velocityGain * r
    }

    override fun updateState(bar: Bar) {
        indicator.onBar(bar)
        val measurement = indicator.value
        if (measurement.isNaN) {
            value = NaN
            return
        }
        if (observations == 0) seed(measurement) else update(measurement)
        ++observations
        value = position
    }

    override val lag = 2

    override val isStable: Boolean
        get() = observations >= 2 && indicator.isStable

    override fun toString() = "KinematicKalman => $value"
}
