package net.kotlinx.guava

import com.google.common.eventbus.EventBus


/** post 하고 그대로 리턴 */
fun <T> EventBus.postEvent(block: () -> T): T = block().apply { post(this) }