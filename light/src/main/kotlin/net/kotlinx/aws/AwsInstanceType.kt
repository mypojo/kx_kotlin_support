package net.kotlinx.aws

import net.kotlinx.file.slash
import net.kotlinx.system.ResourceHolder
import java.io.File

/**
 *
 * 환경변수 리스트
 * https://docs.aws.amazon.com/batch/latest/userguide/job_env_vars.html
 * https://docs.aws.amazon.com/ko_kr/lambda/latest/dg/configuration-envvars.html
 * 대문자로 했어야 하는데.. 일단 그냥 둠
 */
enum class AwsInstanceType(val root: File) {

    //==================================================== 서버리스 제품 ======================================================
    /** AWS LAMBDA  */
    LAMBDA(File("/tmp")),

    /** AWS CODEBUILD.  로컬 디스크 의미없을듯..   */
    CODEBUILD(File("/codebuild/output")),

    //==================================================== 파게이트 ======================================================
    /** AWS BATCH  */
    BATCH(File("/local")),

    /** ECS 파게이트  */
    ECS(File("/local")),

    //==================================================== 기타 ======================================================

    /** 로컬  */
    LOCAL(ResourceHolder.WORKSPACE);

    /** 클라우드와치 로그 링크가 가능한 타입들 */
    fun isLogLinkAble(): Boolean = this in setOf(LAMBDA, BATCH, ECS)

    /** 로그 링크에 시간 입력이 필요한것들 */
    fun isLogLinkWithTime(): Boolean = this in setOf(LAMBDA, ECS)

    /**
     * 임시 디렉토리 생성
     * 이 경로로 통일하자.
     * ex) tmpDir()
     * ECS나 람다를 사용하면 어차피 임시 디렉토리라서 UUID로 디렉토리 안만들어도 됨
     *  */
    fun tmpDir(): File = root.slash("tmp")
}