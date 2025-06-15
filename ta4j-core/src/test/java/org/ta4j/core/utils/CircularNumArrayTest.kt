package org.ta4j.core.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.ta4j.core.num.DecimalNumFactory
import org.ta4j.core.num.NaN

class CircularNumArrayTest {
    private var array = CircularNumArray(3)

    @Test
    fun capacity() {
        assertThat(array.capacity()).isEqualTo(3)
    }

    @Test
    fun get() {
        assertThat(array[0]).isEqualTo(NaN)
        assertThat(array[1]).isEqualTo(NaN)
        assertThat(array[3]).isEqualTo(NaN)
    }

    @Test
    fun set() {
        array.addLast(DecimalNumFactory.getInstance().one())
        array.addLast(DecimalNumFactory.getInstance().two())
        array.addLast(DecimalNumFactory.getInstance().three())

        assertThat(array[0]).isEqualTo(DecimalNumFactory.getInstance().one())
        assertThat(array[1]).isEqualTo(DecimalNumFactory.getInstance().two())
        assertThat(array[2]).isEqualTo(DecimalNumFactory.getInstance().three())

        array.addLast(DecimalNumFactory.getInstance().thousand())
        assertThat(array[0]).isEqualTo(DecimalNumFactory.getInstance().thousand())
        assertThat(array[1]).isEqualTo(DecimalNumFactory.getInstance().two())
        assertThat(array[2]).isEqualTo(DecimalNumFactory.getInstance().three())

        array.addLast(DecimalNumFactory.getInstance().minusOne())
        assertThat(array[0]).isEqualTo(DecimalNumFactory.getInstance().thousand())
        assertThat(array[1]).isEqualTo(DecimalNumFactory.getInstance().minusOne())
        assertThat(array[2]).isEqualTo(DecimalNumFactory.getInstance().three())

        array.addLast(DecimalNumFactory.getInstance().hundred())
        assertThat(array[0]).isEqualTo(DecimalNumFactory.getInstance().thousand())
        assertThat(array[1]).isEqualTo(DecimalNumFactory.getInstance().minusOne())
        assertThat(array[2]).isEqualTo(DecimalNumFactory.getInstance().hundred())
    }
}
