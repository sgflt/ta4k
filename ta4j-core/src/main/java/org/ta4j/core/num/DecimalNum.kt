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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.pow

/**
 * Representation of arbitrary precision [BigDecimal]. A `Num`
 * consists of a `BigDecimal` with arbitrary [MathContext]
 * (precision and rounding mode).
 *
 * @see BigDecimal
 *
 * @see MathContext
 *
 * @see RoundingMode
 *
 * @see Num
 */
class DecimalNum : Num {
    /**
     * Returns the underlying [MathContext] mathContext.
     *
     * @return MathContext of this instance
     */
    val mathContext: MathContext
    override val delegate: BigDecimal


    /**
     * Constructor.
     *
     *
     *
     * Constructs the most precise `Num`, because it converts a `String`
     * to a `Num` with a precision of [.DEFAULT_PRECISION]; only a
     * string parameter can accurately represent a value.
     *
     * @param val the string representation of the Num value
     */
    private constructor(`val`: String) {
        delegate = BigDecimal(`val`)
        val precision = kotlin.math.max(delegate.precision().toDouble(), DEFAULT_PRECISION.toDouble()).toInt()
        mathContext = MathContext(precision, RoundingMode.HALF_UP)
    }


    /**
     * Constructor.
     *
     *
     *
     * Constructs a more precise `Num` than from `double`, because it
     * converts a `String` to a `Num` with a precision of
     * `precision`; only a string parameter can accurately represent a value.
     *
     * @param val the string representation of the Num value
     * @param mathContext the int precision of the Num value
     */
    private constructor(`val`: String, mathContext: MathContext) {
        this.mathContext = mathContext
        delegate = BigDecimal(`val`, mathContext)
    }


    private constructor(`val`: Short, mathContext: MathContext) {
        this.mathContext = mathContext
        delegate = BigDecimal(`val`.toInt(), mathContext)
    }


    private constructor(`val`: Int, mathContext: MathContext) {
        this.mathContext = mathContext
        delegate = BigDecimal.valueOf(`val`.toLong())
    }


    private constructor(`val`: Long, mathContext: MathContext) {
        this.mathContext = mathContext
        delegate = BigDecimal.valueOf(`val`).round(mathContext)
    }


    private constructor(`val`: Float, mathContext: MathContext) {
        this.mathContext = mathContext
        delegate = BigDecimal.valueOf(`val`.toDouble()).round(mathContext)
    }


    private constructor(`val`: Double, mathContext: MathContext) {
        this.mathContext = mathContext
        delegate = BigDecimal.valueOf(`val`).round(mathContext)
    }


    private constructor(`val`: BigDecimal, mathContext: MathContext) {
        this.mathContext = mathContext
        delegate = Objects.requireNonNull<BigDecimal?>(`val`).round(mathContext)
    }


    override val numFactory: NumFactory by lazy { DecimalNumFactory.getInstance(mathContext.getPrecision()) }
    override val name: String
        get() = delegate.toString()


    override fun plus(augend: Num): Num {
        if (augend.isNaN) {
            return NaN
        }
        val decimalNum = augend as DecimalNum
        val sumContext: MathContext = chooseMathContextWithGreaterPrecision(decimalNum, this)
        val result = delegate.add(decimalNum.delegate, sumContext)
        return DecimalNum(result, sumContext)
    }


    /**
     * Returns a `Num` whose value is `(this - augend)`, with rounding
     * according to the context settings.
     *
     * @see BigDecimal.subtract
     */
    override fun minus(subtrahend: Num): Num {
        if (subtrahend.isNaN) {
            return NaN
        }
        val decimalNum = subtrahend as DecimalNum
        val subContext: MathContext = chooseMathContextWithGreaterPrecision(decimalNum, this)
        val result = delegate.subtract(decimalNum.delegate, subContext)
        return DecimalNum(result, subContext)
    }


    /**
     * Returns a `Num` whose value is `this * multiplicand`, with
     * rounding according to the context settings.
     *
     * @see BigDecimal.multiply
     */
    override fun multipliedBy(multiplicand: Num): Num {
        if (multiplicand.isNaN) {
            return NaN
        }
        val decimalNum = multiplicand as DecimalNum
        val multiplicationContext: MathContext =
            chooseMathContextWithGreaterPrecision(decimalNum, this)
        val result = delegate.multiply(decimalNum.delegate, multiplicationContext)
        return DecimalNum(result, multiplicationContext)
    }


