package net.kotlinx.spring.data

import net.kotlinx.number.minWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

/**
 * 레인지 세이프한 인메모리 페이징 처리
 * 소팅 같은 옵션은 나중에 추가하자
 */
fun <T> List<T>.page(req: Pageable): Page<T> {
    val limit = this.size.toLong()
    val start = req.offset.minWith(limit)
    val out = (req.offset + req.pageSize).minWith(limit)
    return PageImpl(
        this.subList(start.toInt(), out.toInt()), req, limit
    )
}