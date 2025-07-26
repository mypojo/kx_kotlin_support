package net.kotlinx.ksp.generateInterface

import kotlin.annotation.AnnotationTarget.CLASS

/**
 * KSP 인터페이스.. 일단 잘 안되서 보류
 * */
@Target(CLASS)
annotation class GenerateInterface(val name: String)