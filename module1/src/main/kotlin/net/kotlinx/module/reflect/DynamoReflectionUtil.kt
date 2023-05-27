package net.kotlinx.module.reflect

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.core.string.toSnakeFromCamel
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * DDB 간단한 리플렉션 도구
 * enhanced 매퍼가 없다..  레거시 호환을 위해 만듬. 임시로만 사용할것!!  val 문제 때문에 사용하기 까다롭다!!
 * json 으로 중간단계 변환하는 방법은 사용하지 않는다.
 * */
@Deprecated("DynamoDbMapSupport.kt 를 사용할것")
object DynamoReflectionUtil {

    /** 데이터 클래스 -> Attribute Map */
    fun <T : Any> toAttributeMap(obj: T, snakeFromCamel: Boolean = true): Map<String, AttributeValue> {

        val kClass = (obj::class as KClass<T>)
        //data 클래스로 간주하며, 생성자에 없다면 포함시키지 않는다.
        val constructors = kClass.primaryConstructor!!.parameters.map { it.name }.toSet()

        return kClass.memberProperties.map { prop ->
            if (prop.name !in constructors) return@map null
            val key = if (snakeFromCamel) prop.name.toSnakeFromCamel() else prop.name
            val value = prop.get(obj) ?: return@map null

            val kClazz: KClass<*> = prop.returnType.classifier as? KClass<*> ?: throw IllegalStateException("안되는거! $prop.returnType") //타입을 클래스로 변환 가능 (실패할 수 있음)
            val attr = when {
                kClazz.isSubclassOf(String::class) -> AttributeValue.S(value.toString())
                kClazz.isSubclassOf(Number::class) -> AttributeValue.N(value.toString())
                kClazz.isSubclassOf(Boolean::class) -> AttributeValue.Bool(value as Boolean)
                kClazz.isData -> throw IllegalArgumentException("내장 데이터 클래스 미지원")
                else -> throw IllegalArgumentException("$kClazz 클래스 미지원")
            }
            key to attr
        }.filterNotNull().toMap()
    }

//    /** Attribute Map -> 데이터 클래스 */
//    fun <T : Any> fromAttributeMap(map: Map<String, AttributeValue>, kClass: KClass<T>, snakeFromCamel: Boolean = true): T {
//
//        val constructor = to.constructors.firstOrNull { it.parameters.size == from.size }
//        checkNotNull(constructor) { "클래스 [${to.simpleName}] 에 입력인자 길이 ${from.size} 와 일치하는 생성자가 존재하지 않습니다. " }
//
//        //data 클래스로 간주하며, 생성자에 없다면 포함시키지 않는다.
//        val constructors = kClass.primaryConstructor!!.parameters.map { it.name }.toSet()
//
//        val constructorParams = kClass.memberProperties.map { prop ->
//            if (prop.name !in constructors) return@map null
//            val key = if (snakeFromCamel) prop.name.toSnakeFromCamel() else prop.name
//            val value = map[key] ?: return@map null
//
//            val kClazz: KClass<*> = prop.returnType.classifier as? KClass<*> ?: throw IllegalStateException("안되는거! $prop.returnType") //타입을 클래스로 변환 가능 (실패할 수 있음)
//            when {
//                kClazz.isSubclassOf(String::class) -> value.asS()
//                kClazz.isSubclassOf(Long::class) -> value.asN().toLong()
//                kClazz.isSubclassOf(Int::class) -> value.asN().toInt()
//                kClazz.isSubclassOf(Double::class) -> value.asN().toDouble()
//                kClazz.isSubclassOf(Boolean::class) -> value.asBool()
//                kClazz.isData -> throw IllegalArgumentException("내장 데이터 클래스 미지원")
//                else -> throw IllegalArgumentException("$kClazz 클래스 미지원")
//            }
//        }
//
//        try {
//            return constructor.call(*constructorParams.toTypedArray())
//        } catch (e: Exception) {
//            constructor.parameters.mapIndexed { index, it ->
//                val value = constructorParams[index]
//                arrayOf(it.name, it.type, value)
//            }.also {
//                listOf("이름", "타입", "값").toTextGrid(it).print()
//            }
//            throw e
//        }
//
//    }
}

