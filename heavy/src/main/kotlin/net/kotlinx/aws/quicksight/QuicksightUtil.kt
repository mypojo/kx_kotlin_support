package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.model.InputColumnDataType
import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.aws.s3.S3Data

object QuicksightUtil {


    /**
     * 퀵사이트 S3 매니페스트
     * https://docs.aws.amazon.com/quicksight/latest/user/supported-manifest-file-format.html
     * CSV 말고 다른거는 커스텀 할것
     * */
    fun s3Manifest(files: List<S3Data>): String = obj {
        "fileLocations" to arr[
            obj {
                "URIs" to arr[files.map { it.toFullPath() }]
            }
        ]
        "globalUploadSettings" to obj {
            "format" to "CSV"
        }
    }.toString()


    /** 컬럼 스키마를 가지고 퀵사이트 스키마 맵으로 변경 */
    fun toColumnMap(columns: List<Pair<String, String>>): Map<String, InputColumnDataType> {
        return columns.associate {
            it.first to when (it.second) {
                "varchar" -> InputColumnDataType.String
                "integer" -> InputColumnDataType.Integer
                "bigint" -> InputColumnDataType.Integer
                "real" -> InputColumnDataType.Decimal
                "timestamp" -> InputColumnDataType.Datetime
                else -> throw IllegalArgumentException("Not supported type: ${it.second}")
            }
        }
    }


}