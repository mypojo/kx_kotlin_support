package net.kotlinx.spring.data

import org.springframework.data.domain.Pageable

/**
 * 아테나용 페이징 쿼리 접미어
 * athena 등은 JPA가 없어서 수동 쿼리를 만들어 줘야한다.
 *  */
val Pageable.pageingAthena: String
    get() = "OFFSET $offset ROWS LIMIT $pageSize"
