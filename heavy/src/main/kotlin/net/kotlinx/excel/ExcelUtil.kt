package net.kotlinx.excel

import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.openxml4j.opc.PackageAccess
import org.apache.poi.poifs.crypt.EncryptionInfo
import org.apache.poi.poifs.crypt.EncryptionMode
import org.apache.poi.poifs.crypt.Encryptor
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import java.io.File
import java.io.FileOutputStream


object ExcelUtil {

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