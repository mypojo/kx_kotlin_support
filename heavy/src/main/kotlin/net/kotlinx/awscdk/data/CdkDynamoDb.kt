package net.kotlinx.awscdk.data

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.dynamodb.*

fun String.cdkDynamoAttS(): Attribute = Attribute.builder().name(this).type(AttributeType.STRING).build()
fun String.cdkDynamoAttN(): Attribute = Attribute.builder().name(this).type(AttributeType.NUMBER).build()

class CdkDynamoDb : CdkInterface {

    @Kdsl
    constructor(block: CdkDynamoDb.() -> Unit = {}) {
        apply(block)
    }

    override val logicalName: String
        get() = "$tableName-${deploymentType.name.lowercase()}"

    /** 테이블명 */
    lateinit var tableName: String
    var billingMode = BillingMode.PAY_PER_REQUEST
    var removalPolicy = RemovalPolicy.RETAIN

    /** 보통 고정값 사용 */
    var pk: Attribute = "pk".cdkDynamoAttS()

    /** 보통 고정값 사용 */
    var sk: Attribute? = "sk".cdkDynamoAttS()

    /** 보통 고정값 사용 */
    var ttl = "ttl"

    /**
     * 30일 정도 자동 복구 가능하게 저장된다.
     * 이거 이상 복구하려면 AWS backup 사용할것
     *  */
    var pointInTimeRecovery = true

    /** 결과 */
    lateinit var iTable: Table

    /** 아직 삭제 보호 모드 설정이 안된다. */
    fun create(stack: Stack, block: TableProps.Builder.() -> Unit = {}): CdkDynamoDb {
        iTable = Table(
            stack, "ddb-$logicalName", TableProps.builder()
                .tableName(logicalName)
                .billingMode(billingMode)
                .removalPolicy(RemovalPolicy.RETAIN)
                .partitionKey(pk)
                .sortKey(sk)
                .timeToLiveAttribute(ttl)
                .pointInTimeRecovery(pointInTimeRecovery)
                .apply(block)
                .build()
        )
        return this
    }

    fun addLocalSecondaryIndex(sk: String, projectionType: ProjectionType = ProjectionType.KEYS_ONLY) = addLocalSecondaryIndex(sk.cdkDynamoAttS(), projectionType)

    /** 로컬 기본은 키값만 */
    fun addLocalSecondaryIndex(sk: Attribute, projectionType: ProjectionType = ProjectionType.KEYS_ONLY) {
        iTable.addLocalSecondaryIndex(
            LocalSecondaryIndexProps.builder()
                .indexName("lidx-${sk.name}")
                .projectionType(projectionType)
                .sortKey(sk)
                .build()
        )

        TagUtil.tag(iTable, deploymentType)
    }

    fun addGlobalSecondaryIndex(pk: String, sk: String, projectionType: ProjectionType = ProjectionType.KEYS_ONLY) =
        addGlobalSecondaryIndex(pk.cdkDynamoAttS(), sk.cdkDynamoAttS(), projectionType)

    /** 글로벌 기본은 키값만 */
    fun addGlobalSecondaryIndex(pk: Attribute, sk: Attribute, projectionType: ProjectionType = ProjectionType.KEYS_ONLY) {
        iTable.addGlobalSecondaryIndex(
            GlobalSecondaryIndexProps.builder()
                .indexName("gidx-${pk.name}-${sk.name}")
                .projectionType(projectionType)
                .partitionKey(pk)
                .sortKey(sk)
                .build()
        )
    }
}