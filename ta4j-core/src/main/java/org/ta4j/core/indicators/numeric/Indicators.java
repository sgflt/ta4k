package org.ta4j.core.indicators.numeric;

import lombok.experimental.UtilityClass;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.MedianPriceIndicator;
import org.ta4j.core.indicators.helpers.TypicalPriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.numeric.adx.ADXIndicator;
import org.ta4j.core.indicators.numeric.adx.MinusDIIndicator;
import org.ta4j.core.indicators.numeric.adx.MinusDMIndicator;
import org.ta4j.core.indicators.numeric.adx.PlusDIIndicator;
import org.ta4j.core.indicators.numeric.adx.PlusDMIndicator;
import org.ta4j.core.indicators.numeric.candles.LowerShadowIndicator;
import org.ta4j.core.indicators.numeric.candles.RealBodyIndicator;
import org.ta4j.core.indicators.numeric.candles.price.ClosePriceIndicator;
import org.ta4j.core.indicators.numeric.candles.price.HighPriceIndicator;
import org.ta4j.core.indicators.numeric.candles.price.LowPriceIndicator;
import org.ta4j.core.indicators.numeric.candles.price.OpenPriceIndicator;
import org.ta4j.core.indicators.numeric.channels.bollinger.BollingerBandFacade;
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


  public static ADXIndicator adx(final BarSeries series, final int diBarCount, final int adxBarCount) {
    return new ADXIndicator(series, diBarCount, adxBarCount);
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
