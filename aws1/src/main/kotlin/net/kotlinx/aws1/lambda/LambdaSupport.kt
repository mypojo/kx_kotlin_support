package net.kotlinx.aws1.lambda

import aws.sdk.kotlin.services.lambda.*
import aws.sdk.kotlin.services.lambda.model.*
import net.kotlinx.core1.string.ResultText
import net.kotlinx.core1.time.toKr01
import org.apache.commons.text.StringEscapeUtils
import java.io.File
import java.time.LocalDateTime


/**  응답을 받을때  */
suspend fun LambdaClient.invokeSynch(functionName: String, param: Any): ResultText {
    val resp: InvokeResponse = this.invoke {
        this.functionName = functionName
        this.payload = param.toString().toByteArray()
        this.invocationType = InvocationType.RequestResponse
    }
    val ok = resp.functionError == null //이게 널이면 성공 (문서확인)

    val resultJson = resp.payload?.let {
        val text = String(it) //원문은 이스케이핑된 텍스트 덩어리이다.
        StringEscapeUtils.UNESCAPE_ECMASCRIPT.translate(text).trim('"') //언이스케이핑 후 " 제거
    } ?: "{}"
    return ResultText(ok, resultJson) //결과코드는 무조건 200라인임. payload 변환 주의!. 비동기면 결과 없음
}

/** 실행만 */
suspend fun LambdaClient.invokeAsynch(functionName: String, param: Any) {
    this.invoke {
        this.functionName = functionName
        this.payload = param.toString().toByteArray()
        this.invocationType = InvocationType.Event
    }
}

//==================================================== 주로  그래들에서 사용하는거 ======================================================

/** ECR 배포 후 이거 실행해주면 반영됨 */
suspend fun LambdaClient.updateFunctionCode(functionName: String, imageUri: String, tag: String): UpdateFunctionCodeResponse {
    return this.updateFunctionCode {
        this.functionName = functionName
        this.imageUri = "$imageUri:$tag"
    }
}

/**
 * fatjar 배포 후 이거 실행해주면 반영됨.
 * Code storage 에 저장됨 (대시보드에서 남은용량 확인가능. 2023 기준 기본 75.0 GB 제공중)
 * 버전XX. 즉시 반영용.
 *  */
suspend fun LambdaClient.updateFunctionCode(functionName: String, jarFile: File): UpdateFunctionCodeResponse {
    return this.updateFunctionCode {
        this.functionName = functionName
        this.zipFile = jarFile.readBytes()
    }
}

//==================================================== 람다 버전교체 ======================================================

/**
 * alias 는  CDK에서 이미 만들어져있어야 하기 때문에 아마 없을리는 없지만 혹시나 해서 세트로 제작
 * */
suspend fun LambdaClient.publishVersionAndUpdateAlias(functionName: String, alias: String) {
    val versionResponse = publishVersion(functionName)
    val version = versionResponse.version!!
    try {
        updateAlias(functionName, version, alias)
    } catch (e: ResourceNotFoundException) {
        createAlias(functionName, version, alias)
    }
}


/**
 * 버전 하나 올림. 중간에 하나 삭제해도 삭제버전 채우지 않고 다음거로 올라감.
 * 해시가 동일하면 호출은 성공하지만 버전업은 하지 않음.
 * */
suspend fun LambdaClient.publishVersion(functionName: String): PublishVersionResponse = this.publishVersion {
    this.functionName = functionName
    this.description = "update ${LocalDateTime.now().toKr01()}"
}

/** Alias 교체. (없으면 오류남) */
suspend fun LambdaClient.updateAlias(functionName: String, functionVersion: String, alias: String): UpdateAliasResponse = this.updateAlias {
    this.functionName = functionName
    this.functionVersion = functionVersion
    this.name = alias
}

/** Alias 생성 (없으면 오류남) */
suspend fun LambdaClient.createAlias(functionName: String, functionVersion: String, alias: String): CreateAliasResponse = this.createAlias {
    this.functionName = functionName
    this.functionVersion = functionVersion
    this.name = alias
}