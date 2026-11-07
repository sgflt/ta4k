/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy.optimization

import org.ta4j.core.strategy.configuration.ParameterName

@JvmRecord
data class ParameterDescriptor(val name: ParameterName, val range: ParameterRange)
