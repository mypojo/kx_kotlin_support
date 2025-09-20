package net.kotlinx.awscdk.sfn2

private val innerCache = mutableMapOf<software.amazon.awscdk.services.stepfunctions.State, CdkSfnChain>()

var software.amazon.awscdk.services.stepfunctions.State.chain: CdkSfnChain
    get() {
        return innerCache[this]!!
    }
    set(value) {
        innerCache[this] = value
    }