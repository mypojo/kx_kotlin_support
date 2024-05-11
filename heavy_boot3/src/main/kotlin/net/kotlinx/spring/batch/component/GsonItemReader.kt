package net.kotlinx.spring.batch.component

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import net.kotlinx.json.gson.GsonSet
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemStream
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream
import org.springframework.core.io.Resource
import java.io.InputStreamReader

/**
 * 대용량의 json 파일을 읽기 위한 리더
 */
class GsonItemReader<T>(
    private val gson: Gson = GsonSet.GSON,
    private val encoding: String = "UTF-8",
    private val clazz: Class<T> // 모르겠으면 LinkedHashMap.class
) : ResourceAwareItemReaderItemStream<T>, ItemReader<T>, ItemStream {

    //================= 주입 ======================
    private lateinit var resource: Resource

    //================= 내부 사용 ======================
    private lateinit var reader: JsonReader
    private var readCnt: Long = 0

    override fun open(context: ExecutionContext) {
        reader = JsonReader(InputStreamReader(resource.inputStream, encoding))
        reader.beginArray()
    }

    override fun close() {
        reader.endArray()
        reader.close()
    }

    override fun update(context: ExecutionContext) {
        //아무것도 안함
    }

    @Synchronized
    override fun read(): T? {
        if (!reader.hasNext()) return null
        readCnt++
        return gson.fromJson(reader, clazz)
    }

    override fun setResource(resource: Resource) {
        this.resource = resource
    }
}
