package org.ta4j.core.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.ta4j.core.num.DecimalNumFactory.Companion.instance
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
        array.addLast(instance.one())
        array.addLast(instance.two())
        array.addLast(instance.three())

        assertThat(array[0]).isEqualTo(instance.one())
        assertThat(array[1]).isEqualTo(instance.two())
        assertThat(array[2]).isEqualTo(instance.three())

        array.addLast(instance.thousand())
        assertThat(array[0]).isEqualTo(instance.thousand())
        assertThat(array[1]).isEqualTo(instance.two())
        assertThat(array[2]).isEqualTo(instance.three())

        array.addLast(instance.minusOne())
        assertThat(array[0]).isEqualTo(instance.thousand())
        assertThat(array[1]).isEqualTo(instance.minusOne())
        assertThat(array[2]).isEqualTo(instance.three())

        array.addLast(instance.hundred())
        assertThat(array[0]).isEqualTo(instance.thousand())
        assertThat(array[1]).isEqualTo(instance.minusOne())
        assertThat(array[2]).isEqualTo(instance.hundred())
    }
}
