package xyz.dowenliu.igetint.nullable

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class GetIntTest {
    @Test
    fun testGetInt() {
        val getInt = GetInt()
        assertThat(getInt).isInstanceOf(IGetInt::class.java)
        assertThat(getInt.get(1)).isEqualTo("primitive: 1")
        assertThat(getInt.get((1 as Int?))).isEqualTo("boxed: 1")
        assertThat(getInt.get(null)).isEqualTo("boxed: null")
    }
}