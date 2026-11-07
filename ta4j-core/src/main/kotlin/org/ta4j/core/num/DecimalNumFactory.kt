/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.num

import java.math.MathContext
import java.math.RoundingMode

class DecimalNumFactory private constructor(precision: Int) : NumFactory {
    private val mathContext = MathContext(precision, RoundingMode.HALF_UP)


    override fun minusOne(): Num {
        return MINUS_ONE
    }


    override fun zero(): Num {
        return ZERO
    }


    override fun one(): Num {
        return ONE
    }


    override fun two(): Num {
        return TWO
    }


    override fun three(): Num {
        return THREE
    }


    override fun fifty(): Num {
        return FIFTY
    }


    override fun hundred(): Num {
        return HUNDRED
    }


    override fun thousand(): Num {
        return THOUSAND
    }


    override fun numOf(number: Number): Num {
        return numOf(number.toString())
    }


    override fun numOf(number: String): Num {
        return DecimalNum.Companion.valueOf(number, this.mathContext)
    }


    companion object {
        private val MINUS_ONE: DecimalNum = DecimalNum.valueOf(-1, MathContext(1))
        private val ZERO: DecimalNum = DecimalNum.valueOf(0, MathContext(1))
        private val ONE: DecimalNum = DecimalNum.valueOf(1, MathContext(1))
        private val TWO: DecimalNum = DecimalNum.valueOf(2, MathContext(1))
        private val THREE: DecimalNum = DecimalNum.valueOf(3, MathContext(1))
        private val FIFTY: DecimalNum = DecimalNum.valueOf(50, MathContext(2))
        private val HUNDRED: DecimalNum = DecimalNum.valueOf(100, MathContext(3))
        private val THOUSAND: DecimalNum = DecimalNum.valueOf(1000, MathContext(4))


        @JvmStatic
        @JvmOverloads
        fun getInstance(precision: Int = DecimalNum.Companion.DEFAULT_PRECISION) = DecimalNumFactory(precision)
    }
}
