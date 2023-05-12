package net.kotlinx.core2.gson

import com.google.gson.*

inline fun String.toGsonData() = GsonData.parse(this)

/**
 * koson 은 특정값 읽기가 안됨 -> 간단 읽기 & 수정 용으로 작성
 * 편의상 불변객체 유지를 하지 않음
 *
 * kotlin의 엄격한 객체 정의와 어울리지 않음으로 로직에 가급적 사용 금지
 * 모든 이상은 예외 대신 null을 리턴함
 */
data class GsonData(val delegate: JsonElement) : Iterable<GsonData> {

    /** GsonVo 리턴. null을 리턴하지 않기 때문에 get().get() 식의 체인이 가능하다.  */
    operator fun get(key: String): GsonData = when (delegate) {
        is JsonObject -> GsonData(delegate[key] ?: JsonNull.INSTANCE)
        else -> EMPTY
    }

    operator fun get(index: Int): GsonData = when (delegate) {
        is JsonArray -> GsonData(delegate[index] ?: JsonNull.INSTANCE)
        else -> EMPTY
    }


    /** primitive 는 불변이라서 여기서 수정해야함. 맘에들지 않음.. */
    fun put(key: String, value: String?) = (delegate as? JsonObject)?.addProperty(key, value)
    fun put(key: String, value: Number?) = (delegate as? JsonObject)?.addProperty(key, value)
    fun put(key: String, value: Boolean?) = (delegate as? JsonObject)?.addProperty(key, value)

    /** 삭제. */
    fun remove(key: String): GsonData? = (delegate as? JsonObject)?.remove(key)?.let { GsonData(it) }

    //==================================================== 기본 구현 ======================================================

    /** 타입 세이프하지 않음 주의!! */
    override fun iterator(): Iterator<GsonData> = when (delegate) {
        is JsonArray -> delegate.map { GsonData(it) }.iterator()
        is JsonObject -> delegate.entrySet().map { GsonData(it.value) }.iterator()
        else -> emptyList<GsonData>().iterator()
    }

    override fun toString(): String = delegate.toString()

    //==================================================== 편의용  ======================================================
    val str: String?
        get() = (delegate as? JsonPrimitive)?.asString
    val long: Long?
        get() = (delegate as? JsonPrimitive)?.asLong

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

    /** 간단버전 */
    inline fun <reified T> fromJson(gson: Gson = GsonSet.GSON): T = gson.fromJson(delegate, T::class.java)

