/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.configuration

import java.math.BigDecimal

class StrategyConfiguration : Iterable<Parameter> {
    private val parameters = HashMap<ParameterName, Number>()


    fun getInt(parameterName: ParameterName): Int? {
        val value = this.parameters[parameterName]

        if (value is Number) {
            return value.toInt()
        }

        error("Parameter $parameterName is not an instance of Number")
    }


    fun getDouble(parameterName: ParameterName): Double? {
        val value = this.parameters[parameterName]

        if (value is Number) {
            return value.toDouble()
        }

        error("Parameter $parameterName is not an instance of Number")
    }


    fun getBigDecimal(parameterName: ParameterName): BigDecimal? {
        val value = this.parameters[parameterName]

        if (value is BigDecimal) {
            return value
        }

        error("Parameter $parameterName is not an instance of BigDecimal")
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
