package net.kotlinx.kotest.modules

import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.aws.javaSdkv2.dynamoLock.DynamoLockManager
import net.kotlinx.collection.toPair
import net.kotlinx.json.koson.toGsonData
import net.kotlinx.koin.KoinModule
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.MyEnv
import net.kotlinx.lock.*
import net.kotlinx.reflect.name
import net.kotlinx.time.toLong
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

/** 해당 패키지의 기본적인 의존성 주입 */
object ResourceLockModule : KoinModule {

    private val log = KotlinLogging.logger {}

    private val RESOURCE = AtomicLong()

    override fun moduleConfig(): Module = module {

        single(named(ResourceItem::class.name())) {
            ResourceItemTableUtil.createDefault {
                tableName = "system-${MyEnv.SUFFIX}"
            }
        }

        AwsModule.IAM_PROFILES.profiles.forEach { pair ->
            val profile = pair.first
            single(named(profile)) {
                ResourceLockManager {
                    lockManager = koin<DynamoLockManager>(profile)
                    repository = ResourceItemRepository(profile)
                    factory = object : ResourceItemFactory {
                        override suspend fun createResource(req: ResourceLockReq, cnt: Int): List<ResourceItem> {
                            val (logic, adv) = req.resourcePk.split("#").toPair()
                            log.info { "신규 리소스 ${logic}/${adv} ${cnt}건 생성.." }
                            return (0 until cnt).map {
                                ResourceItem(req.resourcePk, "id#${RESOURCE.incrementAndGet()}").apply {
                                    ttl = LocalDateTime.now().plusHours(4).toLong() / 1000
                                    body = obj {
                                        "desc" to "demo"
                                    }.toGsonData()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}