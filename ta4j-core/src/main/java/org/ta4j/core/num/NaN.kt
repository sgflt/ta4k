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


    override fun floatValue(): Float {
        return Float.Companion.NaN
    }


    override fun doubleValue(): Double {
        return Double.Companion.NaN
    }


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


    override fun plus(augend: Num): Num {
        return this
    }


    override fun minus(subtrahend: Num): Num {
        return this
    }


    override fun multipliedBy(multiplicand: Num): Num {
        return this
    }


    override fun dividedBy(divisor: Num): Num {
        return this
    }


    override fun remainder(divisor: Num): Num {
        return this
    }


    override fun floor(): Num {
        return this
    }


    override fun ceil(): Num {
        return this
    }


    override fun pow(n: Int): Num {
        return this
    }


    override fun pow(n: Num): Num {
        return this
    }


    override fun log(): Num {
        return this
    }


    override fun sqrt(): Num {
        return this
    }


    override fun sqrt(mathContext: MathContext): Num {
        return this
    }


    override fun abs(): Num {
        return this
    }


    override fun negate(): Num {
        return this
    }


    override val isZero = false

    override val isPositive = false

    override val isPositiveOrZero = false

    override val isNegative = false

    override val isNegativeOrZero = false


    /**
     * **Warning:** This method returns `true` if `this` and
     * `obj` are both [.NaN].
     *
     * @param other the other value, not null
     *
     * @return false if both values are not [.NaN]; true otherwise.
     */
    override fun isEqual(other: Num): Boolean {
        return other == this
    }


    override fun isGreaterThan(other: Num): Boolean {
        return false
    }


    override fun isGreaterThanOrEqual(other: Num): Boolean {
        return false
    }


    override fun isLessThan(other: Num): Boolean {
        return false
    }


    override fun isLessThanOrEqual(other: Num): Boolean {
        return false
    }


    override fun min(other: Num): Num {
        return this
    }


    override fun max(other: Num): Num {
        return this
    }


    override val isNaN = true
}
