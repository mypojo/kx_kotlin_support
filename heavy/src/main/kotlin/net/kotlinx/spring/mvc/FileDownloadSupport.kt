package net.kotlinx.spring.mvc

import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.io.File


/** 스프링부트 오피셜 다운로드 */
@Deprecated("s3 프리사인 사용하세요")
fun File.download(): ResponseEntity<Resource> {
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .cacheControl(CacheControl.noCache())
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + this.name)
        .body(FileSystemResource(this))
}