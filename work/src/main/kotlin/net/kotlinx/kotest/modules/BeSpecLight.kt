package net.kotlinx.kotest.modules

import net.kotlinx.kotest.BeSpecKoin

abstract class BeSpecLight : BeSpecKoin(MODULES) {

    companion object {
        val MODULES = listOf(
            MyBasicModule.moduleConfig(),
            MyAws1Module.moduleConfig(),
            MyJobModule.moduleConfig(),
            MyBatchStepModule.moduleConfig(),
            MyKoinTestModule.moduleConfig(),
        )
    }

}