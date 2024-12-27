
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Lukáš Kvídera
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

package org.ta4j.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ta4j.core.api.callback.EntrySignalListener;
import org.ta4j.core.api.callback.ExitSignalListener;
import org.ta4j.core.api.strategy.Rule;
import org.ta4j.core.api.strategy.RuntimeContext;
import org.ta4j.core.api.strategy.Strategy;
import org.ta4j.core.api.strategy.StrategyFactory;
import org.ta4j.core.indicators.IndicatorContextUpdateListener;
import org.ta4j.core.indicators.IndicatorContexts;
import org.ta4j.core.indicators.TimeFrame;

/**
 * @author Lukáš Kvídera
 */
public class ObservableStrategyFactoryBuilder {

  private final List<EntrySignalListener> entrySignalListeners = new ArrayList<>(1);
  private final List<ExitSignalListener> exitSignalListeners = new ArrayList<>(1);
  private StrategyFactory<Strategy> strategyFactory;


  public ObservableStrategyFactoryBuilder withEntryListener(final EntrySignalListener entrySignalListener) {
    this.entrySignalListeners.add(entrySignalListener);
    return this;
  }


  public ObservableStrategyFactoryBuilder withExitListener(final ExitSignalListener exitSignalListener) {
    this.exitSignalListeners.add(exitSignalListener);
    return this;
  }


  public ObservableStrategyFactoryBuilder withStrategyFactory(final StrategyFactory<Strategy> strategyFactory) {
    this.strategyFactory = strategyFactory;
    return this;
  }


  public StrategyFactory<Strategy> build() {
    return new StrategyFactory<>() {

      @Override
      public TradeType getTradeType() {
        return ObservableStrategyFactoryBuilder.this.strategyFactory.getTradeType();
      }


      @Override
      public ObservableStrategy createStrategy(
          final RuntimeContext runtimeContext,
          final IndicatorContexts indicatorContexts
      ) {
        final var observableStrategy = new ObservableStrategy(
            ObservableStrategyFactoryBuilder.this.strategyFactory.createStrategy(runtimeContext, indicatorContexts),
            ObservableStrategyFactoryBuilder.this.entrySignalListeners,
            ObservableStrategyFactoryBuilder.this.exitSignalListeners
        );
        indicatorContexts.register(observableStrategy);
        return observableStrategy;
      }
    };
  }


  public static class ObservableStrategy implements Strategy, IndicatorContextUpdateListener {

    private final List<EntrySignalListener> entrySignalListeners;
    private final List<ExitSignalListener> exitSignalListeners;
    private final Strategy strategy;
    private Instant lastCallTime = Instant.MIN;


    private ObservableStrategy(
        final Strategy strategy,
        final List<EntrySignalListener> entrySignalListeners,
        final List<ExitSignalListener> exitSignalListeners
    ) {
      this.strategy = strategy;
      this.entrySignalListeners = entrySignalListeners;
      this.exitSignalListeners = exitSignalListeners;
    }


    @Override
    public String name() {
      return this.strategy.name();
    }


    @Override
    public Set<TimeFrame> timeFrames() {
      return this.strategy.timeFrames();
    }


    @Override
    public Rule entryRule() {
      return this.strategy.entryRule();
    }


    @Override
    public Rule exitRule() {
      return this.strategy.exitRule();
    }


    @Override
    public boolean isStable() {
      return this.strategy.isStable();
    }


    @Override
    public void onContextUpdate(final Instant time) {
      if (time.isAfter(this.lastCallTime)) {
        this.lastCallTime = time;

        if (isStable() && entryRule().isSatisfied()) {
          this.entrySignalListeners.forEach(l -> l.onSignal(new Signal(time, name())));
        } else if (isStable() && exitRule().isSatisfied()) {
          this.exitSignalListeners.forEach(l -> l.onSignal(new Signal(time, name())));
        }
      }
    }
  }
}


