package net.kotlinx.core1.lib

import java.net.Inet4Address


object SystemUtil {

    val ip: String by lazy { Inet4Address.getLocalHost().hostAddress }

}