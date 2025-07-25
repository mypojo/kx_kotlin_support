package net.kotlinx.aws.cloudfront

import aws.sdk.kotlin.services.cloudfront.getDistribution
import aws.sdk.kotlin.services.cloudfront.updateDistribution
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.LazyAwsClientProperty
import net.kotlinx.core.Kdsl


/**
 * 클라우드프론트를 사용해서 프론트를 블루그린 배포할때 사용됨
 * */
class CloudfrontBlueGreenDeployment {

    @Kdsl
    constructor(block: CloudfrontBlueGreenDeployment.() -> Unit = {}) {
        apply(block)
    }

    var client: AwsClient by LazyAwsClientProperty()


    /** E1IB941WRI0DZN 이렇게 생김 */
    lateinit var distributionId: String

    /**
     * 블루 환경정보.  id & 오리진
     * ex) "demo" to "demo-static-dev.s3.ap-northeast-2.amazonaws.com"
     *  */
    lateinit var blue: Pair<String, String>

    /** 그린 환경 정보 */
    lateinit var green: Pair<String, String>

    /** 클라우드 프론트 캐시 클리어 경로들 */
    lateinit var invalidationPaths: List<String>

    /**
     * 블루그린 오리진을 교체함
     * 비용이 나가니 교체되는부분만 정확히 타게팅 할것!
     *  */
    suspend fun switch() {

        val (eTag, existConfig) = client.cloudFront.getDistribution {
            id = distributionId
        }.let {
            it.eTag!! to it.distribution!!.distributionConfig!!
        }

        /** 교체할 오리진을 확인 */
        val replacedOrigin = existConfig.origins!!.items!!.let { orgins ->
            check(orgins.size == 1)
            val origin = orgins.first()
            val newOrigone = when (val originId = origin.id) {
                blue.first -> green
                green.first -> blue
                else -> throw IllegalStateException("origin id(${originId})가 blueGreen의 id(${blue.first}, ${green.first})과 일치하지 않음")
            }
            log.info { "클라우드 프론트 블루그린 스위칭 : [${origin.id}] ===> [${newOrigone.first}]" }
            origin.copy {
                id = newOrigone.first
                domainName = newOrigone.second
            }
        }

        /** 업데이트는 기존값을 먼저 로드 후 부분 교체하는식으로 진행된다 (숨은값이 많음) */
        client.cloudFront.updateDistribution {
            id = distributionId
            ifMatch = eTag  //동시 업데이트로 인한 충돌을 방지
            distributionConfig = existConfig.copy {
                origins = origins!!.copy {
                    items = listOf(replacedOrigin)
                }
                // defaultCacheBehavior와 cacheBehaviors에서 교체된 Origin ID를 참조하도록 업데이트
                defaultCacheBehavior = defaultCacheBehavior?.copy {
                    targetOriginId = replacedOrigin.id
                }
                cacheBehaviors = cacheBehaviors?.copy {
                    items = items?.map { behavior ->
                        behavior.copy {
                            targetOriginId = replacedOrigin.id
                        }
                    }
                }
            }
        }
        client.cloudFront.clear(distributionId, invalidationPaths)
    }

    /**
     * 현재 배포 상태를 확인하고, 배포해야 할 대체 오리진을 반환함
     * 현재 A 오리진을 사용 중이면 B 오리진을 반환하고, B 오리진을 사용 중이면 A 오리진을 반환함
     * @return 배포해야 할 대체 오리진 (id, domainName)
     */
    suspend fun getTargetOrigin(): Pair<String, String> {
        val existConfig = client.cloudFront.getDistribution {
            id = distributionId
        }.distribution!!.distributionConfig!!

        return existConfig.origins!!.items!!.let { origins ->
            check(origins.size == 1) { "오리진이 여러 개 존재합니다. 현재 오리진 개수: ${origins.size}" }
            val origin = origins.first()
            when (val originId = origin.id) {
                blue.first -> {
                    log.info { "현재 블루 오리진(${blue.first})을 사용 중입니다. 그린 오리진(${green.first})으로 배포해야 합니다." }
                    green
                }
                green.first -> {
                    log.info { "현재 그린 오리진(${green.first})을 사용 중입니다. 블루 오리진(${blue.first})으로 배포해야 합니다." }
                    blue
                }
                else -> throw IllegalStateException("origin id(${originId})가 blueGreen의 id(${blue.first}, ${green.first})과 일치하지 않음")
            }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }


}