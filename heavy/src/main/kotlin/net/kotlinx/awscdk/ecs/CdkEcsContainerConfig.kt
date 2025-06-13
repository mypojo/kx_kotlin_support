package net.kotlinx.awscdk.ecs

import net.kotlinx.core.Kdsl

/**
 * ECS 컨테이너 최초 설정값
 *  */
class CdkEcsContainerConfig {

    @Kdsl
    constructor(block: CdkEcsContainerConfig.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 기본서버 스펙 ======================================================

    /**
     * 인스턴스 CPU
     * 1024 = 1 CPU
     * 최소는 256
     *  */
    var vcpuNum: Int = 1024

    /** 인스턴스 메모리 */
    var memoryGb: Int = 2

    //==================================================== 오토스케일링 ======================================================

    /**
     * 최초 유지할 인스턴스 숫자.
     * 이 값은 프로그램에 의해서 자주 변경될 수 있다.
     * */
    var desiredCount: Int = 1

    /** 오토스케일링 최소 인스턴스 숫자 */
    var minCapacity: Int = 1

    /** 오토스케일링 최대 인스턴스 숫자 */
    var maxCapacity: Int = 2

    /** CPU x%를 유지하려고 노력합니다. */
    var targetUtilizationPercent: Int = 50

}