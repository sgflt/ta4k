/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.num

object DoubleNumFactory : NumFactory {
    override fun minusOne(): Num {
        return DoubleNum.MINUS_ONE
    }


    override fun zero(): Num {
        return DoubleNum.ZERO
    }


    override fun one(): Num {
        return DoubleNum.ONE
    }


    override fun two(): Num {
        return DoubleNum.TWO
    }


    override fun three(): Num {
        return DoubleNum.THREE
    }


    override fun fifty(): Num {
        return DoubleNum.FIFTY
    }


    override fun hundred(): Num {
        return DoubleNum.HUNDRED
    }


    override fun thousand(): Num {
        return DoubleNum.THOUSAND
    }


    override fun numOf(number: Number): Num {
        return DoubleNum.valueOf(number)
    }


    override fun numOf(number: String): Num {
        return DoubleNum.valueOf(number)
    }
}
