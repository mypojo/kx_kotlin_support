package net.kotlinx.system

/**
 * OS별로 구분해야할것들
 * ex) 커맨드라인 문법 등..
 * 커먼즈의 SystemUtils 참고했음
 * */
enum class OsType {

    WINDOWS,
    MAC,
    LINUX,
    ;

    @Deprecated("command 사용하세요")
    fun toGradleCommand(command: String): List<String> = when (this) {
        WINDOWS -> listOf("cmd", "/c", command)
        LINUX -> listOf("bash", "-c", command)
        MAC -> listOf("bash", "-c", command) // mac 은 잘 모름
    }

    /**
     * os에 따른 커멘드 라인을 리턴해줌
     * ex) commandLine(build.command(command))
     * 일반 js build 명령은 cmd 등이 앞에 있어야 하고 AWS 호출은 cmd 없어도 됨..
     * 한번에 한개의 커맨드만 exec{} 안에 둘것!
     * js 번들링의 경우 각 플젝 루트에서 실행하면 됨
     * ex) npx vite build
     * ex) aws s3 sync ${project(":demo-svelte").projectDir}\dist s3://demo.kotlinx.net/
     * */
    fun command(): List<String> = when (this) {
        WINDOWS -> listOf("cmd", "/c")
        LINUX -> listOf("bash", "-c")
        MAC -> listOf("bash", "-c") // mac 은 잘 모름
    }

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

