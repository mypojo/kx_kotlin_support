@file:Suppress("UnstableApiUsage")

package net.kotlinx.guava

import com.google.common.hash.HashFunction
import com.google.common.hash.Hashing
import net.kotlinx.number.NumberUtil
import net.kotlinx.number.padStart
import java.nio.charset.Charset
import java.time.LocalDateTime
import kotlin.math.abs


/**
 * 해시 분할 관련 유틸
 */
object HashUtil {

    /** 암호화가 아니라 단순 분할 해시가 필요함으로 MD5 -> sha256를 쓴다  */
    private val HF: HashFunction = Hashing.sha512()

    /** 해시는 무조건 UTF-8  */
    private val SET: Charset = Charsets.UTF_8

    /** 자주 사용하는 소스코드 샘플  */
    fun hash(key: String): String {
        return HF.newHasher().putString(key, SET).hash().toString()
    }

    /**
     * 잘려진 해시 문자를 리턴한다.
     * 해당 key값으로 파일을 넓게 써서 인메모리에서 처리하기 위해서 사용했다.
     * 스래드 숫자만큼 나누는게 적절해 보인다.
     * ex) "asdlkfhasdhfuiawe" -> "02"
     * */
    fun hashIntString(key: String, max: Int): String {
        val code = key.hashCode().toLong()
        val hashCode = abs(code.toDouble()).toLong()
        val num = hashCode % max

        val padSize = NumberUtil.numPadSize(max)
        return num.padStart(padSize)
    }

    /** 현재 분 단위로 해시값을 리턴한다. 초단위까지는 필요없음  */
    fun hashIntString(dateTime: LocalDateTime, splitMin: Int, groupBy: Int): String {
        val sumOfmin = dateTime.hour * 60 + dateTime.minute
        val minMine = sumOfmin % splitMin
        val minNum = minMine / groupBy //버림
        return minNum.padStart(2)
    }

    /**
     * 마지막 x자리를 리턴한다. 숫자로 구성된 간단 해시용. 깔맞춤 하기 위해서 그냥 추가했음.
     * ex) 마지막 1자리를 사용해서 10자리 해시 구성
     * 필요 없을듯..?
     */
    fun hashLastString(id: String, size: Int): String {
        return id.substring(id.length - size) //ID 마지막 1자리를 키로 사용함
    }
}
