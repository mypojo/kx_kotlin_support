package net.kotlinx.core.gson

import com.google.gson.*
import com.lectra.koson.KosonType
import mu.KotlinLogging
import net.kotlinx.core.serial.SerialJsonObj

/** 간단 변환. 없으면 빈거 리턴 */
fun String?.toGsonDataOrEmpty(): GsonData {
    if (this.isNullOrBlank()) return GsonData.empty()
    return this.toGsonData()
}

/** 간단 변환 */
fun String.toGsonData(): GsonData = GsonData.parse(this)

/** 간단 변환 */
fun KosonType.toGsonData(): GsonData = GsonData.parse(this)

/** 간단 변환 */
fun List<GsonData>.toGsonArray(): GsonData = GsonData.array().also { ar -> this.forEach { ar.add(it) } }

/**
 * 간단 변환을 문자열로 변경함
 * ex) athena array<string> 으로 캐스팅
 *  */
fun List<GsonData>.toGsonArrayAsStr(): GsonData = GsonData.array().also { ar -> this.forEach { ar.add(it.toString()) } }

/**
 * koson 은 특정값 읽기가 안됨 -> 간단 읽기 & 수정 용으로 작성
 * 편의상 불변객체 유지를 하지 않음
 *
 * kotlin의 엄격한 객체 정의와 어울리지 않음으로 로직에 가급적 사용 금지
 * 모든 이상은 예외 대신 null을 리턴함
 */
data class GsonData(val delegate: JsonElement) : Iterable<GsonData> {

    //==================================================== operator ======================================================

    /** GsonVo 리턴. null을 리턴하지 않기 때문에 get().get() 식의 체인이 가능하다.  */
    operator fun get(key: String): GsonData = when (delegate) {
        is JsonObject -> GsonData(delegate[key] ?: JsonNull.INSTANCE)
        else -> EMPTY
    }

    operator fun get(index: Int): GsonData = when (delegate) {
        is JsonArray -> GsonData(delegate[index] ?: JsonNull.INSTANCE)
        else -> EMPTY
    }

    operator fun plus(data: GsonData): GsonData = when (delegate) {

        is JsonObject -> {
            check(data.delegate is JsonObject) { "더할 데이터가 JsonObject가 아닙니다." }
            val newOne = delegate.deepCopy()
            data.delegate.asJsonObject.entrySet().forEach { e ->
                newOne.add(e.key, e.value)
            }
            GsonData(newOne)
        }

        else -> throw IllegalArgumentException("${delegate::class.simpleName} 타입은 아직 지원하지 않습니다.")
    }

    //==================================================== 일반 ======================================================


    /** primitive 는 불변이라서 여기서 수정해야함. 맘에들지 않음.. */
    fun put(key: String, value: String?) = (delegate as? JsonObject)?.addProperty(key, value)
    fun put(key: String, value: Number?) = (delegate as? JsonObject)?.addProperty(key, value)
    fun put(key: String, value: Boolean?) = (delegate as? JsonObject)?.addProperty(key, value)
    fun put(key: String, value: GsonData?) = (delegate as? JsonObject)?.add(key, value?.delegate)

    /** 삭제. */
    fun remove(key: String): GsonData? = (delegate as? JsonObject)?.remove(key)?.let { GsonData(it) }

    //==================================================== array 추가 ======================================================

    fun add(data: String) = (delegate as? JsonArray)?.add(data)
    fun add(data: Number) = (delegate as? JsonArray)?.add(data)
    fun add(data: Boolean) = (delegate as? JsonArray)?.add(data)
    fun add(data: GsonData) = (delegate as? JsonArray)?.add(data.delegate)

    /** 추가 */
    fun add(collection: Collection<GsonData>) = (delegate as? JsonArray)?.let { d -> collection.forEach { d.add(it.delegate) } }

    /** 벌크추가 */
    fun addAll(data: GsonData) = (delegate as? JsonArray)?.addAll(data.delegate as JsonArray)

    //==================================================== 기본 구현 ======================================================

