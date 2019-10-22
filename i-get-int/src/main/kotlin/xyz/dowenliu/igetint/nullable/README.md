如果 GetInt 的代码是 Kotlin 实现方可控的，Kotlin 实现方可以修改其代码，但又限制不能删除方法，
可以在`get(Integer)`方法参数加`@Nullable`注解的方式帮助 Kotlin 实现。