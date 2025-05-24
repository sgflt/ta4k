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
package org.ta4j.core.num

import java.math.MathContext

/**
 * Representation of an undefined or unrepresentable value: NaN (not a number)
 *
 *
 *
 * Special behavior in methods such as:
 *
 *
 *  * [NaN.plus] => NaN
 *  * [NaN.isEqual] => true
 *  * [NaN.isPositive] => false
 *  * [NaN.isNegativeOrZero] => false
 *  * [NaN.min] => NaN
 *  * [NaN.max] => NaN
 *  * [NaN.doubleValue] => [Double.NaN]
 *  * [NaN.intValue] => throws
 * [UnsupportedOperationException]
 *
 */
object NaN : Num {
    override fun compareTo(other: Num): Int {
        return 0
    }


    override fun intValue(): Int {
        throw UnsupportedOperationException("No NaN represantation for int")
    }


    override fun longValue(): Long {
        throw UnsupportedOperationException("No NaN represantation for long")
    }


    override fun floatValue(): Float = Float.Companion.NaN


    override fun doubleValue(): Double = Double.Companion.NaN


    override val delegate = Double.NaN

    override val numFactory: NumFactory
        get() {
            return object : NumFactory {
                override fun minusOne(): Num {
                    return this@NaN
                }


                override fun zero(): Num {
                    return this@NaN
                }


                override fun one(): Num {
                    return this@NaN
                }


                override fun two(): Num {
                    return this@NaN
                }


                override fun three(): Num {
                    return this@NaN
                }


                override fun fifty(): Num {
                    return this@NaN
                }


                override fun hundred(): Num {
                    return this@NaN
                }


                override fun thousand(): Num {
                    return this@NaN
                }


                override fun numOf(number: Number): Num {
                    return this@NaN
                }


                override fun numOf(number: String): Num {
                    return this@NaN
                }
            }
        }


    override val name = "NaN"


    override fun plus(augend: Num): Num = this


    override fun minus(subtrahend: Num): Num = this


    override fun times(multiplicand: Num): Num = this


    override fun div(divisor: Num): Num = this


    override fun rem(divisor: Num): Num = this


    override fun floor(): Num = this


    override fun ceil(): Num = this


    override fun pow(n: Int): Num = this


    override fun pow(n: Num): Num = this


    override fun log(): Num = this


    override fun sqrt(): Num = this


    override fun sqrt(mathContext: MathContext): Num = this


    override fun abs(): Num = this


    override fun unaryMinus(): Num = this


    override val isZero = false

    override val isPositive = false

    override val isPositiveOrZero = false

    override val isNegative = false

    override val isNegativeOrZero = false

    override val isNaN = true

    override fun toString() = "NaN"
}
