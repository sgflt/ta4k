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
package org.ta4j.core.strategy.optimization

import java.util.concurrent.ThreadLocalRandom
import org.ta4j.core.strategy.configuration.Parameter
import org.ta4j.core.strategy.configuration.StrategyConfiguration

class StrategyConfigurationGenerator(parameterDescriptors: List<ParameterDescriptor>) {
    private val parameterDescriptors = parameterDescriptors.associate { it.name to it.range }


    fun generateInitialParameters(): StrategyConfiguration {
        val configuration = StrategyConfiguration()

        for (parameterDescriptor in this.parameterDescriptors.entries) {
            configuration.put(parameterDescriptor.key, generateRandomValue(parameterDescriptor.value))
        }

        return configuration
    }


    fun generateNeighborParameters(current: StrategyConfiguration): StrategyConfiguration {
        val result = StrategyConfiguration()

        for (parameter in current) {
            val modifiedParameter = modifyParameter(parameter)
            result.put(modifiedParameter.name, modifiedParameter.value)
        }

        return result
    }


    val spaceSize: Int
        get() = parameterDescriptors.values
            .map { it.countOfSteps() }
            .reduce { a, b -> a * b }


    private fun modifyParameter(current: Parameter): Parameter {
        val range: ParameterRange = parameterDescriptors[current.name]!!
        val newValue =
            (current.value.toDouble()
                    + (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * range.step)

        // Ensure value stays within allowed range
        val cappedValue = Math.clamp(roundToStep(newValue, range.step), range.min, range.max)
        return Parameter(current.name, cappedValue)
    }


    private fun generateRandomValue(range: ParameterRange): Double {
        val steps = range.countOfSteps()
        val randomStep = ThreadLocalRandom.current().nextInt(steps + 1)
        return range.min + (randomStep * range.step)
    }


    private fun roundToStep(value: Double, step: Double): Double {
        return Math.round(value / step) * step
    }
}
