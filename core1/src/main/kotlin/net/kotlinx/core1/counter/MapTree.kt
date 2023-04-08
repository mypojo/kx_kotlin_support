//package net.kotlinx.core1.counter
//
//import java.util.concurrent.atomic.AtomicLong
//import java.util.function.BiConsumer
//import java.util.function.Function
//import java.util.function.Supplier
//import kotlin.collections.Map.Entry
//
///**
// * 스래드 세이프한, value가 없으면 생성해주는 맵
// * 주의!!!  value에는 가변 객체가 와야 수정이 가능하다. supplier로 제공된 래퍼런스 교체는 불가능함
// * ex) value가 Long 같은 프리미티브가 아니라  MutableLong을 사용
// * 주의!!!  JSON 과는 다르게 뎁스에 스키마가 있다. 즉 트리구조가 아닌 플랫 구조이다
// */
//class MapTree<T>(
//    private val delegate: MutableMap<String, T?> = mutableMapOf(),
//    private var supplier: ()->T?
//): MutableMap<String, T?> by delegate {
//
//    //============================== method =================================
//    /** 핵심 메소드  */
//    @Synchronized
//    override operator fun get(key: Any): T? {
//        val realKey = key?.toString() ?: "" //널 세이프하게!  오류의 영향이 있긴 하다.
//        var value = delegate!![realKey]
//        if (value == null) {
//            value = supplier!!.get()
//            val innerKey = realKey ?: ""
//            delegate!![innerKey] = value
//        }
//        Preconditions.checkNotNull(value)
//        return value
//    }
//
//    /**
//     * AtomicLong 등 시리얼이 힘든걸 일반적인 맵 구조로 변환해준다.
//     * ex( String json = GsonUtil.toJson(counter.convert(MapTree.TO_LONG));
//     * ex) counter.convert((AtomicLong a)->a.get()+2)
//     */
//    @Synchronized
//    fun <V, R, E> convert(func: Function<V, R>): Map<String, E> {
//        val newMap: MutableMap<String, E> = Maps.newTreeMap()
//        for ((key, value) in delegate!!) {
//            if (value is MapTree<*>) {
//                val mapTree = value as MapTree<*>
//                val innerMap = mapTree.convert<V, R, R>(func)
//                newMap[key] = innerMap as E
//            } else {
//                val converted = func.apply(value as V)
//                newMap[key] = converted as E
//            }
//        }
//        return newMap
//    }
//    //==============================================  위임구햔 =================================================
//    /**
//     * 직접 입력에 주의할것!
//     * 타 구현체가 사용해서 이것도 구현했음
//     */
//    override fun put(key: String, value: T?): T? {
//        return delegate!!.put(key, value)
//    }
//
//    override fun entrySet(): Set<Entry<String, T>> {
//        return delegate!!.entries
//    }
//    //==============================================  특수기능01 - 인라인화 =================================================
//    /**
//     * 뎁스를 구분하지 않고 최종 노드만의 값들을 리턴한다.
//     * 특정 키의 합계 등을 구할때 사용함
//     * ex) counter.<Number>getAllFlat().stream().filter(e->e.getKey().equals("c")).mapToLong(e-> e.getValue().longValue() ).sum()
//    </Number> */
//    fun <V> flatEntry(): List<Entry<String, V>> {
//        val list: MutableList<Entry<String, V>> = Lists.newArrayList()
//        flatEntry(list)
//        return list
//    }
//
//    private fun <V> flatEntry(list: MutableList<Entry<String, V>>) {
//        for (e in delegate!!.entries) {
//            val value = e.value!!
//            if (value is MapTree<*>) {
//                val map = value as MapTree<*>
//                map.flatEntry(list)
//            } else {
//                list.add(e as Entry<*, *>)
//            }
//        }
//    }
//    //======================== 특수 기능들 (static 머지)  ======================================
//    /**
//     * ## Map 계열 인터페이스 통일 (GsonData / MapTree) <-- MapUtil쪽은 버림.. 일반 맵을 MapTree로 써보자
//     *
//     *
//     * 트리 구조를 유지하면서 숫자로 된 값만 찾아서 데이터를 합쳐(누적)준다. (테스트 참고)
//     * Map만 작동한다 (프리미티브 or Array는 무시된다)
//     * Map에 비해서 타입이 없는 Json이 Tree구조 표현에  더 깔끔하다.
//     * ex) 각 task별 한줄씩 기록된 JSON 결과들을 합치기
//     * ex) 다수 인스턴스의 데이터를 로드해서 하나로 합쳐서 보여주기
//     *
//     *
//     * ex) copy(results, new MapTreeMergeNumber<>());
//     */
//    fun merge(results: Collection<MapTree<T>?>, mergeFunc: BiConsumer<MapTree<T>?, MapTree<T>?>): MapTree<T> {
//        for (append in results) {
//            mergeFunc.accept(this, append)
//        }
//        return this
//    }
//
//    @Slf4j
//    class MapTreeMergeNumber<T> : BiConsumer<MapTree<T?>, MapTree<T>?> {
//        override fun accept(mapA: MapTree<T?>, mapB: MapTree<T>?) {
//            for ((key, valueB) in mapB!!) {
//                val valueA: Any? = mapA[key]
//
//                //A 기준으로 매핑한다.
//                if (valueA is MapTree<*>) {
//                    accept(valueA as MapTree<T?>, valueB as MapTree<T>?)
//                } else if (valueA is Number && valueB is Number) {
//
//                    //========= 숫자 형태인 경우 =========
//                    if (valueA is MutableLong) {
//                        val b = valueB as Number
//                        val a: MutableLong = valueA as MutableLong
//                        a.add(b)
//                    } else if (valueA is AtomicLong) {
//                        val b = valueB as Number
//                        valueA.addAndGet(b.toLong())
//                    } else if (valueA is Long) {
//                        //불변 객체는  다이렉트로 박아준다. 원래의 의도와 틀려질 수 있으니 주의!!
//                        val a = valueA as Number
//                        val b = valueB as Number
//                        val sum = a.toLong() + b.toLong()
//                        mapA[key] = sum as T
//                    } else {
//                        log.warn("NUMBER 매핑 실패 [{}] ->  {} / {}", key, valueA.javaClass.simpleName, valueB.javaClass.simpleName)
//                    }
//                } else {
//                    log.warn("매핑 실패 [{}] ->  {} / {}", key, valueA!!.javaClass.simpleName, valueB!!.javaClass.simpleName)
//                }
//            }
//        }
//    }
//    //==============================================  편의용 메소드 =================================================
//    /** 편의용 메소드  */
//    fun <V> getAsMapTree(key: String): MapTree<V>? {
//        var value = delegate!![key]
//        if (value == null) {
//            value = supplier!!.get()
//            delegate!![key] = value
//        }
//        Preconditions.checkNotNull(value)
//        return get(key) as MapTree<V>?
//    }
//
//    fun <V> getAsPath(keyList: List<String?>): V? {
//        var inner: MapTree<*>? = this
//        for (i in 0 until keyList.size - 1) {
//            val key = keyList[i]
//            inner = inner!!.getAsMapTree<Any>(key!!)
//        }
//        val lastKey: String = CollectionUtil.getLast(keyList)
//        return inner!![lastKey] as V?
//    }
//
//    override fun toString(): String {
//        return delegate.toString()
//    }
//
//    override fun size(): Int {
//        return delegate!!.size
//    }
//
//    override fun clear() {
//        delegate!!.clear()
//    }
//
//    override fun isEmpty(): Boolean {
//        return delegate!!.isEmpty()
//    }
//
//    override fun keySet(): Set<String> {
//        return delegate!!.keys
//    }
//
//    override fun containsKey(key: Any): Boolean {
//        return delegate!!.containsKey(key)
//    }
//
//    override fun remove(key: Any): T? {
//        return delegate!!.remove(key)
//    }
//
//    override fun values(): Collection<T> {
//        return delegate!!.values
//    }
//
//    companion object {
//        fun <T> tree(supplier: Supplier<T>?): MapTree<T> {
//            val vo = MapTree<T>()
//            vo.delegate = Maps.newTreeMap()
//            vo.supplier = supplier
//            return vo
//        }
//
//        /** 입력 순서 베이스 (bean 리플렉션)  */
//        fun <T> linked(supplier: Supplier<T>?): MapTree<T> {
//            val vo = MapTree<T>()
//            vo.delegate = Maps.newLinkedHashMap()
//            vo.supplier = supplier
//            return vo
//        }
//
//        /** flat한 map을 읽어서 머지 등의 파싱 작업을 할때 사용. supplier 가 null임으로 에디팅은 안된다.  */
//        fun <T> from(delegate: MutableMap<String, T>?): MapTree<T> {
//            val vo = MapTree<T>()
//            vo.delegate = delegate
//            return vo
//        }
//        //============================== AtomicLong =================================
//        /** 카운터 만들때 사용  */
//        fun AtomicLong(): MapTree<AtomicLong?> {
//            return linked(Supplier { AtomicLong() })
//        }
//
//        fun AtomicLong2(): MapTree<MapTree<AtomicLong>> {
//            return tree<MapTree<AtomicLong>>(Supplier<MapTree<AtomicLong?>> { AtomicLong() })
//        }
//
//        fun AtomicLong3(): MapTree<MapTree<MapTree<AtomicLong>>> {
//            return tree { AtomicLong2() }
//        }
//
//        /** 인라인으로 카운터 만들기  */
//        fun <T> AtomicLong(list: List<T>, toId: Function<T, String?>): MapTree<AtomicLong?> {
//            val vo = AtomicLong()
//            for (each in list) {
//                val id = toId.apply(each)
//                vo[id]!!.incrementAndGet()
//            }
//            return vo
//        }
//        //============================== MutableLong =================================
//        /** 카운터 만들때 사용  */
//        fun MutableLong(): MapTree<MutableLong?> {
//            return linked<MutableLong?>(Supplier<MutableLong?> { MutableLong() })
//        }
//
//        /**
//         * MapTree<MapTree></MapTree><MutableLong>> t2 = MapTree.MutableLong(2);
//         * MapTree<MapTree></MapTree><MapTree></MapTree><MutableLong>>> t2 = MapTree.MutableLong(3);
//        </MutableLong></MutableLong> */
//        fun <T> MutableLong(depth: Int): MapTree<T?>? {
//            Preconditions.checkState(depth > 0)
//            if (depth == 1) return MutableLong()
//            var tree: MapTree<*>? = null
//            for (i in 1 until depth) {
//                tree = tree<MapTree<Any>?>(Supplier<MapTree<Any?>?> { MutableLong<Any?>(i) })
//            }
//            return tree as MapTree<T?>?
//        }
//
//        /** ex) counter.convert(MapTree.TO_LONG)  */
//        var TO_LONG = Function { obj: Number -> obj.toLong() }
//    }
//}