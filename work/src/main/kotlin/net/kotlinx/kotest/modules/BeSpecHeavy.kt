package net.kotlinx.kotest.modules

import net.kotlinx.kotest.BeSpecKoin

abstract class BeSpecHeavy : BeSpecKoin(MODULES) {

    companion object {
        val MODULES = BeSpecLight.MODULES + listOf(
            AwsHeavyModule.moduleConfig(),
            DynamoLockModule.moduleConfig(),
            ResourceLockModule.moduleConfig(),
        )
    }
}