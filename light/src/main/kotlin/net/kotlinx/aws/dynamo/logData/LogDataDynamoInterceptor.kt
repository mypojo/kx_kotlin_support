package net.kotlinx.aws.dynamo.logData

import aws.sdk.kotlin.services.dynamodb.model.*
import aws.smithy.kotlin.runtime.client.ResponseInterceptorContext
import aws.smithy.kotlin.runtime.http.interceptors.HttpInterceptor
import aws.smithy.kotlin.runtime.http.request.HttpRequest
import aws.smithy.kotlin.runtime.http.response.HttpResponse
import mu.KotlinLogging
import net.kotlinx.aws.dynamo.DynamoMapUtil
import net.kotlinx.aws.firehose.logData.LogDataHolder
import net.kotlinx.json.gson.GsonData

/**
 * DynamoDB 저장을 로깅하는 인터셉터
 */
class LogDataDynamoInterceptor : HttpInterceptor {

    override suspend fun modifyBeforeCompletion(context: ResponseInterceptorContext<Any, Any, HttpRequest?, HttpResponse?>): Result<Any> {
        try {
            when (val input = context.request) {
                is PutItemRequest -> {
                    val response = context.response as PutItemResponse
                    LogDataHolder.addData {
                        g1 = FROM_NAME
                        g2 = input.tableName ?: ""
                        g3 = DIV_INSERT
                        keyword = keyword(input.item)

                        // 1. 기존 값 (Old Item): ReturnValues.AllOld 설정 시 응답에 포함됨
                        val oldItemMap = response?.attributes ?: emptyMap()
                        if (oldItemMap.isNotEmpty()) {
                            x = GsonData.fromObj(DynamoMapUtil.fromAttributeMap(AttributeValue.M(oldItemMap)))
                        }

                        y = GsonData.fromObj(input.item?.let { DynamoMapUtil.fromAttributeMap(AttributeValue.M(it)) } ?: emptyMap())
                    }
                }

                is UpdateItemRequest -> {
                    val response = context.response as UpdateItemResponse
                    LogDataHolder.addData {
                        g1 = FROM_NAME
                        g2 = input.tableName ?: ""
                        g3 = DIV_UPDATE
                        keyword = keyword(input.key)

                        // 기존 값 로깅 (ReturnValues.AllOld 설정 시)
                        val oldItemMap = response?.attributes ?: emptyMap()
                        if (oldItemMap.isNotEmpty()) {
                            x = GsonData.fromObj(DynamoMapUtil.fromAttributeMap(AttributeValue.M(oldItemMap)))
                        }

                        // 업데이트 내역은 y에 담기 (단축 표현)
                        y = GsonData.fromObj(input.attributeUpdates?.mapValues { it.value.value?.let { av -> DynamoMapUtil.fromAttributeMapValue(av) } } ?: emptyMap())
                    }
                }

                is DeleteItemRequest -> {
                    val response = context.response as DeleteItemResponse
                    LogDataHolder.addData {
                        g1 = FROM_NAME
                        g2 = input.tableName ?: ""
                        g3 = DIV_DELETE
                        keyword = keyword(input.key)

                        // 기존 값 로깅 (ReturnValues.AllOld 설정 시)
                        val oldItemMap = response?.attributes ?: emptyMap()
                        if (oldItemMap.isNotEmpty()) {
                            x = GsonData.fromObj(DynamoMapUtil.fromAttributeMap(AttributeValue.M(oldItemMap)))
                        }
                    }
                }

                is BatchWriteItemRequest -> {
                    input.requestItems?.forEach { (tableName, requests) ->
                        requests.forEach { request ->
                            request.putRequest?.let { put ->
                                LogDataHolder.addData {
                                    g1 = FROM_NAME
                                    g2 = tableName
                                    g3 = DIV_INSERT
                                    keyword = keyword(put.item)
                                    y = GsonData.fromObj(put.item?.let { DynamoMapUtil.fromAttributeMap(AttributeValue.M(it)) } ?: emptyMap())
                                }
                            }
                            request.deleteRequest?.let { del ->
                                LogDataHolder.addData {
                                    g1 = FROM_NAME
                                    g2 = tableName
                                    g3 = DIV_DELETE
                                    keyword = keyword(del.key)
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        } catch (e: Exception) {
            log.warn(e) { "DynamoDB Interceptor logging failed" }
        }
        return Result.success(context.response!!)
    }

    private fun keyword(map: Map<String, AttributeValue>?): String {
        if (map == null) return ""
        val pk = map["pk"]?.let { if (it.asSOrNull() != null) it.asS() else it.toString() }
        val sk = map["sk"]?.let { if (it.asSOrNull() != null) it.asS() else it.toString() }
        return listOfNotNull(pk, sk).joinToString("-")
    }

    companion object {
        private val log = KotlinLogging.logger {}

        private const val FROM_NAME = "dynamo"

        private const val DIV_INSERT = "I"
        private const val DIV_UPDATE = "U"
        private const val DIV_DELETE = "D"
    }
}