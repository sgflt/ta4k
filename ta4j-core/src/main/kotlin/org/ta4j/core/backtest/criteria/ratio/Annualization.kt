/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.backtest.criteria.ratio

/**
 * Whether a ratio is reported per bar or scaled to a year.
 */
enum class Annualization {
    /** Per bar of the equity curve, unscaled. */
    PERIOD,

    /** Per-bar ratio scaled by sqrt(bars per year), bars per year estimated from the curve's timestamps. */
    ANNUALIZED,
}
