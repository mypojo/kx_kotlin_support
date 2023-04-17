package net.kotlinx.kopring.spring.batch

import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemStream


fun ItemReader<*>.closeIfAble() {
    if (this is ItemStream) close()
}

/** 강제 ExecutionContext 입력 */
fun ItemStream.open() = open(ExecutionContext())
