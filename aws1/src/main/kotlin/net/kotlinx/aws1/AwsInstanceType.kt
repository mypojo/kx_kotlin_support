package net.kotlinx.aws1

import net.kotlinx.core1.threadlocal.ResourceHolder.getWorkspace
import java.io.File

/**
 *
 * 환경변수 리스트
 * https://docs.aws.amazon.com/batch/latest/userguide/job_env_vars.html
 * https://docs.aws.amazon.com/ko_kr/lambda/latest/dg/configuration-envvars.html
 */
enum class AwsInstanceType(val root: File?) {

    //==================================================== 서버리스 제품 ======================================================
    /** AWS LAMBDA  */
    lambda(File("/tmp")),

    /** AWS CODEBUILD.  로컬 디스크 의미없을듯..   */
    codebuild(File("/codebuild/output")),

    //==================================================== 파게이트 ======================================================
    /** AWS BATCH  */
    batch(File("/local")),

    /** ECS 파게이트  */
    ecs(File("/local")),

    //==================================================== 기타 ======================================================

    /** 로컬  */
    local(getWorkspace());
}