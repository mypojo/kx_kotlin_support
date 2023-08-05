package net.kotlinx.module.dynamoLock

import com.amazonaws.services.dynamodbv2.AcquireLockOptions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBLockClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBLockClientOptions
import com.amazonaws.services.dynamodbv2.LockItem
import net.kotlinx.aws.javaSdkv2.toJavaAttributeValue
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.util.concurrent.TimeUnit

/**
 * AWS에서 서버리스로 사용 가능한 분산 락 처리기
 * https://aws.amazon.com/ko/blogs/database/building-distributed-locks-with-the-dynamodb-lock-client/
 * https://github.com/awslabs/amazon-dynamodb-lock-client
 *
 * withShouldSkipBlockingWait : true 로 하면 expire 체크 안하는듯.
 *
 * ## 사용법 ##
 * enum 으로 div를 정의한 후 spring transaction 등과 묶어서 사용하면 됨
 */
class DynamoLockModule(
    private val dynamoDbClient: DynamoDbClient,
    block: DynamoLockModule.() -> Unit = {}
) {

    //==================================================== 필수 ======================================================
    /** DDB 테이블명 */
    var tableName: String = "dist_lock"

    /** DDB pk */
    var partitionKeyName: String = "pk"

    /** DDB sk */
    var sortKeyName: String = "sk"

    /** 락 오너 = 머신  */
    var ownerName: String = "unknown"

    /** 락 대여기간 단위. 자바버전이 베이스라 그냥 이렇게 함  */
    var timeUnit: TimeUnit = TimeUnit.SECONDS

    /** 백그라운드에서 하트비트 날릴지 */
    var createHeartbeatBackgroundThread: Boolean = true

    /** 락 대여기간  */
    var leaseDuration: Long = 60

    /** 허트비트 체크 기간  */
    var heartbeatPeriod: Long = leaseDuration / 2

    /** 디폴트 타임아웃  */
    var defaultTimeout: Long = leaseDuration * 5

    init {
        block(this)
    }

    /** 락 클라이언트 생성 */
    private val lockClient: AmazonDynamoDBLockClient by lazy {
        AmazonDynamoDBLockClient(
            AmazonDynamoDBLockClientOptions.builder(dynamoDbClient, tableName)
                .withPartitionKeyName(partitionKeyName)
                .withSortKeyName(sortKeyName)
                .withTimeUnit(timeUnit)
                .withLeaseDuration(leaseDuration)
                .withHeartbeatPeriod(heartbeatPeriod)
                .withCreateHeartbeatBackgroundThread(createHeartbeatBackgroundThread)
                .withOwnerName(ownerName)
                .withHoldLockOnServiceUnavailable(false)
                .build()
        )
    }

    //==================================================== 락 확보 ======================================================

    /** DSL 버전 */
    fun acquireLock(block: DynamoLockReq.() -> Unit): LockItem {
        val req = DynamoLockReq().apply(block)
        return acquireLock(req)
    }

    /**
     * 락을 확보하면 data를 제외하고 신규 값으로 다 변경된다.
     * @return 타임아웃 발생시 빈값 리턴함
     */
    fun acquireLock(req: DynamoLockReq): LockItem {
        val timeout = req.timeout ?: defaultTimeout
        val option = AcquireLockOptions.builder(req.pk).withSortKey(req.sk)
            .withReplaceData(false)
            .withDeleteLockOnRelease(true)
            .withAdditionalAttributes(req.toAttribute().toJavaAttributeValue())
            .withAdditionalTimeToWaitForLock(timeout).withTimeUnit(timeUnit)
            .build()
        return lockClient.acquireLock(option)
    }

}