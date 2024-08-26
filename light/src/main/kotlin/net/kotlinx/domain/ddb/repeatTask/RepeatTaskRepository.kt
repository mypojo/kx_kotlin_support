package net.kotlinx.domain.ddb.repeatTask

import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.dynamo.deleteItem
import net.kotlinx.aws.dynamo.getItem
import net.kotlinx.aws.dynamo.putItem
import net.kotlinx.aws.dynamo.query.query
import net.kotlinx.domain.ddb.DdbBasic
import net.kotlinx.domain.ddb.DdbBasicConverter
import net.kotlinx.koin.Koins.koinLazy

/** 기본 저장소 */
class RepeatTaskRepository(
    private val profile: String? = null,
    private val converter: DdbBasicConverter<DdbBasic, RepeatTask> = RepeatTaskConverter(),
) {

    private val aws by koinLazy<AwsClient1>(profile)

    //==================================================== 기본 메소드 ======================================================
    suspend fun getItem(task: RepeatTask): RepeatTask? {
        val query = converter.createBasic(task)
        val ddb = aws.dynamo.getItem(query) ?: return null
        return converter.convertTo(ddb)
    }

    suspend fun putItem(task: RepeatTask) {
        val ddb = converter.convertFrom(task)
        aws.dynamo.putItem(ddb)
    }

    suspend fun deleteItem(task: RepeatTask) {
        val query = converter.createBasic(task)
        aws.dynamo.deleteItem(query)
    }

    //==================================================== 기본 쿼리 ======================================================

    /**
     * PK / SK 기반으로 조회
     *  */
    suspend fun findBy(task: RepeatTask): List<RepeatTask> {
        val query = converter.createBasic(task)
        val dynamoResult = aws.dynamo.query(query)
        return dynamoResult.datas.map { converter.convertTo(it) }
    }

//
//    /** 전체 조회 */
//    suspend fun findAllByPk(jobDef: RepeatTaskDefinition, block: DynamoQuery.() -> Unit = {}): List<RepeatTask> {
//        val dynamoQuery = DynamoQuery {
//            createParamAndQuery = {
//                buildMap {
//                    put(":${DynamoDbBasic.PK}", AttributeValue.S(jobDef.jobPk))
//                }
//            }
//            block()
//        }
//        return aws.dynamo.queryAll(dynamoQuery, EMPTY)
//    }
//
//    //==================================================== 인덱스 쿼리 ======================================================
//
//    /**
//     * 상태 / PK 기준으로 조회
//     * ex) 실패잡 x건
//     * 보통 모니터링에 사용됨
//     *  */
//    suspend fun findByStatusPk(
//        jobStatus: RepeatTaskStatus,
//        jobDef: RepeatTaskDefinition? = null,
//        last: Map<String, AttributeValue>? = null,
//        block: DynamoQuery.() -> Unit = {}
//    ): DynamoResult<RepeatTask> {
//        return aws.dynamo.query(EMPTY) {
//            indexName = RepeatTaskIndexUtil.GID_STATUS
//            scanIndexForward = false //최근 데이터 우선
//            select = Select.AllProjectedAttributes
//            createParamAndQuery = {
//                buildMap {
//                    put(":${RepeatTask::jobStatus.name}", AttributeValue.S(jobStatus.name))
//                    jobDef?.let { put(":${DynamoDbBasic.PK}", AttributeValue.S(jobDef.jobPk)) }
//                }
//            }
//            exclusiveStartKey = last
//            block()
//        }
//    }


}