    companion object {

        /** NULL 대신 기본형을 리턴 */
        val EMPTY = GsonData(JsonNull.INSTANCE)

        fun obj(): GsonData = GsonData(JsonObject())
        fun array(): GsonData = GsonData(JsonArray())
        fun empty(): GsonData = EMPTY

        fun parse(obj: Any?): GsonData {
            if (obj == null) return EMPTY
            if (obj is GsonData) return obj

            val json = obj.toString()
            return GsonData(JsonParser.parseString(json))
        }

    }


//: Iterable<GsonData>
//    //============ JsonElement는 아래 셋중 하나의 상태를 가진다.==================
//    private var jsonPrimitive: JsonPrimitive? = null
//    private var jsonObject: JsonObject? = null
//    private var jsonArray: JsonArray? = null

//    fun toList(): List<GsonData> {
//        if (delegate is JsonArray) {
//            return delegate.asJsonArray.map { GsonData(it) }
//        }
//        throw IllegalStateException("json array required")
//    }
//
//    fun toMap(): Map<String, GsonData?> {
//        if (isObject) {
//            val map: MutableMap<String, GsonData?> = Maps.newLinkedHashMap()
//            for ((key, value) in jsonObject!!.entrySet()) map[key] = of(value)
//            return map
//        }
//        return emptyMap<String, GsonData>()
//    }
//
//    override fun iterator(): Iterator<GsonData> {
//        return toList().iterator()
//    }


//    val isObject: Boolean
//        get() = jsonObject != null
//    val isArray: Boolean
//        get() = jsonArray != null
//    val isPrimitive: Boolean
//        get() = jsonPrimitive != null
//    val isNumber: Boolean
//        get() = isPrimitive && jsonPrimitive!!.isNumber
//    val isNull: Boolean
//        get() = jsonPrimitive == null && jsonObject == null && jsonArray == null
//    val isEmpty: Boolean
//        get() = size() == 0
//
//    fun size(): Int {
//        if (isObject) return jsonObject!!.entrySet().size
//        return if (isArray) jsonArray!!.size() else 0
//    }

//    val asLong: Long?
//        get() = if (jsonPrimitive == null) null else jsonPrimitive!!.asLong
//    val asString: String?
//        get() = if (jsonPrimitive == null) null else jsonPrimitive!!.asString

//======================== 기본 put ======================================
//    operator fun put(key: String, num: Number) {
//        if (delegate is JsonObject) {
//            jsonObject.addProperty(key, num)
//        }
//        throw IllegalStateException("json object required")
//    }
//    operator fun put(key: String, num: String) {
//        if (delegate is JsonObject) {
//            jsonObject.addProperty(key, num)
//        }
//    }

////    fun put(key: String?, value: Boolean?): GsonData {
////        if (jsonObject == null) jsonObject = JsonObject()
////        jsonObject!!.addProperty(key, value)
////        return this
////    }
////
////    /** 간단 enum 입력. 뒤의 get 하고도 연결됨  */
////    fun put(value: Enum<*>): GsonData {
////        if (jsonObject == null) jsonObject = JsonObject()
////        val key: String = StringUtil.uncapitalize(value.javaClass.simpleName)
////        jsonObject!!.addProperty(key, value.name)
////        return this
////    }
//
//    fun put(key: String?, data: GsonData?): GsonData {
//        if (jsonObject == null) jsonObject = JsonObject()
//        jsonObject!!.add(key, data!!.delegate())
//        return this
//    }
//
//    fun putAll(data: GsonData): GsonData {
//        if (jsonObject == null) jsonObject = JsonObject()
//        for ((key, value) in data.delegate().asJsonObject.entrySet()) {
//            jsonObject!!.add(key, value)
//        }
//        return this
//    }
//
//    /** csv를 reflection으로 vo로 매핑할때 사용  */
//    fun putAll(header: Array<String?>, line: Array<String?>): GsonData {
//        if (jsonObject == null) jsonObject = JsonObject()
//        for (i in line.indices) {
//            val name = header[i]
//            jsonObject!!.addProperty(name, line[i])
//        }
//        return this
//    }
//
//    /** 리플렉션용  */
//    fun putAny(key: String?, data: Any?): GsonData {
//        if (data == null) return this
//        if (data is GsonData) return put(key, data as GsonData?)
//        if (data is Number) return put(key, data as Number?)
//        return if (data is Boolean) put(key, data as Boolean?) else put(key, data.toString())
//    }
//
//    /** 간단 입력. 뒤의 get 하고도 연결됨  */
//    fun putInstance(value: Any): GsonData {
//        if (jsonObject == null) jsonObject = JsonObject()
//        val key: String = StringUtil.uncapitalize(value.javaClass.simpleName)
//        put(key, from(value))
//        return this
//    }

//    //==================================================== 기본 add ======================================================
//    fun add(data: String?): GsonData {
//        return add(parse(data))
//    }
//
//    fun add(data: GsonData?): GsonData {
//        if (jsonObject != null) {
//            log.warn("")
//        }
//        if (jsonArray == null) jsonArray = JsonArray()
//        jsonArray!!.add(data!!.delegate())
//        return this
//    }
//
//    fun addAll(data: GsonData): GsonData {
//        if (jsonArray == null) jsonArray = JsonArray()
//        jsonArray!!.addAll(data.jsonArray)
//        return this
//    }
//======================== 기본 get (GsonData 리턴. null을 리턴하지 않는다.) ======================================

//	/** GsonVo 리턴 */
//	public GsonData get(int index){
//		JsonElement jsonElement = jsonArray.get(index);
//		if(jsonElement==null) return GsonData.empty();
//		return GsonData.of(jsonElement);
//	}
//    /** 원본 객체에서 제거 & 제거된 객체 리턴한다.  */
//    fun remove(index: Int): GsonData? {
//        val jsonElement = jsonArray!!.remove(index) ?: return empty()
//        return of(jsonElement)
//    }
//
//
//    /** 원본 객체에서 제거 & 제거된 객체 리턴한다.  */
//    fun remove(key: String?): GsonData? {
//        val jsonElement = jsonObject!!.remove(key) ?: return empty()
//        return of(jsonElement)
//    }

//======================== 기본 get (Object의 value 리턴. 없을때는 null리턴 가능) ======================================
//    /**  특이하게 null을 리턴한다.  */
//    private fun getJsonPrimitive(key: String): JsonPrimitive? {
//        if (jsonObject == null) return null
//        val jsonElement = jsonObject!![key]
//        if (jsonElement == null || jsonElement.isJsonNull) return null
//        if (!jsonElement.isJsonPrimitive) {
//            val msg: String = StringFormatUtil.format("{} => [{}]는 프리미티브 타입이 아닙니다", key, jsonElement)
//            throw IllegalStateException(msg)
//        }
//        return jsonElement.asJsonPrimitive
//    }
//
//    //====== String
//    fun get(key: String, nullValue: String?): String? {
//        val primitive = getJsonPrimitive(key) ?: return nullValue
//        return if (primitive.isJsonNull) nullValue else primitive.asString
//    }
//
//    fun get(key: String): String? {
//        return get(key, null)
//    }
//
//    //====== Long
//    fun getLong(key: String, nullValue: Long?): Long? {
//        val primitive = getJsonPrimitive(key) ?: return nullValue
//        return if (primitive.isNumber) {
//            primitive.asLong
//        } else {
//            DecimalUtil.toDecimal(primitive.asString).longValue()
//        }
//    }
//
//    fun getLong(key: String): Long? {
//        return getLong(key, null)
//    }
//
//    fun addLong(key: String, delta: Long): Long {
//        val current = getLong(key, 0L)
//        val value = current!! + delta
//        put(key, value)
//        return value
//    }
//
//    //====== Integer
//    fun getInt(key: String, nullValue: Int?): Int? {
//        val primitive = getJsonPrimitive(key) ?: return nullValue
//        return if (primitive.isNumber) {
//            primitive.asInt
//        } else {
//            DecimalUtil.toDecimal(primitive.asString).intValue()
//        }
//    }
//
//    fun getInt(key: String): Int? {
//        return getInt(key, null)
//    }
//
//    fun addInt(key: String, delta: Int): Int {
//        val current = getInt(key, 0)
//        val value = current!! + delta
//        put(key, value)
//        return value
//    }
//
//    //====== Boolean
//    fun getBoolean(key: String): Boolean? {
//        return getBoolean(key, null)
//    }
//
//    fun getBoolean(key: String, nullValue: Boolean?): Boolean? {
//        val primitive = getJsonPrimitive(key) ?: return nullValue
//        return primitive.asBoolean
//    }
//
//    //====== BigDecimal
//    fun getDecimal(key: String, nullValue: BigDecimal?): BigDecimal? {
//        val primitive = getJsonPrimitive(key) ?: return nullValue
//        return if (primitive.isNumber) {
//            primitive.asBigDecimal
//        } else {
//            DecimalUtil.toDecimal(primitive.asString)
//        }
//    }
//
//    fun getDecimal(key: String): BigDecimal? {
//        return getDecimal(key, null)
//    }
//
//    //====== Enum
//    fun <T : Enum<*>?> getEnmum(clazz: Class<T>, nullValue: T): T {
//        val key: String = StringUtil.uncapitalize(clazz.simpleName)
//        val value = get(key) ?: return nullValue
//        return java.lang.Enum.valueOf(clazz, value) as T
//    }
//
//    fun <T : Enum<*>?> getEnmum(clazz: Class<T?>): T? {
//        return getEnmum(clazz, null)
//    }
//
//    /**
//     * 해당 json의 자바 타입을 맞춰서 리턴
//     * 숫자/문자에 따라 형식이 바뀔때 사용함 ex) TextGrid
//     */
//    fun getObject(key: String, nullValue: Any): Any {
//        val primitive = getJsonPrimitive(key) ?: return nullValue
//        if (primitive.isNumber) return primitive.asBigDecimal
//        return if (primitive.isBoolean) primitive.asBoolean else primitive.asString
//    }
//
//    fun getObject(key: String): Any {
//        return getObject(key, "")
//    }
//
//    fun <T : Enum<*>?> getInstance(clazz: Class<T>, nullValue: T): T {
//        val key: String = StringUtil.uncapitalize(clazz.simpleName)
//        val gsonData = get(key) ?: return nullValue
//        return gsonData.fromJson(clazz)
//    }
//
//    /**
//     * 파싱 실패시 디폴트값을 사용한다.
//     * 넘버 타입은 이걸로 파싱후 각자 컨버팅 할것
//     */
//    fun parseDecimal(key: String, nullValue: BigDecimal): BigDecimal {
//        val value = get(key)
//        return try {
//            DecimalUtil.toDecimal(value)
//        } catch (e: Exception) {
//            nullValue
//        }
//    }
//    //======================== 확장 get  ======================================
//    /** GsonVo 리턴.  TreeMap 처럼 get().put() 등 인라인 데이터 추가를 위해 만들어졌다.  */
//    fun getOrCreateObject(key: String?): GsonData? {
//        val jsonElement = jsonObject!![key]
//        if (jsonElement == null) {
//            val newJson: JsonElement = JsonObject()
//            jsonObject!!.add(key, newJson) //데이터를 연결해준 후 리턴한다
//            return of(newJson)
//        }
//        return of(jsonElement)
//    }
//
//    /**
//     * GsonVo리턴.
//     * '.' 으로 연결된 패스로 가져온다.
//     * 크롤링 등에서 간단 편집에 사용하기위해 만들었음
//     *
//     * 빈거도 객체를 리턴하게 바꼈다.
//     */
//    @Deprecated("")
//    fun getByPath(key: String): GsonData? {
//        if (jsonObject == null) return empty()
//        Preconditions.checkArgument(StringUtil.contains(key, "."))
//        val keys = key.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//        var current: JsonElement? = jsonObject
//        for (currentKey in keys) {
//            if (current!!.isJsonArray) current = current.asJsonArray[0] //첫번째꺼 가죠옴
//            current = current!!.asJsonObject[currentKey]
//            if (current == null) return empty()
//        }
//        return of(current)
//    }
//
//    fun <T> fromJson(clazz: Class<T>?): T {
//        return GsonUtil.fromJson(delegate(), clazz)
//    }
//
//    fun <T> fromJson(type: Type?): T {
//        return GsonUtil.fromJson(this, type)
//    }
//    //======================== 특수 기능들 ======================================
//    /**
//     * MapTree의 getAllFlat 와 동일한 기능이다.
//     * 프리미티브인 value 만을 찾아서 리턴해준다.
//     */
//    fun flatEntry(): List<Map.Entry<String?, JsonPrimitive?>> {
//        val list: MutableList<Map.Entry<String?, JsonPrimitive?>> = Lists.newArrayList()
//        flatEntry<Any>(list)
//        return list
//    }
//
//    private fun <V> flatEntry(list: MutableList<Map.Entry<String?, JsonPrimitive?>>) {
//        if (isArray) {
//            for (each in this) {
//                each.flatEntry<Any>(list)
//            }
//        } else if (isObject) {
//            //객체인경우 value가 컬렉션이 아닌 경우만
//            for ((key, jsonElement) in jsonObject!!.entrySet()) {
//                val gson = of(jsonElement) ?: continue
//                if (gson.isArray || gson.isObject) {
//                    gson.flatEntry<Any>(list)
//                } else {
//                    list.add(AbstractMap.SimpleEntry<Any?, Any?>(key, gson.jsonPrimitive))
//                }
//            }
//            //		}else if(this.isNull()) {
////			//아무것도 안함
//        } else {
//            list.add(AbstractMap.SimpleEntry<Any?, Any?>(null, jsonPrimitive))
//        }
//    }
//    //======================== 특수 기능들 (static 머지)  ======================================
//    /**
//     * 트리 구조를 유지하면서 숫자로 된 값만 찾아서 데이터를 합쳐(누적)준다. (테스트 참고)
//     * Map만 작동한다 (프리미티브 or Array는 무시된다)
//     * Map에 비해서 타입이 없는 Json이 Tree구조 표현에  더 깔끔하다.
//     * ex) 각 task별 한줄씩 기록된 JSON 결과들을 합치기
//     * ex) 다수 인스턴스의 데이터를 로드해서 하나로 합쳐서 보여주기
//     *
//     * 사용예
//     * ex) GsonData.object().copy(Lists.newArrayList(append01,append02), GsonData.MERGE_NUMBER )
//     */
//    fun merge(results: Collection<GsonData?>, mergeFunc: BiConsumer<GsonData?, GsonData?>): GsonData {
//        for (append in results) {
//            mergeFunc.accept(this, append)
//        }
//        return this
//    }
//
//    //======================== 기타 ======================================
//    override fun toString(): String {
//        return if (jsonPrimitive != null) jsonPrimitive!!.asString else if (jsonObject != null) jsonObject.toString() else if (jsonArray != null) jsonArray.toString() else "" //임시
//    }
//
//    fun toPrettyString(): String {
//        return if (jsonPrimitive != null) GsonUtil.PRETTY.toJson(jsonPrimitive) else if (jsonObject != null) GsonUtil.PRETTY.toJson(jsonObject) else if (jsonArray != null) GsonUtil.PRETTY.toJson(
//            jsonArray
//        ) else "" //임시
//    }
//
//    /**  실제 위임 객체 리턴  */
//    fun delegate(): JsonElement {
//        if (jsonPrimitive != null) return jsonPrimitive else if (jsonObject != null) return jsonObject else if (jsonArray != null) return jsonArray
//        return JsonNull.INSTANCE
//    }
//
//    /**
//     * 키값을 키네시스 타입에 적합한 언더스코어 형태로 변환해준다.
//     * 임시로직이지만 성능때문에 기본객체 그대로 씀
//     */
//    fun toUnderscore(): JsonObject {
//        check(isObject) { "일단 객체만 지원" }
//        return GsonUtil.toUnderscore(jsonObject)
//    }
//

}