package net.kotlinx.core.koson

import com.lectra.koson.ArrayType
import com.lectra.koson.ObjectType
import com.lectra.koson.arr


/** 이런식으로 변환 가능하다. list map 할때 참고 */
fun List<ObjectType>.toKsonArray(): ArrayType = arr[this]