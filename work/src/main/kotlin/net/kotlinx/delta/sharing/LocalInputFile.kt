package net.kotlinx.delta.sharing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.avro.generic.GenericRecord
import org.apache.parquet.avro.AvroParquetReader
import org.apache.parquet.hadoop.ParquetReader
import org.apache.parquet.io.InputFile
import org.apache.parquet.io.SeekableInputStream
import java.io.EOFException
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.file.Path

/**
 * 로컬 파케이을 읽는 리더
 * 데이터브릭스 공식 파일에서 주워온거 수정함
 */
class LocalInputFile(path: Path) : InputFile {

    private val input: RandomAccessFile = try {
        RandomAccessFile(path.toFile(), "r")
    } catch (e: FileNotFoundException) {
        throw e
    }

    /** ParquetReader 로 변경 */
    fun toReader(): ParquetReader<GenericRecord> = AvroParquetReader.builder<GenericRecord>(this).build()!!

    /** Parquet 리더에서 레코드를 Flow로 읽어오는 함수 */
    fun toFlow(): Flow<GenericRecord> = flow {
        val reader = toReader()
        while (true) {
            val record = reader.read() ?: break
            emit(record)
        }
        reader.close()
    }

    override fun getLength(): Long = input.length()

    override fun newStream(): SeekableInputStream = object : SeekableInputStream() {
        private val page = ByteArray(8192)
        private var markPos = 0L

        override fun read(): Int = input.read()

        override fun read(b: ByteArray): Int = input.read(b)

        override fun read(b: ByteArray, off: Int, len: Int): Int = input.read(b, off, len)

        override fun read(byteBuffer: ByteBuffer): Int =
            readDirectBuffer(byteBuffer, page) { b, offset, length -> input.read(b, offset, length) }

        override fun skip(n: Long): Long {
            val savPos = input.filePointer
            val amtLeft = input.length() - savPos
            val skipAmount = minOf(n, amtLeft)
            val newPos = savPos + skipAmount
            input.seek(newPos)
            val curPos = input.filePointer
            return curPos - savPos
        }

        override fun available(): Int = 0

        override fun close() {
            input.close()
        }

        @Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
        private inline fun <T : Throwable, R> uncheckedExceptionThrow(t: Throwable): R {
            throw t as T
        }

        @Synchronized
        override fun mark(readlimit: Int) {
            try {
                markPos = input.filePointer
            } catch (e: IOException) {
                uncheckedExceptionThrow<IOException, Unit>(e)
            }
        }

        @Synchronized
        override fun reset() {
            input.seek(markPos)
        }

        override fun markSupported(): Boolean = true

        override fun getPos(): Long = input.filePointer

        override fun seek(l: Long) {
            input.seek(l)
        }

        override fun readFully(bytes: ByteArray) {
            input.readFully(bytes)
        }

        override fun readFully(bytes: ByteArray, i: Int, i1: Int) {
            input.readFully(bytes, i, i1)
        }

        override fun readFully(byteBuffer: ByteBuffer) {
            readFullyDirectBuffer(byteBuffer, page) { b, offset, length -> input.read(b, offset, length) }
        }

        private fun readDirectBuffer(
            byteBuffer: ByteBuffer,
            page: ByteArray,
            reader: (ByteArray, Int, Int) -> Int
        ): Int {
            // copy all the bytes that return immediately, stopping at the first
            // read that doesn't return a full buffer.
            var nextReadLength = minOf(byteBuffer.remaining(), page.size)
            var totalBytesRead = 0
            var bytesRead: Int

            while (reader(page, 0, nextReadLength).also { bytesRead = it } == page.size) {
                byteBuffer.put(page)
                totalBytesRead += bytesRead
                nextReadLength = minOf(byteBuffer.remaining(), page.size)
            }

            return when {
                bytesRead < 0 -> if (totalBytesRead == 0) -1 else totalBytesRead
                else -> {
                    // copy the last partial buffer
                    byteBuffer.put(page, 0, bytesRead)
                    totalBytesRead + bytesRead
                }
            }
        }

        private fun readFullyDirectBuffer(
            byteBuffer: ByteBuffer,
            page: ByteArray,
            reader: (ByteArray, Int, Int) -> Int
        ) {
            var nextReadLength = minOf(byteBuffer.remaining(), page.size)
            var bytesRead = 0

            while (nextReadLength > 0 && reader(page, 0, nextReadLength).also { bytesRead = it } >= 0) {
                byteBuffer.put(page, 0, bytesRead)
                nextReadLength = minOf(byteBuffer.remaining(), page.size)
            }

            if (bytesRead < 0 && byteBuffer.remaining() > 0) {
                throw EOFException(
                    "Reached the end of stream with ${byteBuffer.remaining()} bytes left to read"
                )
            }
        }
    }
}