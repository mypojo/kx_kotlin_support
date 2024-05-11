package net.kotlinx.kotest.modules

import net.kotlinx.kotest.BeSpecKoin

abstract class BeSpecHeavy : BeSpecKoin(BeSpecLight.MODULES + MyAwsModule.moduleConfig())