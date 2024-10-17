package net.kotlinx.aws.fargate

import mu.KotlinLogging
import net.kotlinx.system.ResourceHolder
import sun.misc.Signal
import java.io.File
import kotlin.system.exitProcess

object FargateUtil {

    private val log = KotlinLogging.logger {}

    /** 파게이트의 로컬디렉토리 디폴트 경로. 여기부터 20G가 할당된다.  */
    val ROOT = File("/local")

    //==================================================== 비용 ======================================================
    /** 시간당 vCPU당  */
    private const val COST_CPU = 0.013968

    /** 시간당 GB당  */
    private const val COST_MEMORY = 0.001533

    /** 대략 계산  */
    fun cost(cpu: Double, memoryGb: Double, intervalMills: Long): Double {
        val hour = intervalMills / 1000 / 60 / 60
        val costPerHour = COST_CPU * cpu + COST_MEMORY * memoryGb / 1024
        return costPerHour * hour
    }

    /**
     * 컨테이너 종료 처리
     * https://aws.amazon.com/ko/blogs/containers/graceful-shutdowns-with-ecs/
     * 기본 stopTimeout 값은 30초
     */
    fun gracefulShutdowns() {
        //SIGTERM신호  를 보내기 전에 로드 밸런서의 대상 그룹에서 작업을 자동으로 등록 취소함
        //완료하는 데 시간이 오래 걸리는 작업의 경우 진행 중인 작업을 체크포인트하고 비용이 많이 드는 강제 종료를 방지하기 위해 신속하게 종료하는 것을 고려
        Signal.handle(Signal("TERM")) { sig: Signal? ->
            log.warn { "SIGTERM $sig 입력됨. SIGKILL 입력되기 30초 전.. 리소스를 처리합니다." }
            ResourceHolder.finish()
            log.warn { "리소스를 처리 완료." }
            exitProcess(0)
        }
    }
}