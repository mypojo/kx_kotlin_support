package net.kotlinx.domain.job

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.dynamo.DynamoMapUtil
import net.kotlinx.aws.dynamo.enhanced.DbConverter
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.aws.dynamo.find
import net.kotlinx.aws.dynamo.findJson
import net.kotlinx.aws.dynamo.findOrThrow
import net.kotlinx.json.gson.GsonSet
import net.kotlinx.time.toIso

/**
 * DDB에 입력되는 메타데이터
 * https://www.notion.so/mypojo/Job-Module-serverless-docker-57e773b5f0494fb59dcbff5d9a8eb8f5
 *
 * 주의!! 향후 신규 작업시 ErrorLogConverter 참고해서 코드 리팩토링 하기!
 */
class JobConverter(private val table: DbTable) : DbConverter<Job> {

    override fun toAttribute(item: Job): Map<String, AttributeValue> {
        return buildMap {
            put(table.pkName, AttributeValue.S(item.pk))
            put(table.skName, AttributeValue.S(item.sk))

            //==================================================== 최초 생성시 필수 입력값 ======================================================
            put(Job::reqTime.name, AttributeValue.S(item.reqTime.toIso()))
            put(Job::memberId.name, AttributeValue.S(item.memberId))
            put(Job::ttl.name, AttributeValue.N(item.ttl.toString()))
            put(Job::memberReqTime.name, AttributeValue.S(item.memberReqTime)) //인덱스용 입력
            put(Job::jobStatus.name, AttributeValue.S(item.jobStatus.name))
            put(Job::jobExeFrom.name, AttributeValue.S(item.jobExeFrom.name))
            put(Job::jobContext.name, AttributeValue.S(item.jobContext.toString()))
            put(Job::jobOption.name, AttributeValue.S(item.jobOption.toString()))
            // 스레드 안전한 맵 컨텍스트(Map으로 저장)
            put(Job::jobContextMap.name, DynamoMapUtil.toAttribute(item.jobContextMap))

            //==================================================== 공통 시스템 자동(필수) 입력값 ======================================================
            item.startTime?.let { put(Job::startTime.name, AttributeValue.S(it.toIso())) }
            item.updateTime?.let { put(Job::updateTime.name, AttributeValue.S(it.toIso())) }
            item.endTime?.let { put(Job::endTime.name, AttributeValue.S(it.toIso())) }

            item.jobErrMsg?.let { put(Job::jobErrMsg.name, AttributeValue.S(it)) }
            item.instanceMetadata?.let { put(Job::instanceMetadata.name, AttributeValue.S(GsonSet.GSON.toJson(it))) }

            item.sfnId?.let { put(Job::sfnId.name, AttributeValue.S(it)) }
            item.lastSfnId?.let { put(Job::lastSfnId.name, AttributeValue.S(it)) }
            item.jobEnv?.let { put(Job::jobEnv.name, AttributeValue.S(it)) }
        }
    }

    override fun fromAttributeMap(map: Map<String, AttributeValue>): Job {
        return Job(
            map[table.pkName]!!.asS(), map[table.skName]!!.asS()
        ) {
            //==================================================== 최초 생성시 필수 입력값 ======================================================
            reqTime = map.findOrThrow(Job::reqTime)
            memberId = map.findOrThrow(Job::memberId)
            ttl = map.findOrThrow(Job::ttl)
            jobStatus = map.findOrThrow(Job::jobStatus)
            jobExeFrom = map.findOrThrow(Job::jobExeFrom)
            jobContext = map.findOrThrow(Job::jobContext)
            jobContextMap = DynamoMapUtil.fromAttributeMap(map[Job::jobContextMap.name])

            //==================================================== 공통 시스템 자동(필수) 입력값 ======================================================
            startTime = map.find(Job::startTime)
            updateTime = map.find(Job::updateTime)
            endTime = map.find(Job::endTime)

            jobErrMsg = map.find(Job::jobErrMsg)
            instanceMetadata = map.findJson(Job::instanceMetadata)
            jobOption = map.findOrThrow(Job::jobOption)
            sfnId = map.find(Job::sfnId)
            lastSfnId = map.find(Job::lastSfnId)
            jobEnv = map.find(Job::jobEnv)
        }
    }

}