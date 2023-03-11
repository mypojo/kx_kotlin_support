package net.kotlinx.module1.reflect

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.core1.string.toSnakeFromCamel
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * DDB 간단한 리플렉션 도구
 * enhanced 매퍼가 없다..  레거시 호환을 위해 만듬
 * */
object DynamoReflectionUtil {

    /**
     * 데이터 클래스를 Attribute Map 으로 변환
     * json 으로 중간단계 변환하는 방법은 사용하지 않는다.
     *  */
    fun <T : Any> toAttributeMap(obj: T, snakeFromCamel: Boolean = true): Map<String, AttributeValue> {

        val kClass = (obj::class as KClass<T>)
        //data 클래스로 간주하며, 생성자에 없다면 포함시키지 않는다.
        val constructors = kClass.primaryConstructor!!.parameters.map { it.name }.toSet()

        return kClass.memberProperties.map { prop ->
            if(prop.name !in constructors)  return@map null
            val key = if (snakeFromCamel) prop.name.toSnakeFromCamel() else prop.name
            val value = prop.get(obj)
            if (key == null) return@map null

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
}
