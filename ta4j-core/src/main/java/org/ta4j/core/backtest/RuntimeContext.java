
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

package org.ta4j.core.backtest;

import java.time.Duration;

import org.ta4j.core.num.Num;

public interface RuntimeContext {
  /**
   * @return count of currently opened positions
   */
  int getCountOfOpenedPositions();

  /**
   * @return maximum profit in currency (including costs) as sum of all opened positions at the peak.
   */
  Num getMaxTotalProfit();

  /**
   * @return maximum profit in currency (including costs) from all of opened positions. Only one position with max profit is chosen.
   */
  Num getMaxProfit();

  /**
   * @return how long we have opened first position
   */
  Duration getTimeInTrade();

  /**
   * 100 -> 90 = (100 - 90) / 100 = 0.1
   * @return in percent how much the worst position fell
   */
  Num getMaxDrawDown();

  /**
   * 100 -> 110 = 110 / 100 - 1 = 0.1
   * @return in percent how much the best position flew
   */
  Num getMaxGain();
}
