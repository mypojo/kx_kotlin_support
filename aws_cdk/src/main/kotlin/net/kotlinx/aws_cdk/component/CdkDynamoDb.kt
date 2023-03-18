package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkInterface
import net.kotlinx.aws_cdk.DeploymentType
import net.kotlinx.aws_cdk.util.TagUtil
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.RemovalPolicy.RETAIN
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.dynamodb.*

fun String.cdkDynamoAttS(): Attribute = Attribute.builder().name(this).type(AttributeType.STRING).build()
fun String.cdkDynamoAttN(): Attribute = Attribute.builder().name(this).type(AttributeType.NUMBER).build()

class CdkDynamoDb(
    val tableName: String,
    val deploymentType: DeploymentType,
) : CdkInterface {

    override val logicalName: String = "$tableName-${deploymentType.name}"

    var billingMode = BillingMode.PAY_PER_REQUEST
    var removalPolicy = RemovalPolicy.RETAIN

    var pk: Attribute = "pk".cdkDynamoAttS()!!
    var sk: Attribute? = "sk".cdkDynamoAttS()
    var ttl = "ttl"
    var pointInTimeRecovery = true

    var table: Table? = null

    /** 아직 삭제 보호 모드 설정이 안된다. */
    fun create(stack: Stack): CdkDynamoDb {
        table = Table(
            stack, "ddb-$logicalName", TableProps.builder()
                .tableName(logicalName)
                .billingMode(billingMode)
                .removalPolicy(RETAIN)
                .partitionKey(pk)
                .sortKey(sk)
                .timeToLiveAttribute(ttl)
                .pointInTimeRecovery(pointInTimeRecovery)
                .build()
        )

        return this
    }

    /** 로컬 기본은 키값만 */
    fun addLocalSecondaryIndex(sk: Attribute, projectionType: ProjectionType = ProjectionType.KEYS_ONLY) {
        table!!.addLocalSecondaryIndex(
            LocalSecondaryIndexProps.builder()
                .indexName("lidx-${sk.name}")
                .projectionType(projectionType)
                .sortKey(sk)
                .build()
        )
        TagUtil.tag(table!!, deploymentType)
    }

    /** 글로벌 기본은 키값만 */
    fun addGlobalSecondaryIndex(pk: Attribute, sk: Attribute, projectionType: ProjectionType = ProjectionType.KEYS_ONLY) {
        table!!.addGlobalSecondaryIndex(
            GlobalSecondaryIndexProps.builder()
                .indexName("gidx-${pk.name}-${sk.name}")
                .projectionType(projectionType)
                .partitionKey(pk)
                .sortKey(sk)
                .build()
        )
    }
}