package net.kotlinx.csv.chunkTools

import com.github.doyaaaaaken.kotlincsv.client.ICsvFileWriter


/**
 * header 가 꼭 필요하지 않다면 flow를 사용해주세요
 * */
data class CsvChunk(

    /** 0부터 시작 */
    val index: Int,

    /** 청크 안에 담긴 로우 */
    val rows: List<List<String>>,

    /** CSV 헤더 */
    val header: List<String>? = null,

    /**
     * writer가 있을경우 쓰지 지원
     * N개의 처리에 N개 이상의 row가 입력될 수 있음!
     * ex) writeRow(...)
     *  */
    val writer: ICsvFileWriter? = null,
)