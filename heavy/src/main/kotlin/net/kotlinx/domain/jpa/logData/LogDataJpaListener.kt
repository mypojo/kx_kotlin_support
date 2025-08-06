package net.kotlinx.domain.jpa.logData

import com.google.common.collect.Sets
import net.kotlinx.aws.firehose.logData.LogDataHolder
import net.kotlinx.hibernate.config.AbstractJpaPostListener
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.GsonSet
import net.kotlinx.string.toSnakeFromCamel
import org.hibernate.event.spi.PostDeleteEvent
import org.hibernate.event.spi.PostInsertEvent
import org.hibernate.event.spi.PostUpdateEvent

/**
 * 모든 이벤트는 예외 발생하지 않게 래핑
 */
class LogDataJpaListener : AbstractJpaPostListener() {

    /**
     * 엔티티에 적용할 gson
     * GSON 무한루프 돈다면 @field:NotExpose 달기
     *  */
    var gson = GsonSet.TABLE_UTC_WITH_ZONE

    /** 데이터 수정 */
    override fun onPostUpdate(event: PostUpdateEvent) {
        LogDataHolder.addData {
            g1 = FROM_NAME
            g2 = event.persister.entityName
            g3 = DIV_UPDATE
            keyword = event.id.toString()
            x = GsonData.obj {
                val properties = event.persister.propertyNames
                val oldState = event.oldState
                val state = event.state
                for (dirtyProperty in event.dirtyProperties) {
                    val fieldName = properties[dirtyProperty].toSnakeFromCamel() //아테나 내장 json 변환 때문에 키값은 카멜라이즈 해준다.
                    if (ignoreFieldNames.contains(fieldName)) continue

                    val oldValue = oldState[dirtyProperty]
                    val newValue = state[dirtyProperty]

                    //엔티티 확인
                    put(fieldName, GsonData.obj {
                        put("a", LogDataJpaUtil.entityDataToSimpleString(oldValue))
                        put("b", LogDataJpaUtil.entityDataToSimpleString(newValue))
                    })
                }
            }
            y = GsonData.fromObj(event.entity, gson)
        }
    }

    /** 데이터 입력  */
    override fun onPostInsert(event: PostInsertEvent) {
        LogDataHolder.addData {
            g1 = FROM_NAME
            g2 = event.persister.entityName
            g3 = DIV_INSERT
            keyword = event.id.toString()
            x = GsonData.empty()
            y = GsonData.fromObj(event.entity, gson)
        }
    }

    /** 데이터 삭제  */
    override fun onPostDelete(event: PostDeleteEvent) {
        LogDataHolder.addData {
            g1 = FROM_NAME
            g2 = event.persister.entityName
            g3 = DIV_DELETE
            keyword = event.id.toString()
            x = GsonData.empty()
            y = GsonData.empty()
        }
    }


    /** 실제 static한 구현 내용 */
    companion object {

        private const val FROM_NAME = "jpa"

        private const val DIV_INSERT = "I"
        private const val DIV_UPDATE = "U"
        private const val DIV_DELETE = "D"

        /** 수정 메타데이터 등은 스킵  */
        private val ignoreFieldNames: Set<String> = Sets.newHashSet("update")

    }
}