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
package org.ta4j.core.num

import java.math.MathContext

/**
 * Ta4js definition of operations that must be fulfilled by an object that
 * should be used as base for calculations.
 *
 * @see Num
 *
 * @see DoubleNum
 *
 * @see DecimalNum
 */
interface Num : Comparable<Num> {
    /**
     * @return the delegate used from this `Num` implementation
     */
    val delegate: Number

    /**
     * @return factory that created this instance with defined precision
     */
    val numFactory: NumFactory

    /**
     * Returns the name/description of this Num implementation.
     *
     * @return the name/description
     */
    val name: String

    /**
     * Returns a `Num` whose value is `(this + augend)`.
     *
     * @param augend value to be added to this `Num`
     * @return `this + augend`, rounded as necessary
     */
    operator fun plus(augend: Num): Num

    /**
     * Returns a `Num` whose value is `(this - augend)`.
     *
     * @param subtrahend value to be subtracted from this `Num`
     * @return `this - subtrahend`, rounded as necessary
     */
    operator fun minus(subtrahend: Num): Num

    /**
     * Returns a `Num` whose value is `this * multiplicand`.
     *
     * @param multiplicand value to be multiplied by this `Num`
     * @return `this * multiplicand`, rounded as necessary
     */
    operator fun times(multiplicand: Num): Num

    /**
     * Returns a `Num` whose value is `(this / divisor)`.
     *
     * @param divisor value by which this `Num` is to be divided
     * @return `this / divisor`, rounded as necessary
     */
    operator fun div(divisor: Num): Num

    /**
     * Returns a `Num` whose value is `(this % divisor)`.
     *
     * @param divisor value by which this `Num` is to be divided
     * @return `this % divisor`, rounded as necessary
     */
    operator fun rem(divisor: Num): Num

    /**
     * Returns a `Num` whose value is rounded down to the nearest whole
     * number.
     *
     * @return `this` to whole Num rounded down
     */
    fun floor(): Num

    /**
     * Returns a `Num` whose value is rounded up to the nearest whole number.
     *
     * @return `this` to whole Num rounded up
     */
    fun ceil(): Num

    /**
     * Returns a `Num` whose value is `(this<sup>n</sup>)`.
     *
     * @param n power to raise this `Num` to.
     * @return `this<sup>n</sup>`
     */
    fun pow(n: Int): Num

    /**
     * Returns a `Num` whose value is `(this<sup>n</sup>)`.
     *
     * @param n power to raise this `Num` to.
     * @return `this<sup>n</sup>`
     */
    fun pow(n: Num): Num

    /**
     * Returns a `Num` whose value is `log(this)`.
     *
     * @return `log(this)`
     */
    fun log(): Num

    /**
     * Returns a `Num` whose value is `√(this)`.
     *
     * @return `√(this)`
     */
    fun sqrt(): Num

    /**
     * Returns a `Num` whose value is `√(this)`.
     *
     * @param mathContext to calculate.
     * @return `√(this)`
     */
    fun sqrt(mathContext: MathContext): Num

    /**
     * Returns a `Num` whose value is the absolute value of this `Num`.
     *
     * @return `abs(this)`
     */
    fun abs(): Num

    /**
     * Returns a `Num` whose value is (-this), and whose scale is
     * this.scale().
     *
     * @return `negate(this)`
     */
    operator fun unaryMinus(): Num

    /**
     * Checks if `this` is zero.
     *
     * @return true if `this == 0`, false otherwise
     */
    val isZero: Boolean

    /**
     * Checks if `this` is greater than zero.
     *
     * @return true if `this > 0`, false otherwise
     */
    val isPositive: Boolean

    /**
     * Checks if `this` is zero or greater.
     *
     * @return true if `this ≥ 0`, false otherwise
     */
    val isPositiveOrZero: Boolean

    /**
     * Checks if `this` is less than zero.
     *
     * @return true if `this < 0`, false otherwise
     */
    val isNegative: Boolean

    /**
     * Checks if `this` is zero or less.
     *
     * @return true if `this ≤ 0`, false otherwise
     */
    val isNegativeOrZero: Boolean

    /**
     * Returns true only if `this` is an instance of [NaN].
     *
     * @return false if this implementation is not [NaN]
     */
    val isNaN: Boolean
        get() = false


    /**
     * Converts this `Num` to a `double`.
     *
     * @return this `Num` converted to a `double`
     */
    fun doubleValue(): Double {
        return this.delegate.toDouble()
    }

    /**
     * Converts this `Num` to an `integer`.
     *
     * @return this `Num` converted to an `integer`
     */
    fun intValue(): Int {
        return this.delegate.toInt()
    }

    /**
     * Converts this `Num` to a `long`.
     *
     * @return this `Num` converted to a `long`
     */
    fun longValue(): Long {
        return this.delegate.toLong()
    }

    /**
     * Converts this `Num` to a `float`.
     *
     * @return this `Num` converted to a `float`
     */
    fun floatValue(): Float {
        return this.delegate.toFloat()
    }
}
