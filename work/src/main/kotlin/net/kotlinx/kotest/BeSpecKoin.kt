package net.kotlinx.kotest

import net.kotlinx.counter.Latch
import net.kotlinx.koin.Koins
import net.kotlinx.kotest.modules.MyAws1Module
import net.kotlinx.system.SystemSeparator
import net.kotlinx.system.SystemUtil
import org.koin.core.module.Module

/**
 * 간단 스펙 재정의
 * koin 기본 오버라이드
 *
 * 주의!! 한번에 한개의 AWS 프로파일만 가능함 -> 두개의 프로파일은 따로 테스트 돌릴것!
 *
 * 이렇게 만든 이유.. ㅠㅠ
 * 1. io.kotest.koin.KoinExtension 사용하지 않음 -> 이경우 Given 마다 koin이 리셋됨 & 전체 테스트시 문제가 있는거같음.. 학습하기 귀찮음
 * 2. 나는 한개의 koin으로 모든 테스트를 수행하고싶음 ex) DB야연결 -> 따라서 일반적인 koin으로 사용
 *
 * KoinComponent 이거 나중에 제거하기
 * */
abstract class BeSpecKoin(modules: List<Module>) : BeSpecLog() {

    init {
        beforeSpec {
            INIT.check {
                if (SystemSeparator.IS_GRADLE) {
                    val tagExp = SystemUtil.systemValue("kotest.tags") ?: ""
                    val awsProfiles = tagExp.split("|").map { it.trim() }.filter { it.startsWith("kx.") }
                    check(awsProfiles.size <= 1) { "AWS 프로파일은 2개 이상일 수 없습니다." }
                    val awsProfile = awsProfiles.firstOrNull()
                    MyAws1Module.PROFILE_NAME = awsProfile
                    log.warn { "from gradle kotest => 설정된 태그 [$tagExp] -> 적용된 AWS 프로파일 $awsProfile" }
                } else {
                    val awsProfiles = appliedTags().map { it.name }.filter { it.startsWith("kx.") }
                    check(awsProfiles.size <= 1) { "AWS 프로파일은 2개 이상일 수 없습니다." }
                    val awsProfile = awsProfiles.firstOrNull()
                    MyAws1Module.PROFILE_NAME = awsProfile
                    log.warn { "from local kotest => 설정된 태그 [${appliedTags()}] -> 적용된 AWS 프로파일 $awsProfile" }
                }
                Koins.startup(modules)
            }
        }
    }

    companion object {
        private val INIT = Latch()
    }

//    override fun extensions() = listOf(
//        io.kotest.koin.KoinExtension(
//            modules = MyLightKoinStarter.MODULES,
//            mockProvider = null,
//            mode = KoinLifecycleMode.Root,
//        )
//    )

}