    /**
     * 타입 세이프하지 않음 주의!!
     * JsonObject 의 경우 편의상 value만 리턴한다.
     *  */
    override fun iterator(): Iterator<GsonData> = when (delegate) {
        is JsonArray -> delegate.map { GsonData(it) }.iterator()
        is JsonObject -> delegate.entrySet().map { GsonData(it.value) }.iterator()  // see entryMap
        else -> emptyList<GsonData>().iterator()
    }

    /** 간단히 entrySet 변형 구현 */
    fun entryMap(): Map<String, GsonData> {
        check(delegate is JsonObject)
        return delegate.entrySet().associate { it.key to GsonData(it.value) }
    }

    override fun toString(): String = delegate.toString()

    /** 편의용 메소드 */
    fun toPreety(): String = GsonSet.GSON_PRETTY.toJson(delegate)

    /**
     * 편의용 메소드
     * */
    fun toMap(): Map<String, String?> = when (delegate) {
        is JsonObject -> entryMap().map { it.key to it.value.str }.toMap()
        is JsonArray -> throw IllegalStateException("JsonArray 는 map으로 변환될 수 없습니다.")
        is JsonPrimitive -> throw IllegalStateException("JsonPrimitive 는 map으로 변환될 수 없습니다.")
        else -> emptyMap()
    }

//==================================================== 편의용  ======================================================

    /**
     * get 호출시 null 이 무조건 아니기 때문에 빈값을 체크하기위한 let 대용으로 사용됨
     * 비어있으면 block이 실행되지 않는다.
     * ex)  gson.lett? { do.. }
     *  */
    inline fun <R> lett(block: (GsonData) -> R): R? {
        if (empty) return null
        return block(this)
    }

    val str: String?
        get() = (delegate as? JsonPrimitive)?.asString
    val long: Long?
        get() = (delegate as? JsonPrimitive)?.asLong
    val bool: Boolean?
        get() = (delegate as? JsonPrimitive)?.asBoolean

    val empty: Boolean
        get() = when (delegate) {
            is JsonObject -> delegate.size() == 0
            is JsonArray -> delegate.size() == 0
            is JsonPrimitive -> false
            is JsonNull -> true
            else -> throw IllegalStateException("${delegate::class.simpleName} is not required")
        }

    val size: Int = when (delegate) {
        is JsonObject -> delegate.size()
        is JsonArray -> delegate.size()
        is JsonPrimitive -> 1
        is JsonNull -> 0
        else -> throw IllegalStateException("${delegate::class.simpleName} is not required")
    }

    val isArray: Boolean = when (delegate) {
        is JsonArray -> true
        else -> false
    }

    /**
     * 간단버전
     * fromJsonList 의 경우 TypeTokenUtil 참고
     * */
    inline fun <reified T> fromJson(gson: Gson = GsonSet.GSON): T = gson.fromJson(delegate, T::class.java)

    companion object {

        private val log = KotlinLogging.logger {}

        /** NULL 대신 기본형을 리턴. 위험하니 수정 금지해야함!! */
        val EMPTY = GsonData(JsonNull.INSTANCE)

        /**
         * 객체형 생성. koson 사용해도 무방
         * koson 하고 자동완성이 겹쳐서 이름 변경함..
         * */
        fun obj(block: GsonData.() -> Unit = {}): GsonData = GsonData(JsonObject()).apply(block)

        /** array 생성. koson 사용해도 무방 */
        fun array(block: GsonData.() -> Unit = {}): GsonData = GsonData(JsonArray()).apply(block)

        /** 빈값 리턴 */
        fun empty(): GsonData = EMPTY

        /** json을 파싱할때 */
        fun parse(obj: Any?): GsonData {
            return try {
                when (obj) {
                    null -> EMPTY
                    is GsonData -> obj
                    is JsonElement -> GsonData(obj)
                    is SerialJsonObj -> GsonData(JsonParser.parseString(obj.toJson()))
                    else -> GsonData(JsonParser.parseString(obj.toString())) //koson 등등 다 해당
                }
            } catch (e: JsonSyntaxException) {
                log.warn { "파싱데이터 -> `$obj`" }
                throw e
            }
        }

        /** 객체로 파싱할때 */
        fun fromObj(obj: Any, gson: Gson = GsonSet.GSON): GsonData = parse(gson.toJson(obj))

    }

}