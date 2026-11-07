/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core

import org.ta4j.core.backtest.criteria.AnalysisCriterion

/**
 * Functional interface for creating AnalysisCriterion instances with parameters.
 */
fun interface CriterionFactory {
    /**
     * Applies parameters to a CriterionFactory and returns the AnalysisCriterion.
     *
     * @param params criteria parameters
     * @return AnalysisCriterion with the parameters applied
     */
    fun getCriterion(vararg params: Any): AnalysisCriterion
}