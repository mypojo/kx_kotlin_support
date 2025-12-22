package net.kotlinx.notion

/**
 * 노션 블록 리스트에 대한 지원 도구
 *  */
fun List<NotionBlock>.applyListOrders(): List<NotionBlock> {
    val listCounters = mutableMapOf<Int, Int>() // depth 별 리스트 순서 추적
    val tableActive = mutableMapOf<Int, Boolean>() // depth 별 테이블 활성화 여부

    return this.map { block ->
        // 리스트 번호 처리
        val block1 = if (block.type == "numbered_list_item") {
            val currentCount = (listCounters[block.depth] ?: 0) + 1
            listCounters[block.depth] = currentCount
            block.copy(listOrder = currentCount)
        } else {
            val depthToClear = listCounters.keys.filter { it >= block.depth }
            depthToClear.forEach { listCounters.remove(it) }
            block
        }

        // 테이블 헤더 처리
        if (block1.type == "table_row") {
            if (tableActive[block1.depth] != true) {
                tableActive[block1.depth] = true
                block1.copy(isTableHeader = true)
            } else {
                block1
            }
        } else {
            val depthToClear = tableActive.keys.filter { it >= block1.depth }
            depthToClear.forEach { tableActive.remove(it) }
            block1
        }
    }
}
