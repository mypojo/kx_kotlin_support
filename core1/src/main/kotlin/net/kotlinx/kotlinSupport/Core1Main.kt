package net.kotlinx.kotlinSupport


fun main(){

    data class Poo(
        val name: String,
        val group: String,
        val age: Int,
    )

    data class PooDto(
        val name: String,
        val groupname: String,
        val age: Int,
    )

    val poo = Poo("영감", "asdsad", 16)
    println(poo)

//    val map = Poo::class.members.filterIsInstance<KProperty<*>>().associateBy { it.name }
//    println(map)
//    println(map.get("name")!!.getter.call(poo))


//    fun Poo.toUserViewReflection() = with(::PooDto) {
//        val propertiesByName = Poo::class.members.associateBy { it.name }
//        val kCallable = propertiesByName["Asd"]
//        kCallable.
//        callBy(parameters.associate { parameter ->
//            println(parameter)
//            parameter to when (parameter.name) {
//                PooDto::groupname.name -> group
//                else -> propertiesByName[parameter.name]?. get(this@toUserViewReflection)
//            }
//        })
//    }

    //val dto = Poo("aa", 16).toUserViewReflection()

    //println(dto)





}