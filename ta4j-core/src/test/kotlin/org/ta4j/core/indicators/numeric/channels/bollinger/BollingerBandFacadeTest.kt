/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.indicators.numeric.channels.bollinger

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.ta4j.core.MarketEventTestContext
import org.ta4j.core.api.Indicators.bollingerBands
import org.ta4j.core.api.Indicators.closePrice
import org.ta4j.core.indicators.numeric.average.SMAIndicator
import org.ta4j.core.indicators.numeric.statistics.StandardDeviationIndicator
import org.ta4j.core.num.NumFactory

internal class BollingerBandFacadeTest {
    private lateinit var testContext: MarketEventTestContext


    @BeforeEach
    fun setUp() {
        testContext = MarketEventTestContext()
        testContext.withCandlePrices(
            1.0, 2.0, 3.0, 4.0, 3.0, 4.0, 5.0, 4.0, 3.0, 3.0, 4.0, 3.0, 2.0
        )
    }


    @ParameterizedTest(name = "PCB [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun pcbFromFacadeIsCorrect(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val closePriceIndicator = closePrice()

        val pcb = PercentBIndicator(closePriceIndicator, 5, 2.0)
        val pcbNumeric = bollingerBands(5, 2).percentB

        testContext.withIndicators(pcb, pcbNumeric)
            .assertIndicatorEquals(pcb, pcbNumeric)
    }


    @ParameterizedTest(name = "Middle BB [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun middleBBFromFacadeIsCorrect(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val closePriceIndicator = closePrice()
        val barCount = 3
        val sma = SMAIndicator(closePriceIndicator, 3)

        val middleBB = BollingerBandsMiddleIndicator(sma)
        val bollingerBandFacade = bollingerBands(barCount, 2)
        val middleBBNumeric = bollingerBandFacade.middle

        testContext.withIndicators(middleBB, middleBBNumeric)
            .assertIndicatorEquals(middleBB, middleBBNumeric)
    }


    @ParameterizedTest(name = "Lower BB [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun lowerBBFromFacadeIsCorrect(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val closePriceIndicator = closePrice()
        val barCount = 3
        val sma = SMAIndicator(closePriceIndicator, 3)

        val middleBB = BollingerBandsMiddleIndicator(sma)
        val standardDeviation = StandardDeviationIndicator(closePriceIndicator, barCount)
        val lowerBB = BollingerBandsLowerIndicator(middleBB, standardDeviation)

        val bollingerBandFacade = bollingerBands(barCount, 2)
        val lowerBBNumeric = bollingerBandFacade.lower

        testContext!!.withIndicators(lowerBB, lowerBBNumeric)
            .assertIndicatorEquals(lowerBB, lowerBBNumeric)
    }


    @ParameterizedTest(name = "Upper BB [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun upperBBFromFacadeIsCorrect(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val closePriceIndicator = closePrice()
        val barCount = 3
        val sma = SMAIndicator(closePriceIndicator, 3)

        val middleBB = BollingerBandsMiddleIndicator(sma)
        val standardDeviation = StandardDeviationIndicator(closePriceIndicator, barCount)
        val upperBB = BollingerBandsUpperIndicator(middleBB, standardDeviation)

        val bollingerBandFacade = bollingerBands(barCount, 2)
        val upperBBNumeric = bollingerBandFacade.upper

        testContext.withIndicators(upperBB, upperBBNumeric)
            .assertIndicatorEquals(upperBB, upperBBNumeric)
    }


    @ParameterizedTest(name = "Width of BB [{index}] {0}")
    @MethodSource("org.ta4j.core.NumFactoryTestSource#numFactories")
    fun widthBBFromFacadeIsCorrect(numFactory: NumFactory) {
        testContext.withNumFactory(numFactory)

        val closePriceIndicator = closePrice()
        val barCount = 3
        val sma = SMAIndicator(closePriceIndicator, 3)

        val middleBB = BollingerBandsMiddleIndicator(sma)
        val standardDeviation = StandardDeviationIndicator(closePriceIndicator, barCount)
        val lowerBB = BollingerBandsLowerIndicator(middleBB, standardDeviation)
        val upperBB = BollingerBandsUpperIndicator(middleBB, standardDeviation)
        val widthBB = BollingerBandWidthIndicator(upperBB, middleBB, lowerBB)

        val bollingerBandFacade = bollingerBands(barCount, 2)
        val widthBBNumeric = bollingerBandFacade.bandwidth

        testContext.withIndicators(widthBB, widthBBNumeric)
            .assertIndicatorEquals(widthBB, widthBBNumeric)
    }
}
