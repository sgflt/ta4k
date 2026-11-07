/*
 * SPDX-License-Identifier: MIT
 */
package org.ta4j.core.strategy

/**
 * Interface for type-safe runtime value resolution
 *
 * @param <T> the type of value to resolve
</T> */
fun interface RuntimeValueResolver<T> {
    /**
     * Resolves a value from a RuntimeContext
     *
     * @param context the context to resolve from
     *
     * @return the resolved value, or null if not available
     */
    fun resolve(context: RuntimeContext): T?
}