    /**
     * Returns a `Num` whose value is `(this / divisor)`, with rounding
     * according to the context settings.
     *
     * @see BigDecimal.divide
     */
    override fun dividedBy(divisor: Num): Num {
        if (divisor.isNaN || divisor.isZero == true) {
            return NaN
        }
        val decimalNum = divisor as DecimalNum
        val divisionMathContext: MathContext =
            chooseMathContextWithGreaterPrecision(decimalNum, this)
        val result = delegate.divide(decimalNum.delegate, divisionMathContext)
        return DecimalNum(result, divisionMathContext)
    }


    /**
     * Returns a `Num` whose value is `(this % divisor)`, with rounding
     * according to the context settings.
     *
     * @see BigDecimal.remainder
     */
    override fun remainder(divisor: Num): Num {
        if (divisor.isNaN) {
            return NaN
        }
        val decimalNum = divisor as DecimalNum
        val moduloContext: MathContext = chooseMathContextWithGreaterPrecision(decimalNum, this)
        val result = delegate.remainder(decimalNum.delegate, moduloContext)
        return DecimalNum(result, moduloContext)
    }


    override fun floor(): Num {
        return DecimalNum(delegate.setScale(0, RoundingMode.FLOOR), mathContext)
    }


    override fun ceil(): Num {
        return DecimalNum(delegate.setScale(0, RoundingMode.CEILING), mathContext)
    }


    /**
     * @see BigDecimal.pow
     */
    override fun pow(n: Int): Num {
        val result = delegate.pow(n, mathContext)
        return DecimalNum(result, mathContext)
    }


    /**
     * Returns a `Num` whose value is `√(this)` with `precision` =
     * [.DEFAULT_PRECISION].
     *
     * @see DecimalNum.sqrt
     */
    override fun sqrt(): Num {
        return sqrt(mathContext)
    }


    override fun sqrt(mathContext: MathContext): Num {
        // Handle special cases
        val comparedToZero = delegate.compareTo(BigDecimal.ZERO)
        if (comparedToZero < 0) {
            return NaN
        }
        if (comparedToZero == 0) {
            return DecimalNumFactory.instance.zero()
        }

        // Initial estimate calculation
        var estimate = calculateInitialEstimate(delegate, mathContext)

        // Newton-Raphson iteration
        var lastEstimate: BigDecimal?
        val maxIterations = mathContext.getPrecision() + 2 // Prevent infinite loops
        var iteration = 0

        do {
            lastEstimate = estimate
            // x[n+1] = (x[n] + S/x[n])/2
            estimate = delegate.divide(estimate, mathContext)
                .add(estimate)
                .divide(BigDecimal.TWO, mathContext)

            if (++iteration >= maxIterations) {
                break
            }

            // Log iteration progress if needed
            if (log.isTraceEnabled) {
                logIterationProgress(iteration, estimate, lastEstimate.subtract(estimate).abs())
            }
        } while (estimate.subtract(lastEstimate).abs().compareTo(getConvergenceThreshold(mathContext)) > 0)

        return valueOf(estimate, mathContext)
    }


