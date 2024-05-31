package net.kotlinx.aws


object AwsCommandUtil {

    /**
     * @param profile null인경우 공백문자 리턴
     * @return 커맨드 명령어에 추가될 프로파일 옵션을 리턴
     *  */
    fun profile(profile: String?): String = profile?.let { "--profile $it" } ?: ""

}