package net.kotlinx.system

/**
 * OS별로 구분해야할것들
 * ex) 커맨드라인 문법 등..
 * 커먼즈의 SystemUtils 참고
 * */
enum class OsType {

    WINDOWS,
    MAC,
    LINUX,
    ;

    companion object {

        val OS_TYPE by lazy {
            val osName = System.getProperty("os.name").lowercase()
            when {
                osName.startsWith("windows") -> WINDOWS
                osName.startsWith("mac") -> MAC
                osName.startsWith("linux") -> LINUX
                else -> throw IllegalArgumentException("지원하지 않는 OS 입니다. $osName")
            }
        }


    }

}

