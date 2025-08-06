package net.kotlinx.domain.jpa


/** 메타데이터 생성은 각 로직별로 다르다. */
interface BasicMetadataFactory {

    fun createMetadata(): BasicMetadata

}

