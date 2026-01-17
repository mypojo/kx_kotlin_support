package net.kotlinx.awscdk.data

import io.kotest.matchers.shouldBe
import net.kotlinx.awscdk.lambda.addEventSourceDynamo
import net.kotlinx.kotest.modules.BeSpecHeavy
import software.amazon.awscdk.App
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.dynamodb.StreamViewType
import software.amazon.awscdk.services.lambda.Code
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.lambda.Runtime

class CdkDynamoDbTest : BeSpecHeavy() {
    init {
        Given("DynamoDB Stream 및 Trigger 설정 테스트") {
            val app = App()
            val stack = Stack(app, "TestStack")

            Then("Stream 활성화 및 Lambda Trigger 추가") {
                // 1. DDB 생성 (Stream 활성화)
                val cdkDdb = CdkDynamoDb {
                    tableName = "test-table"
                    stream = StreamViewType.NEW_AND_OLD_IMAGES
                }.create(stack)

                // 2. Lambda 함수 생성
                val testLambda = Function.Builder.create(stack, "TestLambda")
                    .runtime(Runtime.JAVA_17)
                    .handler("test.handler")
                    .code(Code.fromInline("test"))
                    .build()

                // 3. 트리거 추가 (샘플)
                testLambda.addEventSourceDynamo(cdkDdb.iTable) {
                    batchSize(5)
                }

                log.info { "DDB Stream & Lambda Trigger 설정 완료" }
            }

            Then("기존 테이블 로드 테스트 (load)") {
                val cdkDdb = CdkDynamoDb {
                    tableName = "existing-table"
                }.load(stack)

                cdkDdb.iTable.tableName shouldBe "existing-table-local" // logicalName check (suff is local by default)
                log.info { "DDB Load 완료: ${cdkDdb.iTable.tableName}" }
            }
        }
    }
}
