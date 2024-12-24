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
import org.ta4j.core.num.NumFactoryProvider;

/**
 * @author Lukáš Kvídera
 */
@UtilityClass
public final class Indicators {


  public static MedianPriceIndicator medianPrice() {
    return new MedianPriceIndicator(NumFactoryProvider.getDefaultNumFactory());
  }


  public static ClosePriceIndicator closePrice() {
    return new ClosePriceIndicator(NumFactoryProvider.getDefaultNumFactory());
  }


  public static OpenPriceIndicator openPrice() {
    return new OpenPriceIndicator(NumFactoryProvider.getDefaultNumFactory());
  }


  public static LowPriceIndicator lowPrice() {
    return new LowPriceIndicator(NumFactoryProvider.getDefaultNumFactory());
  }


  public static HighPriceIndicator highPrice() {
    return new HighPriceIndicator(NumFactoryProvider.getDefaultNumFactory());
  }


  public static TypicalPriceIndicator typicalPrice() {
    return new TypicalPriceIndicator(NumFactoryProvider.getDefaultNumFactory());
  }


  /**
   * Creates a fluent version of the VolumeIndicator.
   *
   * @return a Indicators wrapped around a VolumeIndicator
   */
  public static VolumeIndicator volume() {
    return new VolumeIndicator(NumFactoryProvider.getDefaultNumFactory());
  }


  public static BollingerBandFacade bollingerBands(final int barCount, final Number k) {
    return new BollingerBandFacade(barCount, k);
  }


  public static RealBodyIndicator realBody() {
    return new RealBodyIndicator(NumFactoryProvider.getDefaultNumFactory());
  }


  public static AroonOscillatorIndicator aroonOscillator(final int barCount) {
    return new AroonOscillatorIndicator(NumFactoryProvider.getDefaultNumFactory(), barCount);
  }


  public static AroonUpIndicator aroonUp(final int barCount) {
    return new AroonUpIndicator(NumFactoryProvider.getDefaultNumFactory(), barCount);
  }


  public static AroonDownIndicator aroonDown(final int barCount) {
    return new AroonDownIndicator(NumFactoryProvider.getDefaultNumFactory(), barCount);
  }


  public static ATRIndicator atr(final int barCount) {
    return new ATRIndicator(NumFactoryProvider.getDefaultNumFactory(), barCount);
  }


  public static ADXIndicator adx(final int diBarCount, final int adxBarCount) {
    return new ADXIndicator(NumFactoryProvider.getDefaultNumFactory(), diBarCount, adxBarCount);
  }


  public static ADXIndicator adx(final int barCount) {
    return adx(barCount, barCount);
  }


  public static PlusDMIndicator plusDMI() {
    return new PlusDMIndicator(NumFactoryProvider.getDefaultNumFactory());
  }


  public static PlusDIIndicator plusDII(final int barCount) {
    return new PlusDIIndicator(NumFactoryProvider.getDefaultNumFactory(), barCount);
  }


  public static MinusDMIndicator minusDMI() {
    return new MinusDMIndicator(NumFactoryProvider.getDefaultNumFactory());
  }


  public static MinusDIIndicator minusDII(final int barCount) {
    return new MinusDIIndicator(NumFactoryProvider.getDefaultNumFactory(), barCount);
  }


  public static LowerShadowIndicator lowerShadow() {
    return new LowerShadowIndicator(NumFactoryProvider.getDefaultNumFactory());
  }


  public static AwesomeOscillatorIndicator awesomeOscillator(final int shortBarCount, final int longBarCount) {
    return new AwesomeOscillatorIndicator(Indicators.medianPrice(), shortBarCount, longBarCount);
  }
}
