//package net.kotlinx.module.reflect
//
//import com.google.common.base.Preconditions
//import com.google.common.collect.Lists
//import com.google.common.collect.Maps
//import java.lang.reflect.Field
//import java.util.*
//import kotlin.math.max
//
///***
// * 특정 필드를 설정해서 값을 비교하는 도구
// * ex) 두 DB간의 필드 달라진점 찾기
// */
//class BeanCompareTool {
//    /**
//     * 조건
//     */
//    private val matcher: KeyContainPredicator = KeyContainPredicator()
//
//    /**
//     * 공백문자와 null을 같은것으로 취급한다.
//     */
//    private val oracleString = true
//    //=========================================================== 2개 비교 ===========================================================
//    /**
//     * 두개를 비교해서 틀린점을 리턴한다.
//     */
//    fun <T : Map<String?, *>?> dirtyMap(before: T, after: T): List<BeanCompareFieldData> {
//        val dirtyFields: MutableList<BeanCompareFieldData> = Lists.newArrayList<BeanCompareFieldData>()
//        for ((key, value) in before!!) {
//            if (!matcher.test(key)) continue
//            val aValue = value!!
//            val bValue = after!![key]!!
//            val isEqual: Boolean = if (oracleString) PredicateUtil.isEqualsWithEmptyString(aValue, bValue) else PredicateUtil.isEquals(aValue, bValue)
//            if (!isEqual) {
//                dirtyFields.add(BeanCompareFieldData(key, aValue, bValue))
//            }
//        }
//        return dirtyFields
//    }
//
//    fun <T : Map<String?, *>?> dirtyMapList(list: List<T>): List<Map.Entry<T, List<BeanCompareFieldData>?>> {
//        return dirtyMapList(list, "")
//    }
//
//    /**
//     * 두개를 비교해서 틀린점을 리턴한다.
//     * T server,T input 순이다.
//     */
//    fun <T> dirtyField(server: T, input: T): BeanCompareData {
//        val dirtyFields: MutableList<BeanCompareFieldData> = Lists.newArrayList<BeanCompareFieldData>()
//        val fields: List<Field> = BeanUtil.getAllDeclaredFields(server.javaClass)
//        for (field in fields) {
//            if (!matcher.test(field.name)) continue
//            val serverValue: Any = ReflectionUtil.getFieldValue(field, server)
//            val inputValue: Any = ReflectionUtil.getFieldValue(field, input)
//            val isEqual: Boolean = if (oracleString) PredicateUtil.isEqualsWithEmptyString(serverValue, inputValue) else PredicateUtil.isEquals(serverValue, inputValue)
//            if (!isEqual) {
//                val di = BeanCompareFieldData(field.name, serverValue, inputValue)
//                di.setField(field)
//                dirtyFields.add(di)
//            }
//        }
//        return BeanCompareData(server, input, dirtyFields)
//    }
//
//    /**
//     * CollectionUtils.isEqualCollection(A, B) 를 대체한다.
//     * 특정 필드만 비교 가능
//     */
//    fun <T> dirtyFieldList(servers: List<T>, inputs: List<T>): List<BeanCompareData> {
//        if (servers.isEmpty() && inputs.isEmpty()) return Lists.newArrayList<BeanCompareData>()
//        val datas: MutableList<BeanCompareData> = Lists.newArrayList<BeanCompareData>()
//        val max = max(servers.size.toDouble(), inputs.size.toDouble()).toInt()
//        val clazz: Class<*> = if (servers.isEmpty()) inputs[0].javaClass else servers[0].javaClass
//        val nullValue = ReflectionUtil.newInstance(clazz) as T //null은 기본값으로 객체로 대체. 혼란의 여지가 있다
//        for (i in 0 until max) {
//            val server: T = CollectionUtil.nullSafeGet(servers, i, nullValue)
//            val input: T = CollectionUtil.nullSafeGet(inputs, i, nullValue)
//            val compared: BeanCompareData = dirtyField(server, input)
//            datas.add(compared)
//        }
//        return datas
//    }
//    //=========================================================== 리스트 비교 ===========================================================
//    /**
//     * 최신 자료가 위로 오게 소팅해야 한다.
//     * ex) List<Entry></Entry><Map></Map><String></String>,Object>,List<DirtyInfo>>> dirtyList =  compareTool.dirtyMapList(list);
//    </DirtyInfo> */
//    fun <T : Map<String?, *>?> dirtyMapList(list: List<T>, compareKey: String): List<Map.Entry<T?, List<BeanCompareFieldData>?>> {
//        val results: MutableList<Map.Entry<T?, List<BeanCompareFieldData>?>> = Lists.newArrayList<Map.Entry<T?, List<BeanCompareFieldData>?>>()
//        val beforeMap: MutableMap<Any?, T> = Maps.newHashMap()
//
//        //아래부터 읽는다.
//        val it = list.listIterator(list.size)
//        while (it.hasPrevious()) {
//            val after = it.previous()
//            var pk = after!![compareKey]
//            if (pk == null) pk = compareKey
//            val before = beforeMap[pk]
//            if (before == null) {
//                results.add(AbstractMap.SimpleEntry<Any?, Any?>(after, emptyList<BeanCompareFieldData>()))
//            } else {
//                val dirtys: List<BeanCompareFieldData> = dirtyMap<T>(before, after)
//                results.add(AbstractMap.SimpleEntry<Any?, Any?>(after, dirtys))
//            }
//            beforeMap[pk] = after
//        }
//        return results
//    }
//
//    /**
//     * 주로 이력 보여줄때 사용한다.
//     * 최근 데이터가 위로 오게 소팅되어있어야 한다.
//     */
//    fun <T> dirtyList(list: List<T>): List<Map.Entry<T?, List<BeanCompareFieldData>?>> {
//        val results: List<Map.Entry<T?, List<BeanCompareFieldData>?>> = Lists.newArrayList<Map.Entry<T?, List<BeanCompareFieldData>?>>()
//        var before: T? = null
//
//        //아래부터 읽는다.
//        val it = list.listIterator(list.size)
//        while (it.hasPrevious()) {
//            val after = it.previous()
//            if (before == null) {
//                results.add(0, AbstractMap.SimpleEntry<Any?, Any?>(after, emptyList<BeanCompareFieldData>()))
//            } else {
//                val tBeanCompareData: BeanCompareData = dirtyField(before, after)
//                val dirtys: List<BeanCompareFieldData> = tBeanCompareData.getDirtyInfos()
//                results.add(0, AbstractMap.SimpleEntry<Any?, Any?>(after, dirtys))
//            }
//            before = after
//        }
//        return results
//    }
//    //=========================================================== 카피 ===========================================================
//    /**
//     * 서버 값을 대상으로 얕은 복사 해준다. ReflectionUtil 그대로 사용
//     * T에 컬렉션이 오지 않게 주의할것!
//     *
//     * @return 변경(더티) 여부
//     */
//    fun <T> shallowCopy(server: T, input: T): Boolean {
//        val compareData: BeanCompareData = dirtyField(server, input)
//        val dirtyFields: List<BeanCompareFieldData> = compareData.getDirtyInfos()
//        if (dirtyFields.isEmpty()) return false
//        for (dirtyField in dirtyFields) {
//            ReflectionUtil.setFieldValue(dirtyField.getField(), server, dirtyField.getAfterValue())
//        }
//        return true
//    }
//
//    /**
//     * shallowCopy의 컬렉션 버전
//     */
//    fun <T> shallowCopys(servers: List<T>, inputs: List<T>) {
//        Preconditions.checkArgument(servers.size == inputs.size)
//        for (i in servers.indices) {
//            val server = servers[i]
//            val input = inputs[i]
//            shallowCopy(server, input)
//        }
//    }
//
//    /**
//     * 새로 만들어준다.
//     */
//    fun <T> shallowCopy(input: T): T {
//        val newInstance = ReflectionUtil.newInstance(input.javaClass) as T
//        shallowCopy(newInstance, input)
//        return newInstance
//    }
//}
