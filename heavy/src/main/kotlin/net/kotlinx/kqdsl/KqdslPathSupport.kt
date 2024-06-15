package net.kotlinx.kqdsl

import com.linecorp.kotlinjdsl.querymodel.jpql.path.Path
import com.linecorp.kotlinjdsl.querymodel.jpql.path.Paths
import kotlin.reflect.KProperty1


//==================================================== 일반적인 주의.. Paging 들어가면 fetch 금지!! ======================================================


/**
 * path(xx) 이걸 xx.path 일케 바꿔준다..
 * 난 이게 더 보기 좋음..
 * */
val <T : Any, V> KProperty1<T, V>.path: Path<V & Any>
    get() = Paths.path(this)

