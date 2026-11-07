/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.api.series

import org.ta4j.core.indicators.TimeFrame

class WrongTimeFrameException(seriesTimeFrame: TimeFrame?, candleTimeFrame: TimeFrame?) : IllegalArgumentException(
    "candle time frame ($candleTimeFrame) does not match time frame ($seriesTimeFrame)"
)
