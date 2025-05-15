/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2023 Ta4j Organization & respective
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
import kotlin.math.ln
import kotlin.math.pow

/**
 * Representation of [Double]. High performance, lower precision.
 */
data class DoubleNum private constructor(override val delegate: Double) : Num {
    override val numFactory = DoubleNumFactory


    override val name = "DoubleNum"

    override fun plus(augend: Num): Num {
        return if (augend.isNaN) NaN else DoubleNum(delegate + (augend as DoubleNum).delegate)
    }


    override fun minus(subtrahend: Num): Num {
        return if (subtrahend.isNaN) NaN else DoubleNum(delegate - (subtrahend as DoubleNum).delegate)
    }


    override fun times(multiplicand: Num): Num {
        return if (multiplicand.isNaN) NaN else DoubleNum(delegate * (multiplicand as DoubleNum).delegate)
    }


    override fun div(divisor: Num): Num {
        if (divisor.isNaN || divisor.isZero) {
            return NaN
        }
        val divisorD = divisor as DoubleNum
        return DoubleNum(delegate / divisorD.delegate)
    }


    override fun rem(divisor: Num): Num {
        return if (divisor.isNaN) NaN else DoubleNum(delegate % (divisor as DoubleNum).delegate)
    }


    override fun floor(): Num {
        return DoubleNum(kotlin.math.floor(delegate))
    }


    override fun ceil(): Num {
        return DoubleNum(kotlin.math.ceil(delegate))
    }


    override fun pow(n: Int): Num {
        return DoubleNum(delegate.pow(n.toDouble()))
    }


    override fun pow(n: Num): Num {
        return DoubleNum(delegate.pow(n.doubleValue()))
    }


    override fun sqrt(): Num {
        if (delegate < 0) {
            return NaN
        }
        return DoubleNum(kotlin.math.sqrt(delegate))
    }


    override fun sqrt(mathContext: MathContext): Num {
        return sqrt()
    }


    override fun abs(): Num {
        return DoubleNum(kotlin.math.abs(delegate))
    }


    override fun unaryMinus(): Num {
        return DoubleNum(-delegate)
    }


    override val isZero: Boolean
        get() = delegate == 0.0

    override val isPositive: Boolean
        get() = delegate > 0

    override val isPositiveOrZero: Boolean
        get() = delegate >= 0

    override val isNegative: Boolean
        get() = delegate < 0

    override val isNegativeOrZero: Boolean
        get() = delegate <= 0


    override fun log(): Num {
        if (delegate <= 0) {
            return NaN
        }
        return DoubleNum(ln(delegate))
    }


    override fun hashCode(): Int {
        return (delegate).hashCode()
    }


    override fun toString(): String {
        return delegate.toString()
    }


    override fun equals(other: Any?): Boolean {
        if (other !is DoubleNum) {
            return false
        }

        return kotlin.math.abs(delegate - other.delegate) < EPS
    }


    override fun compareTo(other: Num): Int {
        if (this == NaN || other === NaN) {
            return 0
        }
        val doubleNumO = other as DoubleNum
        return delegate.compareTo(doubleNumO.delegate)
    }

    companion object {
        val MINUS_ONE: DoubleNum = Companion.valueOf(-1)
        val ZERO: DoubleNum = Companion.valueOf(0)
        val ONE: DoubleNum = Companion.valueOf(1)
        val TWO: DoubleNum = Companion.valueOf(2)
        val THREE: DoubleNum = Companion.valueOf(3)
        val FIFTY: DoubleNum = Companion.valueOf(50)
        val HUNDRED: DoubleNum = Companion.valueOf(100)
        val THOUSAND: DoubleNum = Companion.valueOf(1000)

        private const val EPS = 0.00001 // precision

        /**
         * Returns a `Num` version of the given `String`.
         *
         * @param val the number
         *
         * @return the `Num`
         */
        @JvmStatic
        fun valueOf(`val`: String): DoubleNum {
            return DoubleNum(`val`.toDouble())
        }


        /**
         * Returns a `Num` version of the given `Number`.
         *
         * @param i the number
         *
         * @return the `Num`
         */
        @JvmStatic
        fun valueOf(i: Number): DoubleNum {
            return DoubleNum(i.toDouble())
        }


        /**
         * Returns a `DoubleNum` version of the given `DecimalNum`.
         *
         *
         *
         * **Warning:** The `Num` returned may have inaccuracies.
         *
         * @param val the number
         *
         * @return the `Num` whose value is equal to or approximately equal to the
         * value of `val`.
         */
        fun valueOf(`val`: DecimalNum): DoubleNum {
            return valueOf(`val`.toString())
        }


        /**
         * Returns a `Num` version of the given `int`.
         *
         * @param val the number
         *
         * @return the `Num`
         */
        @JvmStatic
        fun valueOf(`val`: Int): DoubleNum {
            return DoubleNum(`val`.toDouble())
        }


        /**
         * Returns a `Num` version of the given `long`.
         *
         * @param val the number
         *
         * @return the `Num`
         */
        fun valueOf(`val`: Long): DoubleNum {
            return DoubleNum(`val`.toDouble())
        }


        /**
         * Returns a `Num` version of the given `short`.
         *
         * @param val the number
         *
         * @return the `Num`
         */
        fun valueOf(`val`: Short): DoubleNum {
            return DoubleNum(`val`.toDouble())
        }


        /**
         * Returns a `Num` version of the given `float`.
         *
         *
         *
         * **Warning:** The `Num` returned may have inaccuracies.
         *
         * @param val the number
         *
         * @return the `Num` whose value is equal to or approximately equal to the
         * value of `val`.
         */
        fun valueOf(`val`: Float): DoubleNum {
            return DoubleNum(`val`.toDouble())
        }
    }
}
