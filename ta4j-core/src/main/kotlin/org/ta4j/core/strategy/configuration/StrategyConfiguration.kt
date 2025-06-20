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
package org.ta4j.core.strategy.configuration

import java.math.BigDecimal
import java.util.*
import lombok.ToString
import org.ta4j.core.num.Num

@ToString
class StrategyConfiguration : Iterable<Parameter> {
    private val parameters: MutableMap<ParameterName, Number> = HashMap<ParameterName, Number>()


    fun getInt(parameterName: ParameterName): OptionalInt {
        val value = this.parameters[parameterName]

        if (value is Int) {
            return OptionalInt.of(value)
        }

        return OptionalInt.empty()
    }


    fun getDouble(parameterName: ParameterName): OptionalDouble {
        val value = this.parameters[parameterName]

        if (value is Double) {
            return OptionalDouble.of(value)
        }

        return OptionalDouble.empty()
    }


    fun getBigDecimal(parameterName: ParameterName): Optional<BigDecimal> {
        val value = this.parameters[parameterName]

        if (value is BigDecimal) {
            return Optional.of<BigDecimal>(value)
        }

        return Optional.empty<BigDecimal>()
    }


    fun getNum(parameterName: ParameterName): Optional<Num> {
        val value = this.parameters[parameterName]

        if (value is Num) {
            return Optional.of<Num>(value)
        }

        return Optional.empty<Num>()
    }


    fun put(parameterName: ParameterName, value: Number) {
        this.parameters.put(parameterName, value)
    }


    override fun iterator(): Iterator<Parameter> {
        return parameters.entries
            .map { (key, value) -> Parameter(key, value) }
            .iterator()
    }
}
