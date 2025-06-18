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
package org.ta4j.csv

import java.sql.Connection

/**
 * @author Lukáš Kvídera
 */
data class JdbcContext(
    val connection: Connection,
    val tableName: String = "candle",
    val timestampColumnName: String = "timestamp",
    val asset: String,
    val assetColumnName: String = "asset",
    val resolution: String,
    val resolutionColumnName: String = "resolution",
    val openColumName: String = "open",
    val highColumnName: String = "high",
    val lowColumnName: String = "low",
    val closeColumnName: String = "close",
    val volumeColumnName: String = "volume",
    val orderByColumn: String = timestampColumnName,
)
