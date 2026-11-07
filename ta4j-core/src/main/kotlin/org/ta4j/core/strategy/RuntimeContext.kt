/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy

import org.ta4j.core.api.callback.BarListener
import org.ta4j.core.api.callback.TickListener

interface RuntimeContext : BarListener, TickListener {
    /**
     * Type-safe value resolution using visitor pattern
     *
     * @param resolver the value resolver for the specific type
     * @param <T> the type of value to resolve
     *
     * @return resolved value of type T
    </T> */
    fun <T> getValue(resolver: RuntimeValueResolver<T?>): T?

    fun getValue(key: String): Any?
}
