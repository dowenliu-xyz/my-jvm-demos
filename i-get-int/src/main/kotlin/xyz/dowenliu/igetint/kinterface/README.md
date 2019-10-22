如果 IGetInt 接口对 Kotlin 实现方是不可控的，
Kotlin 实现方无法修改 IGetInt 接口（如接口是第三方类库提供）时，
可通过实现一层中间接口 KIGetInt 来实现接口:

```kotlin
interface KIGetInt : IGetInt {
    override fun get(i: Int?): String
}
```
