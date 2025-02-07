package net.kotlinx.io.output

import java.io.OutputStream


/**
 * 스프링하고 비슷함
 * 사실 사용되는건 인풋스트림 직접사용 or 파일 뿐임
 * */
interface OutputResource {

    /** 출력 스트림 */
    val outputStream: OutputStream

}