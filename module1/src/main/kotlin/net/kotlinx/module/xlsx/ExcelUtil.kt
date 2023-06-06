package net.kotlinx.module.xlsx

import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.openxml4j.opc.PackageAccess
import org.apache.poi.poifs.crypt.EncryptionInfo
import org.apache.poi.poifs.crypt.EncryptionMode
import org.apache.poi.poifs.crypt.Encryptor
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import java.io.File
import java.io.FileOutputStream


object ExcelUtil {


    /** 소문자로 바꿔준다~ 체크는 안함 귀찮..
     * 97 ~ 122 : a~z --> 0부터 시작
     * 65 ~ 90   : A ~ Z */
    fun intToAlpha(i: Int): String {
        return String(charArrayOf((i + 97).toChar()))
    }

    /** 1부터 시작하는 int를 대문자 알파벳으로 바꿔준다. 귀찮아서 벨리체크 안함
     * 1-> A   */
    fun intToUpperAlpha(i: Int): String {
        return String(charArrayOf((i + 64).toChar()))
    }

    fun upperAlphaToInt(i: Char): Int {
        return i.code - 64
    }

    /** 1부터 시작하는 int를 소문자 알파벳으로 바꿔준다. 귀찮아서 벨리체크 안함
     * 1-> a   */
    fun intToLowerAlpha(i: Int): String {
        return String(charArrayOf((i + 96).toChar()))
    }

    /**
     * 엑셀 파일을 암호화함
     * orgFile 은 따로 지워주던가 할것
     */
    fun encrypt(orgFile: File, pwd: String, out: File) {
        POIFSFileSystem().use { fs ->
            val enc: Encryptor = EncryptionInfo(EncryptionMode.agile).encryptor.apply {
                confirmPassword(pwd)
            }
            OPCPackage.open(orgFile, PackageAccess.READ_WRITE).use { opc -> enc.getDataStream(fs).use { os -> opc.save(os) } }
            FileOutputStream(out).use { fos -> fs.writeFilesystem(fos) }
        }
    }

}