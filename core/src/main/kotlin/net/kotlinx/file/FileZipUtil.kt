package net.kotlinx.file

import java.io.*
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * File관련이 너무 커져서 별도로 뺌
 * 좀더 복잡한건 이거 쓰지말고 Zip4jModule 참고할것
 */
object FileZipUtil {

    /** 압축 확장자 */
    val ZIP_EXT = setOf(".zip", ".gz")

    const val BUFFER_SIZE = 4096
    const val COMPRESSION_LEVEL = 8

    /**
     * zip 파일의 메타데이터 참고용.
     * 이걸로는 inputStream 생성이 불가능하다.
     * 반대로 스트림의 엗트리는 사이즈 제공이 안됨.. 원래 이래???
     * ex) Map<String></String>, ZipEntry> entryMap = FileZipUtil.metadatas(file).stream().collect(Collectors.toMap(v -> v.getName(), v -> v));
     * 주의!  File 객체만 된다. S3 등의 스트림은 안됨
     */
    fun metadatas(file: File): List<ZipEntry> {
        val zipFile = ZipFile(file)
        val entries = zipFile.entries()
        val entFiles: MutableList<ZipEntry> = ArrayList()
        while (entries.hasMoreElements()) {
            val entry: ZipEntry = entries.nextElement()
            entFiles.add(entry)
        }
        return entFiles
    }

    /**
     * 디렉토리를 통째로 압축한다.
     * 1.파일이 없는 디렉토리는 무시함
     * 2.디렉토리 구조는 유지해줌
     * ex) C:\DATA\WORK\ziptest한글  ==> C:\DATA\WORK\ziptest한글.zip  으로 디렉토리 전체 압축
     */
    fun zipDirectory(zipDirectory: File): File {
        require(zipDirectory.isDirectory) { "input is not directory" }
        val outZipFile = File(zipDirectory.parentFile, zipDirectory.name + ".zip")
        val values = zipDirectory.listFiles()!!
        val files = Stream.of(*values).filter { obj: File? -> obj!!.isFile }.collect(Collectors.toList())
        zip(outZipFile, zipDirectory, files)
        return outZipFile
    }

    fun zip(outZipFile: File, files: Collection<File>) {
        zip(outZipFile, null, files)
    }

    /** 자바 기본패키지로 압축한다. 한글도 잘됨.   */
    fun zip(outZipFile: File, rootpath: File?, files: Collection<File>) {
        val rootpathLength = rootpath?.absolutePath?.length ?: 0
        val buf = ByteArray(BUFFER_SIZE)
        ZipOutputStream(FileOutputStream(outZipFile)).use { zipStream ->
            for (each in files) {
                if (!each.isFile) continue
                if (each.name.startsWith(".")) continue
                val ins = FileInputStream(each)
                ins.use {
                    val path = if (rootpathLength == 0) each.name else each.absolutePath.substring(rootpathLength) //rootpath 기준으로 엔트리 명 수정
                    zipStream.putNextEntry(ZipEntry(path))
                    var len: Int
                    while (ins.read(buf).also { len = it } > 0) {
                        zipStream.write(buf, 0, len)
                    }
                    zipStream.closeEntry()
                }
            }
        }
    }

    /** 한개씩 압축할때  */
    fun zip(outZipFile: File, file: File) {
        zip(outZipFile, file.parentFile, listOf(file))
    }

    //============================================= 압축 풀기 ==========================================
    /** 클래스패스에서 다이렉트로 파일압축 풀때 사용  */
    fun unzip(ins: InputStream, out: OutputStream) {
        val buffer = ByteArray(BUFFER_SIZE)
        ZipInputStream(ins).use { zipStream ->
            while (zipStream.nextEntry != null) {
                var length: Int
                while (zipStream.read(buffer).also { length = it } != -1) {
                    out.write(buffer, 0, length)
                }
                out.flush()
            }
        }
    }

    /**
     * 자바 기본패키지로 압축해제한다. 이름 중복시 덮어쓰지 않고 이름을 변경하여 저장한다.
     */
    /** 현재폴더에 압축을 푼다.  */
    fun unzip(zip: File, dir: File = zip.parentFile) {
        if (!dir.exists()) dir.mkdirs()
        require(dir.isDirectory) { dir.toString() + "is not directory" }
        val buffer = ByteArray(BUFFER_SIZE)
        FileInputStream(zip).use { fileIn ->
            val zipStream = ZipInputStream(fileIn)
            var entry: ZipEntry
            while (zipStream.nextEntry.also { entry = it } != null) {
                val outputFileNm = entry.name
                doUnzip(dir, buffer, zipStream, outputFileNm)
            }
            zipStream.close()
        }
    }

    private fun doUnzip(dir: File, buffer: ByteArray, zipStream: ZipInputStream, outputFileNm: String) {
        val entryFile = File(dir, outputFileNm)

        // 압축을 폴더 안에다 한 경우 폴더를 생성해 준다.
        val parent = entryFile.parentFile
        if (!parent.exists()) parent.mkdirs()

        // 동일한 이름이 있을 경우 이름을 바꿔 준다.
        if (entryFile.exists()) throw IllegalArgumentException(entryFile.absolutePath)
        val fileOut = FileOutputStream(entryFile)
        var length: Int
        while (zipStream.read(buffer).also { length = it } != -1) {
            fileOut.write(buffer, 0, length)
        }
        fileOut.flush()
        fileOut.close()
    }

}
