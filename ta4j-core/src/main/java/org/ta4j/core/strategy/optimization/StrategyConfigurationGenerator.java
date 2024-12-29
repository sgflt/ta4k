/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2024 Ta4j Organization & respective authors (see AUTHORS)
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

package org.ta4j.core.strategy.optimization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.ta4j.core.strategy.configuration.Parameter;
import org.ta4j.core.strategy.configuration.ParameterName;
import org.ta4j.core.strategy.configuration.StrategyConfiguration;


public class StrategyConfigurationGenerator {

  private final Map<ParameterName, ParameterRange> parameterDescriptors = new HashMap<>();


  public StrategyConfigurationGenerator(final List<ParameterDescriptor> parameterDescriptors) {
    parameterDescriptors.forEach(
        parameterDescriptor -> this.parameterDescriptors.put(
            parameterDescriptor.name(),
            parameterDescriptor.range()
        )
    );
  }


  public StrategyConfiguration generateInitialParameters() {
    final var configuration = new StrategyConfiguration();

    for (final var parameterDescriptor : this.parameterDescriptors.entrySet()) {
      configuration.put(parameterDescriptor.getKey(), generateRandomValue(parameterDescriptor.getValue()));
    }

    return configuration;
  }


  public StrategyConfiguration generateNeighborParameters(final StrategyConfiguration current) {
    final var result = new StrategyConfiguration();

    for (final var parameter : current) {
      final var modifiedParameter = modifyParameter(parameter);
      result.put(modifiedParameter.name(), modifiedParameter.value());
    }

    return result;
  }


  public double getSpaceSize() {
    return this.parameterDescriptors.values().stream()
        .mapToDouble(ParameterRange::countOfSteps)
        .reduce(1.0, (a, b) -> a * b);
  }


  private Parameter modifyParameter(final Parameter current) {
    final var range = this.parameterDescriptors.get(current.name());
    final var newValue =
        current.value().doubleValue()
        + (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * range.step();

    // Ensure value stays within allowed range
    final var cappedValue = Math.clamp(roundToStep(newValue, range.step()), range.min(), range.max());
    return new Parameter(current.name(), cappedValue);
  }


  private double generateRandomValue(final ParameterRange range) {
    final var steps = range.countOfSteps();
    final var randomStep = ThreadLocalRandom.current().nextInt(steps + 1);
    return range.min() + (randomStep * range.step());
  }


  private double roundToStep(final double value, final double step) {
    return Math.round(value / step) * step;
  }

}
