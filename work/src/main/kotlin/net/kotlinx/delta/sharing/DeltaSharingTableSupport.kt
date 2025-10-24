package net.kotlinx.delta.sharing

import io.delta.sharing.client.model.Table
import net.kotlinx.string.toTextGridPrint

/**
 * Delta Sharing 테이블 목록을 텍스트 그리드로 출력
 *
 * 사용 예시:
 * ```kotlin
 * tables.printTableGrid()
 * ```
 */
fun List<Table>.printTableGrid() {
    listOf("name", "share", "schema").toTextGridPrint {
        this@printTableGrid.map { table ->
            arrayOf(table.name(), table.share(), table.schema())
        }
    }
}
