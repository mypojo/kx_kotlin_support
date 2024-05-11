package net.kotlinx.awscdk.component.sfnv2

import software.amazon.awscdk.services.stepfunctions.IChainable
import software.amazon.awscdk.services.stepfunctions.INextable
import software.amazon.awscdk.services.stepfunctions.State

fun List<IChainable>.join(): IChainable {
    val list = this
    //순서대로 체인을 이어준다.
    for (i in 0 until list.size - 1) {
        val current = list[i]
        val next = list[i + 1]
        (current as INextable).next(next)
    }
    return list.first() //첫번째 객체를 리턴
}

/** 캐스팅 귀찮아서 사용함. 마지막꺼 리턴 주의!! */
fun State.next(next: State): State {
    (this as INextable).next(next)
    return next
}