package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkDeploymentType
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core.DeploymentType
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.dynamodb.*

fun String.cdkDynamoAttS(): Attribute = Attribute.builder().name(this).type(AttributeType.STRING).build()
fun String.cdkDynamoAttN(): Attribute = Attribute.builder().name(this).type(AttributeType.NUMBER).build()

class CdkDynamoDb(
    val tableName: String,
    block: CdkDynamoDb.() -> Unit = {}
) : CdkDeploymentType {

    override var deploymentType: DeploymentType = DeploymentType.dev

    override val logicalName: String
        get() = "$tableName-${deploymentType.name}"

    var billingMode = BillingMode.PAY_PER_REQUEST
    var removalPolicy = RemovalPolicy.RETAIN

    var pk: Attribute = "pk".cdkDynamoAttS()
    var sk: Attribute? = "sk".cdkDynamoAttS()
    var ttl = "ttl"
    var pointInTimeRecovery = true

    lateinit var iTable: Table

    init {
        block(this)
    }

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