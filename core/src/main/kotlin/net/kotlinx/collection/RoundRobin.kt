package net.kotlinx.collection


/** 간단 라운드로빈 */
class RoundRobin<out T> (private val list: Iterable<T>) {

    private var delegator: Iterator<T> = list.iterator()

    /** synch 주의! */
    operator fun next(): T {
        if (!delegator.hasNext()) {
            delegator = list.iterator() //리셋
        }
        return delegator.next()
    }
}