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

package org.ta4j.core.api;

import lombok.experimental.UtilityClass;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.indicators.numeric.candles.LowerShadowIndicator;
import org.ta4j.core.indicators.numeric.candles.RealBodyIndicator;
import org.ta4j.core.indicators.numeric.candles.VolumeIndicator;
import org.ta4j.core.indicators.numeric.candles.price.ClosePriceIndicator;
import org.ta4j.core.indicators.numeric.candles.price.HighPriceIndicator;
import org.ta4j.core.indicators.numeric.candles.price.LowPriceIndicator;
import org.ta4j.core.indicators.numeric.candles.price.MedianPriceIndicator;
import org.ta4j.core.indicators.numeric.candles.price.OpenPriceIndicator;
import org.ta4j.core.indicators.numeric.candles.price.TypicalPriceIndicator;
import org.ta4j.core.indicators.numeric.channels.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.numeric.momentum.ATRIndicator;
import org.ta4j.core.indicators.numeric.momentum.adx.ADXIndicator;
import org.ta4j.core.indicators.numeric.momentum.adx.MinusDIIndicator;
import org.ta4j.core.indicators.numeric.momentum.adx.MinusDMIndicator;
import org.ta4j.core.indicators.numeric.momentum.adx.PlusDIIndicator;
import org.ta4j.core.indicators.numeric.momentum.adx.PlusDMIndicator;
import org.ta4j.core.indicators.numeric.oscilators.AwesomeOscillatorIndicator;
import org.ta4j.core.indicators.numeric.oscilators.aroon.AroonDownIndicator;
import org.ta4j.core.indicators.numeric.oscilators.aroon.AroonOscillatorIndicator;
import org.ta4j.core.indicators.numeric.oscilators.aroon.AroonUpIndicator;
import org.ta4j.core.num.NumFactory;
import org.ta4j.core.num.NumFactoryProvider;

/**
 * @author Lukáš Kvídera
 */
@UtilityClass
public final class Indicators {


  public static MedianPriceIndicator medianPrice() {
    return extended().medianPrice();
  }


  public static ClosePriceIndicator closePrice() {
    return extended().closePrice();
  }


  public static OpenPriceIndicator openPrice() {
    return extended().openPrice();
  }


  public static LowPriceIndicator lowPrice() {
    return extended().lowPrice();
  }


  public static HighPriceIndicator highPrice() {
    return extended().highPrice();
  }


  public static TypicalPriceIndicator typicalPrice() {
    return extended().typicalPrice();
  }


  public static VolumeIndicator volume() {
    return extended().volume();
  }


  public static BollingerBandFacade bollingerBands(final int barCount, final Number k) {
    return extended().bollingerBands(barCount, k);
  }


  public static RealBodyIndicator realBody() {
    return extended().realBody();
  }


  public static AroonOscillatorIndicator aroonOscillator(final int barCount) {
    return extended().aroonOscillator(barCount);
  }


  public static AroonUpIndicator aroonUp(final int barCount) {
    return extended().aroonUp(barCount);
  }


  public static AroonDownIndicator aroonDown(final int barCount) {
    return extended().aroonDown(barCount);
  }


  public static ATRIndicator atr(final int barCount) {
    return extended().atr(barCount);
  }


  public static ADXIndicator adx(final int diBarCount, final int adxBarCount) {
    return extended().adx(diBarCount, adxBarCount);
  }


  public static ADXIndicator adx(final int barCount) {
    return adx(barCount, barCount);
  }


  public static PlusDMIndicator plusDMI() {
    return extended().plusDMI();
  }


  public static PlusDIIndicator plusDII(final int barCount) {
    return extended().plusDII(barCount);
  }


  public static MinusDMIndicator minusDMI() {
    return extended().minusDMI();
  }


  public static MinusDIIndicator minusDII(final int barCount) {
    return extended().minusDII(barCount);
  }


  public static LowerShadowIndicator lowerShadow() {
    return extended().lowerShadow();
  }


  public static AwesomeOscillatorIndicator awesomeOscillator(final int shortBarCount, final int longBarCount) {
    return extended().awesomeOscillator(shortBarCount, longBarCount);
  }


  private static ExtendedIndicatorFactory extended() {
    return extended(NumFactoryProvider.getDefaultNumFactory());
  }


  public static ExtendedIndicatorFactory extended(final NumFactory numFactory) {
    return new ExtendedIndicatorFactory(numFactory);
  }


  public static class ExtendedIndicatorFactory {
    private final NumFactory numFactory;


    public ExtendedIndicatorFactory(final NumFactory numFactory) {
      this.numFactory = numFactory;
    }


    public MedianPriceIndicator medianPrice() {
      return new MedianPriceIndicator(this.numFactory);
    }


    public ClosePriceIndicator closePrice() {
      return new ClosePriceIndicator(this.numFactory);
    }


    public OpenPriceIndicator openPrice() {
      return new OpenPriceIndicator(this.numFactory);
    }


    public LowPriceIndicator lowPrice() {
      return new LowPriceIndicator(this.numFactory);
    }


    public HighPriceIndicator highPrice() {
      return new HighPriceIndicator(this.numFactory);
    }


    public TypicalPriceIndicator typicalPrice() {
      return new TypicalPriceIndicator(this.numFactory);
    }


    public VolumeIndicator volume() {
      return new VolumeIndicator(this.numFactory);
    }


    public BollingerBandFacade bollingerBands(final int barCount, final Number k) {
      return new BollingerBandFacade(closePrice(), barCount, k);
    }


    public RealBodyIndicator realBody() {
      return new RealBodyIndicator(this.numFactory);
    }


    public AroonOscillatorIndicator aroonOscillator(final int barCount) {
      return new AroonOscillatorIndicator(this.numFactory, barCount);
    }


    public AroonUpIndicator aroonUp(final int barCount) {
      return new AroonUpIndicator(this.numFactory, barCount);
    }


    public AroonDownIndicator aroonDown(final int barCount) {
      return new AroonDownIndicator(this.numFactory, barCount);
    }


    public ATRIndicator atr(final int barCount) {
      return new ATRIndicator(this.numFactory, barCount);
    }


    public ADXIndicator adx(final int diBarCount, final int adxBarCount) {
      return new ADXIndicator(this.numFactory, diBarCount, adxBarCount);
    }


    public ADXIndicator adx(final int barCount) {
      return adx(barCount, barCount);
    }


    public PlusDMIndicator plusDMI() {
      return new PlusDMIndicator(this.numFactory);
    }


    public PlusDIIndicator plusDII(final int barCount) {
      return new PlusDIIndicator(this.numFactory, barCount);
    }


    public MinusDMIndicator minusDMI() {
      return new MinusDMIndicator(this.numFactory);
    }


    public MinusDIIndicator minusDII(final int barCount) {
      return new MinusDIIndicator(this.numFactory, barCount);
    }


    public LowerShadowIndicator lowerShadow() {
      return new LowerShadowIndicator(this.numFactory);
    }


    public AwesomeOscillatorIndicator awesomeOscillator(
        final NumericIndicator indicator,
        final int shortBarCount,
        final int longBarCount
    ) {
      return new AwesomeOscillatorIndicator(this.numFactory, indicator, shortBarCount, longBarCount);
    }


    public AwesomeOscillatorIndicator awesomeOscillator(final int shortBarCount, final int longBarCount) {
      return awesomeOscillator(medianPrice(), shortBarCount, longBarCount);
    }

  }
}
