package net.kotlinx.io.input

import java.io.InputStream


/**
 * 스프링하고 비슷함
 * 사실 사용되는건 인풋스트림 직접사용 or 파일 뿐임
 * */
interface InputResource {

    /** 입력 스트림 */
    val inputStream: InputStream

}