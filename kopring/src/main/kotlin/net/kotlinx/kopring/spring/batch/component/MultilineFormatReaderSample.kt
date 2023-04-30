package net.kotlinx.kopring.spring.batch.component

import net.kotlinx.core1.regex.RegexSet
import net.kotlinx.core2.gson.GsonData
import net.kotlinx.kopring.spring.toResource
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.mapping.PassThroughLineMapper
import java.io.File


object MultilineFormatReaderSample {

    /** 샘플 01번.  요런형태 가끔 보임 */
    fun xml01(file: File): MultilineFormatReader<String, GsonData> {

        val idPattern = RegexSet.extract("<g:", ">").toRegex()
        val valuePattern = RegexSet.extract(">", "<").toRegex()

        return MultilineFormatReader(
            itemReader = FlatFileItemReader<String>().apply {
                setResource(file.toResource())
                setLineMapper(PassThroughLineMapper())
                setEncoding(Charsets.UTF_8.name())
            },
            isItemEnd = { it.trim() == "</entry>" },
            converter = { lines ->
                GsonData.obj().apply {
                    for (line in lines) {
                        val id = idPattern.find(line)?.value ?: continue
                        val value = valuePattern.find(line)?.value ?: continue  //CDATA 는 향후 체크
                        this.put(id, value)
                    }
                }
            }
        )

    }

}
