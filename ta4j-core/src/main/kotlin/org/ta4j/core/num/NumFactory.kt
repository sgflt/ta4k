/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.num

interface NumFactory {
    /**
     * @return the Num of -1
     */
    fun minusOne(): Num

    /**
     * @return the Num of 0
     */
    fun zero(): Num

    /**
     * @return the Num of 1
     */
    fun one(): Num

    /**
     * @return the Num of 2
     */
    fun two(): Num

    /**
     * @return the Num of 3
     */
    fun three(): Num

    /**
     * @return the Num of 50
     */
    fun fifty(): Num

    /**
     * @return the Num of 100
     */
    fun hundred(): Num

    /**
     * @return the Num of 1000
     */
    fun thousand(): Num

    /**
     * Transforms a [Number] into the [implementation][Num] used by this
     * bar series
     *
     * @param number a [Number] implementing object.
     * @return the corresponding value as a Num implementing object
     */
    fun numOf(number: Number): Num

    /**
     * Transforms a [Number] into the [implementation][Num] used by this
     * bar series
     *
     * @param number as string
     * @return the corresponding value as a Num implementing object
     */
    fun numOf(number: String): Num
}
