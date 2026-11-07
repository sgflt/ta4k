/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.mocks

import org.ta4j.core.TradeType
import org.ta4j.core.backtest.strategy.BackTestTradingRecord
import org.ta4j.core.num.Num
import org.ta4j.core.num.NumFactory
import org.ta4j.core.num.NumFactoryProvider

class MockTradingRecord(states: List<Num>, numFactory: NumFactory = NumFactoryProvider.defaultNumFactory) :
    BackTestTradingRecord(startingType = TradeType.BUY, numFactory = numFactory) {
    /*
        * Constructor. Builds a TradingRecord from a list of states. Initial state
        * value is zero. Then at each index where the state value changes, the
        * TradingRecord operates at that index.
        *
        * @param states List<Num> of state values
        */
    init {
        var lastState = 0.0
        for (i in states.indices) {
            val state = states[i].doubleValue()
            if (state != lastState) {
//               TODO this.operate(i);
            }
            lastState = state
        }
    }
}
