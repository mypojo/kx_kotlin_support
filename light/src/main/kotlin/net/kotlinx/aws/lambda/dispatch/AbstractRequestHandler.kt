package net.kotlinx.aws.lambda.dispatch

import com.amazonaws.services.lambda.runtime.RequestHandler
import mu.KotlinLogging
import net.kotlinx.system.ResourceHolder
import net.kotlinx.time.TimeUtil
import org.crac.Context
import org.crac.Resource

/**
 * 람다의 기본 진입점
 * 편의상 만들었음
 *
 * 스냅스타트 적용시
 * init 에서 Core.getGlobalContext().register(this) 해줄것
 * beforeCheckpoint 에서 koin 초기화 & 스냅스타트 로직 실행
 */
abstract class AbstractRequestHandler : RequestHandler<Map<String, Any>, Map<String, Any>>, Resource {

    private val log = KotlinLogging.logger {}

    /**
     * 스탭스타트 이미지 전
     * 보통 여기서 스냅스타트 로직한번 돌려주고 시작객체 초기화 (koin 등) -> 초기화 안하면 http pool 등이 캐시에 남아서 오류남
     * */
    override fun beforeCheckpoint(context: Context<out Resource>?) {
        log.info { "AWS snapstart beforeCheckpoint" }
    }

    /**
     * 스탭스타트 복구이후.
     * 딱히 할거 없음
     *  */
    override fun afterRestore(context: org.crac.Context<out Resource>?) {
        log.info { "AWS snapstart afterRestore" }
    }

    init {
        TimeUtil.initTimeZone()
        //아직 셧다운 훅은 지원안함.
        Runtime.getRuntime().addShutdownHook(Thread {
            log.warn { "### 람다가 셧다운 됩니다 ###" }
            ResourceHolder.finish()
        })
    }


}
