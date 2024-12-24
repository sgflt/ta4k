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
package org.ta4j.core.api.series;

import org.ta4j.core.api.Indicator;
import org.ta4j.core.events.CandleReceived;
import org.ta4j.core.num.NumFactory;

/**
 * A {@code BarSeries} is a sequence of {@link Bar bars} separated by a
 * predefined period (e.g. 15 minutes, 1 day, etc.).
 *
 * Notably, it can be:
 *
 * <ul>
 * <li>the base of {@link Indicator indicator} calculations
 * </ul>
 */
public interface BarSeries {
    /**
     * @return factory that generates numbers usable in this BarSeries
     */
    NumFactory numFactory();

    /**
     * @return builder that generates compatible bars
     */
    BarBuilder barBuilder();

    /**
     * @return the name of the series
     */
    String getName();

    /**
     * Gets the bar from series.
     *
     * @return the bar at the current position
     */
    Bar getBar();


    /**
     * Adds the {@code bar} at the end of the series.
     *
     * @param bar the bar to be added
     */
    void addBar(Bar bar);


    void onCandle(CandleReceived event);
}
