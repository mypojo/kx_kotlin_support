package net.kotlinx.test

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/**
 * IO가 없는, 빠르게 종료되는 로직 테스트 = 단위 테스트
 * 일반적으로 배포시 필수적으로 수행
 * */
@Test
@Tag("TestLevel01")
annotation class TestLevel01

/**
 * 외부 의존성은 없지만 많은 연산이나 스래드 딜레이 등이 있어서 시간이 좀 걸리는 테스트 = 단위 테스트v2
 * 특정 환경이 필요할 수도 있음
 *  */
@Test
@Tag("TestLevel02")
annotation class TestLevel02

/**
 * DB나 http 등의 외부 리소스와 IO 등이 포함된 테스트 = 통합 테스트
 * 특정 환경이 있어야 작동함
 * */
@Test
@Tag("TestLevel03")
annotation class TestLevel03

/**
 * 빌드시 체크하는 테스트
 * 중요 로직 및 RDS 등의 환경 테스트도 포함한다.
 * */
@Test
@Tag("TestLevelBuild")
annotation class TestLevelBuild