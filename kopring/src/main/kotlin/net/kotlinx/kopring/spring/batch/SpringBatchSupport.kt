package net.kotlinx.kopring.spring.batch

import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemStream
import org.springframework.batch.item.ItemWriter

fun ItemReader<*>.closeIfAble() {
    if (this is ItemStream) close()
}

fun ItemWriter<*>.closeIfAble() {
    if (this is ItemStream) close()
}

fun ItemReader<*>.openIfAble(ex: ExecutionContext = ExecutionContext()) {
    if (this is ItemStream) open(ex)
}

fun ItemWriter<*>.openIfAble(ex: ExecutionContext = ExecutionContext()) {
    if (this is ItemStream) open(ex)
}

/** 강제 ExecutionContext 입력 */
fun ItemStream.open() = open(ExecutionContext())
