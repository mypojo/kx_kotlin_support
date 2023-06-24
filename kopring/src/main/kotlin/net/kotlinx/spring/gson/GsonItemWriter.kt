package net.kotlinx.spring.gson

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.stream.JsonWriter
import net.kotlinx.core.gson.GsonSet
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemStream
import org.springframework.batch.item.ItemStreamException
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.ResourceAwareItemWriterItemStream
import org.springframework.core.io.Resource
import org.springframework.core.io.WritableResource
import java.io.IOException
import java.io.OutputStreamWriter

/**
 * 대용량 json writer
 * 아래처럼 인코딩 설정을 추가로 넣어줄것
 * ex) resp.setHeader("content-type", "text/html; charset=utf-8");
 */
class GsonItemWriter<T>(
    private val gson: Gson = GsonSet.GSON,
    private val encoding: String = "UTF-8",
) : ResourceAwareItemWriterItemStream<T>, ItemWriter<T>, ItemStream {
    //================= 설정 ======================
    private lateinit var resource: Resource
    var header: JsonElement? = null
    var footer: JsonElement? = null

    //================= 내부 사용 ======================
    private lateinit var writer: JsonWriter
    private var writeCnt: Long = 0

    override fun open(arg0: ExecutionContext) {
        try {
            val writableResource = resource as WritableResource
            writer = JsonWriter(OutputStreamWriter(writableResource.outputStream, encoding))
            writer.setIndent("  ")
            writer.beginArray()

            header?.let { gson.toJson(it, writer) }
        } catch (e: Exception) {
            throw ItemStreamException(e)
        }
    }

    override fun update(executionContext: ExecutionContext) {
        //아무것도 하지않음
    }

    @Throws(ItemStreamException::class)
    override fun close() {
        try {
            footer?.let { gson.toJson(it, writer) }
            writer.endArray()
            writer.close()
        } catch (e: IOException) {
            throw ItemStreamException(e)
        }
    }

    override fun setResource(resource: Resource) {
        this.resource = resource
    }


    /** 동기화 필수  */
    @Synchronized
    override fun write(items: List<T>) {
        for (item in items) {
            val json = gson.toJsonTree(item)
            gson.toJson(json, writer)
        }
        writeCnt += items.size.toLong()
    }
}
