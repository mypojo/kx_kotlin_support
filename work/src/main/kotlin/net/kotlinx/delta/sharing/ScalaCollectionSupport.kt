package net.kotlinx.delta.sharing

import scala.collection.JavaConverters
import scala.collection.immutable.Seq


/** 스칼라를 코틀린으로 */
fun <T> scala.collection.Seq<T>.toKoltinList(): List<T> = JavaConverters.asJava<T>(this).toList()

/** 코틀린을 스칼라로 */
fun <T> Collection<T>.toScalaSeq(): Seq<T>? = JavaConverters.asScalaIteratorConverter<T>(this.iterator()).asScala().toSeq()