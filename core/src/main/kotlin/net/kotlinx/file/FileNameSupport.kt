package net.kotlinx.file

import java.io.File

/**
 * 파일을 패스 형식으로 나타낼때
 * ex) ResourceHolder.WORKSPACE.slash("work").slash("kotlin.html")
 * 이미 비슷한게 있긴하다. 그냥씀
 * @see File.resolve
 *  */
fun File.slash(name: String): File {
    this.mkdirs()
    check(this.isDirectory) { "이 파일은 디렉토리가 아닙니다. $this" }
    return File(this, name)
}

/** 셀프 리턴. apply 한줄 줄이려고 추가함 */
fun File.slashDir(name: String): File = slash(name).apply { this.mkdirs() }

/** 기존파일에 확장자는 그대로 두고 이름을 추가해준다. 은근 자주 사용됨 */
fun File.nameAppend(append: String): File = this.parentFile.slash("${this.nameWithoutExtension}${append}.${this.extension}")
