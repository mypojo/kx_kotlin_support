package net.kotlinx.core1


data class Poo(
    val name: String,
    val group: String,
    val age: Int,
){
    var parent:Poo? = null
}

data class PooDto(
    val name: String,
    val groupname: String,
    val age: Int,
)