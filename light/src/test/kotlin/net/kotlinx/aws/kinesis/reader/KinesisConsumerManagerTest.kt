package net.kotlinx.aws.kinesis.reader

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.GetItemResponse
import aws.sdk.kotlin.services.kinesis.KinesisClient
import aws.sdk.kotlin.services.kinesis.model.*
import aws.smithy.kotlin.runtime.time.Instant
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.kinesis.kinesis
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import kotlin.time.Duration.Companion.seconds

/**
 * KinesisConsumerManager 클래스의 테스트
 * 주요 테스트 케이스:
 * 1. 컨슈머 매니저 시작 및 종료
 * 2. 샤드 관리 (새로운 샤드 추가, 종료된 샤드 제거)
 * 3. 레코드 처리
 */
class KinesisConsumerManagerTest : BeSpecLight() {
    
    companion object {
        private val log = KotlinLogging.logger {}
    }

    init {
        initTest(KotestUtil.IGNORE) // 실제 AWS 서비스를 호출하지 않도록 설정

        Given("KinesisConsumerManager") {
            // 테스트 데이터 설정
            val streamName = "test-stream"
            val applicationName = "test-application"
            val recordHandlerCalled = mutableListOf<KinesisRecord>()
            val recordHandler: suspend (KinesisRecord) -> Unit = { record ->
                recordHandlerCalled.add(record)
            }

            // 모의 Kinesis 클라이언트 설정
            val mockKinesisClient = mockk<KinesisClient>()
            
            // 모의 DynamoDB 클라이언트 설정
            val mockDynamoDbClient = mockk<DynamoDbClient>()
            
            // ShardProcessor 모킹
            mockkConstructor(ShardProcessor::class)
            coEvery { anyConstructed<ShardProcessor>().start() } just Runs
            
            // CheckpointManager 모킹
            mockkConstructor(CheckpointManager::class)
            coEvery { anyConstructed<CheckpointManager>().createTableIfNotExists() } just Runs
            coEvery { anyConstructed<CheckpointManager>().close() } just Runs
            
            // KinesisClient 생성자 모킹
            mockkConstructor(KinesisClient::class)
            every { anyConstructed<KinesisClient>().close() } just Runs
            
            afterTest {
                unmockkAll() // 모든 모킹 해제
            }

            When("컨슈머 매니저를 시작하고 종료하는 경우") {
                // 샤드 목록 응답 설정
                val describeStreamResponse = mockk<DescribeStreamResponse>()
                val streamDescription = mockk<StreamDescription>()
                val shards = listOf(
                    mockk<Shard>().apply {
                        every { shardId } returns "shard-0001"
                    }
                )
                every { streamDescription.shards } returns shards
                every { describeStreamResponse.streamDescription } returns streamDescription
                
                coEvery { 
                    mockKinesisClient.describeStream(any()) 
                } returns describeStreamResponse
                
                // 테스트 대상 객체 생성 (spyk를 사용하여 일부 메서드만 모킹)
                val reader = KinesisReader()
                reader.streamName = streamName
                reader.applicationName = applicationName
                reader.recordHandler = recordHandler
                
                val manager = spyk(reader, recordPrivateCalls = true)
                
                // aws 필드 접근을 위한 리플렉션 설정
                val awsField = KinesisReader::class.java.getDeclaredField("aws")
                awsField.isAccessible = true
                
                // 모의 AwsClient 생성
                val mockAwsClient = mockk<AwsClient>()
                every { mockAwsClient.kinesis } returns mockKinesisClient
                
                // aws 필드 설정
                awsField.set(manager, mockAwsClient)
                
                // private 메서드 모킹
                coJustRun { manager["startShardProcessor"](any<String>()) }
                coJustRun { manager["stopShardProcessor"](any<String>()) }
                
                Then("컨슈머 매니저가 정상적으로 시작되고 종료된다") {
                    runBlocking {
                        // 비동기로 매니저 시작 (블로킹되지 않도록)
                        val job = launch {
                            withTimeout(5.seconds) {
                                manager.start()
                            }
                        }
                        
                        // 잠시 대기 후 종료
                        delay(1000)
                        manager.stop()
                        job.join()
                        
                        // 검증
                        coVerify(exactly = 1) { 
                            anyConstructed<CheckpointManager>().createTableIfNotExists() 
                        }
                        coVerify(atLeast = 1) { 
                            mockKinesisClient.describeStream(any()) 
                        }
                        coVerify(exactly = 1) { 
                            manager["startShardProcessor"]("shard-0001") 
                        }
                    }
                }
            }
            
            When("새로운 샤드가 추가되는 경우") {
                // 첫 번째 호출에서는 1개 샤드, 두 번째 호출에서는 2개 샤드 반환
                var callCount = 0
                coEvery { 
                    mockKinesisClient.describeStream(any()) 
                } answers {
                    callCount++
                    val shards = if (callCount == 1) {
                        listOf(
                            mockk<Shard>().apply {
                                every { shardId } returns "shard-0001"
                            }
                        )
                    } else {
                        listOf(
                            mockk<Shard>().apply {
                                every { shardId } returns "shard-0001"
                            },
                            mockk<Shard>().apply {
                                every { shardId } returns "shard-0002"
                            }
                        )
                    }
                    
                    mockk<DescribeStreamResponse>().apply {
                        every { streamDescription } returns mockk<StreamDescription>().apply {
                            every { this@apply.shards } returns shards
                        }
                    }
                }
                
                // 테스트 대상 객체 생성
                val reader = KinesisReader()
                reader.streamName = streamName
                reader.applicationName = applicationName
                reader.recordHandler = recordHandler
                
                val manager = spyk(reader, recordPrivateCalls = true)
                
                // aws 필드 접근을 위한 리플렉션 설정
                val awsField = KinesisReader::class.java.getDeclaredField("aws")
                awsField.isAccessible = true
                
                // 모의 AwsClient 생성
                val mockAwsClient = mockk<AwsClient>()
                every { mockAwsClient.kinesis } returns mockKinesisClient
                
                // aws 필드 설정
                awsField.set(manager, mockAwsClient)
                
                // private 메서드 모킹
                coJustRun { manager["startShardProcessor"](any<String>()) }
                coJustRun { manager["stopShardProcessor"](any<String>()) }
                
                Then("새로운 샤드에 대한 프로세서가 시작된다") {
                    runBlocking {
                        // manageShardsAndProcessors 메서드 직접 호출
                        callPrivateMethod(manager, "manageShardsAndProcessors")
                        
                        // 첫 번째 샤드 프로세서 시작 확인
                        coVerify(exactly = 1) { 
                            manager["startShardProcessor"]("shard-0001") 
                        }
                        
                        // 두 번째 호출
                        callPrivateMethod(manager, "manageShardsAndProcessors")
                        
                        // 두 번째 샤드 프로세서 시작 확인
                        coVerify(exactly = 1) { 
                            manager["startShardProcessor"]("shard-0002") 
                        }
                    }
                }
            }
            
            When("샤드가 제거되는 경우") {
                // 첫 번째 호출에서는 2개 샤드, 두 번째 호출에서는 1개 샤드 반환
                var callCount = 0
                coEvery { 
                    mockKinesisClient.describeStream(any()) 
                } answers {
                    callCount++
                    val shards = if (callCount == 1) {
                        listOf(
                            mockk<Shard>().apply {
                                every { shardId } returns "shard-0001"
                            },
                            mockk<Shard>().apply {
                                every { shardId } returns "shard-0002"
                            }
                        )
                    } else {
                        listOf(
                            mockk<Shard>().apply {
                                every { shardId } returns "shard-0001"
                            }
                        )
                    }
                    
                    mockk<DescribeStreamResponse>().apply {
                        every { streamDescription } returns mockk<StreamDescription>().apply {
                            every { this@apply.shards } returns shards
                        }
                    }
                }
                
                // 테스트 대상 객체 생성
                val reader = KinesisReader()
                reader.streamName = streamName
                reader.applicationName = applicationName
                reader.recordHandler = recordHandler
                
                val manager = spyk(reader, recordPrivateCalls = true)
                
                // aws 필드 접근을 위한 리플렉션 설정
                val awsField = KinesisReader::class.java.getDeclaredField("aws")
                awsField.isAccessible = true
                
                // 모의 AwsClient 생성
                val mockAwsClient = mockk<AwsClient>()
                every { mockAwsClient.kinesis } returns mockKinesisClient
                
                // aws 필드 설정
                awsField.set(manager, mockAwsClient)
                
                // shardProcessors 맵에 샤드 추가
                val shardProcessorsField = KinesisReader::class.java.getDeclaredField("shardProcessors")
                shardProcessorsField.isAccessible = true
                val shardProcessors = shardProcessorsField.get(manager) as MutableMap<String, Any>
                shardProcessors["shard-0001"] = mockk()
                shardProcessors["shard-0002"] = mockk()
                
                // private 메서드 모킹
                coJustRun { manager["startShardProcessor"](any<String>()) }
                coJustRun { manager["stopShardProcessor"](any<String>()) }
                
                Then("제거된 샤드에 대한 프로세서가 중지된다") {
                    runBlocking {
                        // 첫 번째 호출
                        callPrivateMethod(manager, "manageShardsAndProcessors")
                        
                        // 두 번째 호출
                        callPrivateMethod(manager, "manageShardsAndProcessors")
                        
                        // 샤드 프로세서 중지 확인
                        coVerify(exactly = 1) { 
                            manager["stopShardProcessor"]("shard-0002") 
                        }
                    }
                }
            }
            
            When("레코드가 처리되는 경우") {
                // ShardProcessor 모킹 해제
                unmockkAll()
                
                // 모의 Kinesis 클라이언트 설정
                val mockKinesisClient = mockk<KinesisClient>()
                
                // 모의 DynamoDB 클라이언트 설정
                val mockDynamoDbClient = mockk<DynamoDbClient>()
                
                // 체크포인트 응답 설정
                coEvery { 
                    mockDynamoDbClient.getItem(any()) 
                } returns GetItemResponse { 
                    item = null 
                }
                
                coJustRun { 
                    mockDynamoDbClient.putItem(any()) 
                }
                
                coJustRun { 
                    mockDynamoDbClient.createTable(any()) 
                }
                
                // 샤드 이터레이터 응답 설정
                coEvery { 
                    mockKinesisClient.getShardIterator(any()) 
                } returns GetShardIteratorResponse {
                    shardIterator = "test-iterator"
                }
                
                // 레코드 응답 설정
                val testData = "test data"
                val testDataBytes = testData.encodeToByteArray()
                
                // 첫 번째 호출에서는 레코드 반환, 두 번째 호출에서는 빈 응답
                var getRecordsCallCount = 0
                coEvery { 
                    mockKinesisClient.getRecords(any()) 
                } answers {
                    getRecordsCallCount++
                    if (getRecordsCallCount == 1) {
                        GetRecordsResponse {
                            records = listOf(
                                Record {
                                    sequenceNumber = "seq-001"
                                    partitionKey = "partition-1"
                                    data = testDataBytes
                                    approximateArrivalTimestamp = Instant.now()
                                }
                            )
                            nextShardIterator = null // 두 번째 호출이 없도록 null 반환
                        }
                    } else {
                        GetRecordsResponse {
                            records = emptyList()
                            nextShardIterator = null
                        }
                    }
                }
                
                // 샤드 목록 응답 설정
                coEvery { 
                    mockKinesisClient.describeStream(any()) 
                } returns DescribeStreamResponse {
                    streamDescription = StreamDescription {
                        shards = listOf(
                            Shard {
                                shardId = "shard-0001"
                            }
                        )
                    }
                }
                
                // CheckpointManager 모킹
                mockkConstructor(CheckpointManager::class)
                coEvery { anyConstructed<CheckpointManager>().createTableIfNotExists() } just Runs
                coEvery { anyConstructed<CheckpointManager>().getCheckpoint(any()) } returns null
                coEvery { anyConstructed<CheckpointManager>().saveCheckpoint(any()) } just Runs
                coEvery { anyConstructed<CheckpointManager>().close() } just Runs
                
                // KinesisClient 및 DynamoDbClient 생성자 모킹
                mockkConstructor(KinesisClient::class)
                every { anyConstructed<KinesisClient>().close() } just Runs
                
                mockkConstructor(DynamoDbClient::class)
                every { anyConstructed<DynamoDbClient>().close() } just Runs
                
                // 테스트용 recordHandler
                val recordHandlerCalled = mutableListOf<KinesisRecord>()
                val recordHandler: suspend (KinesisRecord) -> Unit = { record ->
                    recordHandlerCalled.add(record)
                }
                
                // ShardProcessor 직접 생성 및 테스트
                val processor = ShardProcessor(
                    streamName = streamName,
                    shardId = "shard-0001",
                    checkpointManager = CheckpointManager {
                        this.tableName = "kinesis-checkpoints"
                        this.applicationName = applicationName
                    },
                    recordHandler = recordHandler
                )
                
                // private 필드 접근을 위한 리플렉션 설정
                val kinesisClientField = ShardProcessor::class.java.getDeclaredField("kinesisClient")
                kinesisClientField.isAccessible = true
                kinesisClientField.set(processor, mockKinesisClient)
                
                Then("레코드가 정상적으로 처리된다") {
                    runBlocking {
                        // 비동기로 프로세서 시작 (블로킹되지 않도록)
                        val job = launch {
                            withTimeout(5.seconds) {
                                processor.start()
                            }
                        }
                        
                        // 잠시 대기 후 종료
                        delay(1000)
                        processor.stop()
                        job.join()
                        
                        // 검증
                        recordHandlerCalled.size shouldBe 1
                        recordHandlerCalled[0].data shouldBe testData
                        recordHandlerCalled[0].sequenceNumber shouldBe "seq-001"
                        recordHandlerCalled[0].partitionKey shouldBe "partition-1"
                        recordHandlerCalled[0].shardId shouldBe "shard-0001"
                        
                        coVerify(exactly = 1) { 
                            mockKinesisClient.getRecords(any()) 
                        }
                        coVerify(exactly = 1) { 
                            anyConstructed<CheckpointManager>().saveCheckpoint(any()) 
                        }
                    }
                }
            }
        }
    }
    
    // private 메서드 호출을 위한 헬퍼 함수
    private suspend fun callPrivateMethod(obj: Any, methodName: String, vararg args: Any?) {
        val method = obj::class.java.getDeclaredMethod(methodName, *args.map { it?.javaClass ?: Any::class.java }.toTypedArray())
        method.isAccessible = true
        method.invoke(obj, *args)
    }
}