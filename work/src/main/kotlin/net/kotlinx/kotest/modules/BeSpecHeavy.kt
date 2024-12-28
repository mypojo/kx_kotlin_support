package net.kotlinx.kotest.modules

import net.kotlinx.kotest.BeSpecKoin

abstract class BeSpecHeavy : BeSpecKoin(MODULES) {

    companion object {
        val MODULES = BeSpecLight.MODULES + listOf(
            DynamoLockModule.moduleConfig(),
            ResourceLockModule.moduleConfig(),
        )
    }
}