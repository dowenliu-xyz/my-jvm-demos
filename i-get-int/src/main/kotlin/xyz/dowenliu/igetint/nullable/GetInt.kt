package xyz.dowenliu.igetint.nullable


class GetInt : IGetInt {
    override fun get(i: Int?): String = "boxed: $i"

    override fun get(i: Int): String = "primitive: $i"
}