package net.kotlinx.ksp.generateInterface

import kotlin.annotation.AnnotationTarget.CLASS

@Target(CLASS)
annotation class GenerateInterface(val name: String)