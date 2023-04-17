package net.kotlinx.kopring.cgl

import org.springframework.util.ClassUtils


/** 대상 클래스를 리턴해준다.   */
val Class<*>.targetClass: Class<*>
    get() = when {
        ClassUtils.isCglibProxyClass(this) -> this.targetClass  //프록시라면 하나 벗겨줌
        else -> this
    }