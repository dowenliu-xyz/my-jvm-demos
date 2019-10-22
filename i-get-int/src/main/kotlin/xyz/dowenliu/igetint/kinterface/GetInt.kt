package xyz.dowenliu.igetint.kinterface

interface KIGetInt : IGetInt {
    override fun get(i: Int?): String
}

class GetInt : KIGetInt {
    override fun get(i: Int?): String = "boxed: $i"

    override fun get(i: Int): String = "primitive: $i"
}