    private fun calculateInitialEstimate(value: BigDecimal, mc: MathContext?): BigDecimal {
        // Use simple bit-shifting for initial estimate
        var scientificStr = value.round(MathContext(1, RoundingMode.HALF_UP))
            .toString()
            .uppercase(Locale.getDefault())

        var exponent = 0
        val matcher: Matcher = SCIENTIFIC_NOTATION_PATTERN.matcher(scientificStr)
        if (matcher.matches()) {
            exponent = matcher.group(2).toInt()
        }

        // Adjust exponent to be even
        if (exponent % 2 != 0) {
            exponent--
            scientificStr = if (scientificStr.contains("E"))
                EXPONENT_PATTERN.matcher(scientificStr).replaceFirst("E" + exponent.toString())
            else
                (scientificStr + "E" + exponent.toString())
        }

        // Initial estimate: if n is even, sqrt(a×10^n) ≈ sqrt(a)×10^(n/2)
        val initialEstimate = BigDecimal.valueOf(
            kotlin.math.sqrt(
                scientificStr.split("E".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toDouble()
            )
        )
            .scaleByPowerOfTen(exponent / 2)

        return initialEstimate.round(mc)
    }


    private fun getConvergenceThreshold(mc: MathContext): BigDecimal {
        // Set convergence threshold based on precision
        return BigDecimal.ONE.movePointLeft(mc.getPrecision() + 1)
    }


    private fun logIterationProgress(iteration: Int, estimate: BigDecimal, delta: BigDecimal?) {
        if (!log.isTraceEnabled) {
            return
        }

        val estimateStr = estimate.toString()
        val maxLength = 20
        val prefix = if (estimateStr.length > maxLength)
            estimateStr.substring(0, maxLength) + "..."
        else
            estimateStr

        log.trace(
            "x[{}] = {}, delta = {}",
            iteration,
            prefix,
            delta
        )
    }


    override fun log(): Num {
        // Algorithm: http://functions.wolfram.com/ElementaryFunctions/Log/10/
        // https://stackoverflow.com/a/6169691/6444586
        val logx: Num
        if (isNegativeOrZero) {
            return NaN
        }

        if (delegate == BigDecimal.ONE) {
            logx = valueOf(BigDecimal.ZERO, mathContext)
        } else {
            val ITER: Long = 1000
            val x = delegate.subtract(BigDecimal.ONE)
            var ret = BigDecimal(ITER + 1)
            for (i in ITER downTo 0) {
                var N = BigDecimal(i / 2 + 1).pow(2)
                N = N.multiply(x, mathContext)
                ret = N.divide(ret, mathContext)

                N = BigDecimal(i + 1)
                ret = ret.add(N, mathContext)
            }
            ret = x.divide(ret, mathContext)

            logx = valueOf(ret, mathContext)
        }
        return logx
    }


    override fun abs(): Num {
        return DecimalNum(delegate.abs(), mathContext)
    }


    override fun negate(): Num {
        return DecimalNum(delegate.negate(), mathContext)
    }


    override val isZero: Boolean
        get() = delegate.signum() == 0


    override val isPositive: Boolean
        get() = delegate.signum() > 0


    override val isPositiveOrZero: Boolean
        get() = delegate.signum() >= 0


    override val isNegative: Boolean
        get() = delegate.signum() < 0


    override val isNegativeOrZero: Boolean
        get() = delegate.signum() <= 0

    override fun isEqual(other: Num): Boolean {
        return !other.isNaN && compareTo(other) == 0
    }


    /**
     * Checks if this value matches another to a precision.
     *
     * @param other the other value, not null
     * @param precision the int precision
     *
     * @return true if this matches the specified value to a precision, false
     * otherwise
     */
    fun matches(other: Num, precision: Int): Boolean {
        val otherNum: Num = valueOf(other.toString(), mathContext)
        val thisNum: Num = valueOf(toString(), mathContext)
        if (thisNum.toString() == otherNum.toString()) {
            return true
        }
        if (log.isDebugEnabled) {
            log.debug("{} from {} does not match", thisNum, this)
            log.debug("{} from {} to precision {}", otherNum, other, precision)
        }
        return false
    }


    /**
     * Checks if this value matches another within an offset.
     *
     * @param other the other value, not null
     * @param delta the [Num] offset
     *
     * @return true if this matches the specified value within an offset, false
     * otherwise
     */
    fun matches(other: Num, delta: Num): Boolean {
        val result = minus(other)
        if (!result.isGreaterThan(delta)) {
            return true
        }
        if (log.isDebugEnabled) {
            log.debug("{} does not match", this)
            log.debug("{} within offset {}", other, delta)
        }
        return false
    }


    override fun isGreaterThan(other: Num): Boolean {
        return !other.isNaN && compareTo(other) > 0
    }


    override fun isGreaterThanOrEqual(other: Num): Boolean {
        return !other.isNaN && compareTo(other) > -1
    }


    override fun isLessThan(other: Num): Boolean {
        return !other.isNaN && compareTo(other) < 0
    }


    override fun isLessThanOrEqual(other: Num): Boolean {
        return !other.isNaN && delegate.compareTo((other as DecimalNum).delegate) < 1
    }


    override fun compareTo(other: Num): Int {
        return if (other.isNaN) 0 else delegate.compareTo((other as DecimalNum).delegate)
    }


    /**
     * @return the `Num` whose value is the smaller of this `Num` and
     * `other`. If they are equal, as defined by the
     * [compareTo][.compareTo] method, `this` is returned.
     */
    override fun min(other: Num): Num {
        return if (other.isNaN) NaN else (if (compareTo(other) <= 0) this else other)
    }


    /**
     * @return the `Num` whose value is the greater of this `Num` and
     * `other`. If they are equal, as defined by the
     * [compareTo][.compareTo] method, `this` is returned.
     */
    override fun max(other: Num): Num {
        return if (other.isNaN) NaN else (if (compareTo(other) >= 0) this else other)
    }


    override fun hashCode(): Int {
        return Objects.hash(delegate)
    }


    /**
     * **Warning:** This method returns `true` if `this` and
     * `obj` are both [NaN.NaN].
     *
     * @return true if `this` object is the same as the `obj` argument,
     * as defined by the [compareTo][.compareTo] method; false
     * otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (other !is DecimalNum) {
            return false
        }
        return delegate.compareTo(other.delegate) == 0
    }


    override fun toString(): String {
        return delegate.toString()
    }


    override fun pow(n: Num): Num {
        // There is no BigDecimal.pow(BigDecimal). We could do:
        // double Math.pow(double delegate.doubleValue(), double n)
        // But that could overflow any of the three doubles.
        // Instead perform:
        // x^(a+b) = x^a * x^b
        // Where:
        // n = a+b
        // a is a whole number (make sure it doesn't overflow int)
        // remainder 0 <= b < 1
        // So:
        // x^a uses DecimalNum ((DecimalNum) x).pow(int a) cannot overflow Num
        // x^b uses double Math.pow(double x, double b) cannot overflow double because b
        // < 1.
        // As suggested: https://stackoverflow.com/a/3590314

        // get n = a+b, same precision as n

        val aplusb = ((n as DecimalNum).delegate)
        // get the remainder 0 <= b < 1, looses precision as double
        val b = aplusb.remainder(BigDecimal.ONE)
        // bDouble looses precision
        val bDouble = b.toDouble()
        // get the whole number a
        val a = aplusb.subtract(b)
        // convert a to an int, fails on overflow
        val aInt = a.intValueExact()
        // use BigDecimal pow(int)
        val xpowa = delegate.pow(aInt)
        // use double pow(double, double)
        val xpowb = delegate.toDouble().pow(bDouble)
        // use PrecisionNum.multiply(PrecisionNum)
        val result = xpowa.multiply(BigDecimal.valueOf(xpowb))
        return DecimalNum(result, mathContext)
    }

    companion object {
        private val SCIENTIFIC_NOTATION_PATTERN: Pattern = Pattern.compile("^([+-]?\\d*\\.?\\d+)E([+-]?\\d+)$")
        private val EXPONENT_PATTERN: Pattern = Pattern.compile("E\\d+")

        const val DEFAULT_PRECISION: Int = 20
        private val log: Logger = LoggerFactory.getLogger(DecimalNum::class.java)

        /**
         * Returns a `Num` version of the given `String`.
         *
         *
         *
         * Constructs the most precise `Num`, because it converts a `String`
         * to a `Num` with a precision of [.DEFAULT_PRECISION]; only a
         * string parameter can accurately represent a value.
         *
         * @param val the number
         *
         * @return the `Num` with a precision of [.DEFAULT_PRECISION]
         *
         * @throws NumberFormatException if `val` is `"NaN"`
         */
        @JvmStatic
        fun valueOf(`val`: String): DecimalNum {
            if (`val`.equals("NAN", ignoreCase = true)) {
                throw NumberFormatException()
            }
            return DecimalNum(`val`)
        }


        /**
         * Returns a `Num` version of the given `String` with a precision of
         * `precision`.
         *
         * @param val the number
         * @param mathContext with the precision
         *
         * @return the `Num` with a precision of `precision`
         *
         * @throws NumberFormatException if `val` is `"NaN"`
         */
        @JvmStatic
        fun valueOf(`val`: String, mathContext: MathContext): DecimalNum {
            if (`val`.equals("NAN", ignoreCase = true)) {
                throw NumberFormatException()
            }
            return DecimalNum(`val`, mathContext)
        }


        /**
         * Returns a `Num` version of the given `Number`.
         *
         *
         *
         * Returns the most precise `Num`, because it first converts `val`
         * to a `String` and then to a `Num` with a precision of
         * [.DEFAULT_PRECISION]; only a string parameter can accurately represent
         * a value.
         *
         * @param val the number
         *
         * @return the `Num` with a precision of [.DEFAULT_PRECISION]
         *
         * @throws NumberFormatException if `val` is `"NaN"`
         */
        @JvmStatic
        fun valueOf(`val`: Number): DecimalNum {
            return valueOf(`val`.toString())
        }


        /**
         * Returns a `DecimalNum` version of the given `DoubleNum`.
         *
         *
         *
         * Returns the most precise `Num`, because it first converts `val`
         * to a `String` and then to a `Num` with a precision of
         * [.DEFAULT_PRECISION]; only a string parameter can accurately represent
         * a value.
         *
         * @param val the number
         *
         * @return the `Num` with a precision of [.DEFAULT_PRECISION]
         *
         * @throws NumberFormatException if `val` is `"NaN"`
         */
        @JvmStatic
        fun valueOf(`val`: DoubleNum): DecimalNum {
            return valueOf(`val`.doubleValue())
        }


        /**
         * Returns a `Num` version of the given `int`.
         *
         * @param val the number
         *
         * @return the `Num`
         */
        @JvmStatic
        fun valueOf(`val`: Int, mathContext: MathContext): DecimalNum {
            return DecimalNum(`val`, mathContext)
        }


        /**
         * Returns a `Num` version of the given `long`.
         *
         * @param val the number
         *
         * @return the `Num`
         */
        @JvmStatic
        fun valueOf(`val`: Long, mathContext: MathContext): DecimalNum {
            return DecimalNum(`val`, mathContext)
        }


        /**
         * Returns a `Num` version of the given `short`.
         *
         * @param val the number
         *
         * @return the `Num`
         */
        @JvmStatic
        fun valueOf(`val`: Short, mathContext: MathContext): DecimalNum {
            return DecimalNum(`val`, mathContext)
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
         *
         * @throws NumberFormatException if `val` is `Float.NaN`
         */
        @JvmStatic
        fun valueOf(`val`: Float, mathContext: MathContext): DecimalNum {
            if (`val`.isNaN()) {
                throw NumberFormatException()
            }
            return DecimalNum(`val`, mathContext)
        }


        /**
         * Returns a `Num` version of the given `double`.
         *
         *
         *
         * **Warning:** The `Num` returned may have inaccuracies.
         *
         * @param val the number
         *
         * @return the `Num` whose value is equal to or approximately equal to the
         * value of `val`.
         *
         * @throws NumberFormatException if `val` is `Double.NaN`
         */
        @JvmStatic
        fun valueOf(`val`: Double, mathContext: MathContext): DecimalNum {
            if (`val`.isNaN()) {
                throw NumberFormatException()
            }
            return DecimalNum(`val`, mathContext)
        }


        /**
         * Returns a `Num` version of the given `BigDecimal`.
         *
         *
         *
         * **Warning:** The `Num` returned may have inaccuracies because it
         * only inherits the precision of `val`.
         *
         * @param val the number
         *
         * @return the `Num`
         */
        @JvmStatic
        fun valueOf(`val`: BigDecimal, mathContext: MathContext): DecimalNum {
            return DecimalNum(`val`, mathContext)
        }


        /**
         * If there are operations between constant that have precision 0 and other number we need to preserve bigger
         * precision.
         *
         * If we do not provide math context that sets upper bound, BigDecimal chooses "infinity" precision, that may be too
         * much.
         *
         * @param first decimal num
         * @param second decimal num
         *
         * @return math context with bigger precision
         */
        private fun chooseMathContextWithGreaterPrecision(first: DecimalNum, second: DecimalNum): MathContext {
            val firstMathContext = first.mathContext
            val secondMathContext = second.mathContext
            return if (firstMathContext.getPrecision() > secondMathContext.getPrecision())
                firstMathContext
            else
                secondMathContext
        }
    }
}
