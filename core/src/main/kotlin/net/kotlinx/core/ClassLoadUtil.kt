package net.kotlinx.core


object ClassLoadUtil {

    /** 해당 클래스를 로드해서 있는지 여부를 체크함 */
    fun exist(classFullPath: String): Boolean = try {
        Class.forName(classFullPath)
        true
    } catch (_: ClassNotFoundException) {
        false
    }

}