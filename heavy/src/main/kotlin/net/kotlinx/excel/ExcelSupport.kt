package net.kotlinx.excel

import java.io.File


/**
 * 간단하게 인메모리로 Xlsx 읽기
 *  */
fun File.readExcellLines(): LinkedHashMap<String, List<List<String>>> = Excel.from(this).readAll()
