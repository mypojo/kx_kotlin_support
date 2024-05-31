package net.kotlinx.core


/**
 * 데이터를 변환 / 역변환 하는 네이밍 통일용 인터페이스
 * 찾아보니 없어서 만들었다.
 *  */
interface DataConverter<A, B> {

    fun convertTo(data: A): B

    fun convertFrom(token: B): A

}