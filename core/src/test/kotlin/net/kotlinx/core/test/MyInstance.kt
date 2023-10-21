package net.kotlinx.core.test

import net.kotlinx.core.id.IdGenerator
import java.util.concurrent.atomic.AtomicLong


object MyInstance {


    private val TEMP_SEQ = AtomicLong()

    val ID_GENERATOR = IdGenerator({ TEMP_SEQ.incrementAndGet() })


}
