/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.api.series

import java.time.Instant

class PastCandleParadoxException(barEndTime: Instant?, currentTime: Instant?) : IllegalArgumentException(
    ("New bar is before current bar. It is not possible in this universe. Current: "
            + currentTime
            + ", Bar: "
            + barEndTime)
)
