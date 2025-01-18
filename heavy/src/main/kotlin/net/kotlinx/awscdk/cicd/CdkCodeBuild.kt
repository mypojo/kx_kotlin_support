package net.kotlinx.awscdk.cicd

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.core.Kdsl
import net.kotlinx.system.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.codebuild.*
import software.amazon.awscdk.services.codecommit.IRepository
import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.ec2.SubnetSelection
import software.amazon.awscdk.services.ec2.SubnetType
import software.amazon.awscdk.services.iam.IRole
import software.amazon.awscdk.services.s3.IBucket

/**
 * AWS 코드빌드
 * */
class CdkCodeBuild : CdkInterface {

    @Kdsl
    constructor(block: CdkCodeBuild.() -> Unit = {}) {
        apply(block)
    }

    /** VPC 이름 */
    override val logicalName: String
        get() = "${projectName}-${branchName}-${suff}"

    /** 코드커밋 저장소 */
    lateinit var codeRepository: IRepository

    /** 브랜치 명 */
    var branchName: String = deploymentType.name.lowercase()

    /** 캐싱용 버킷 */
    lateinit var chacheBucket: IBucket

    /** 그래들 버전. 항상 최신버전 사용할것! */
    var gradleVersion: String = "8.8"

    /** 자바 런타임버전. 항상 최신버전 써도됨 */
    var javaVersion: String = "corretto21"

    /**
     * 그래들 명령어들
     *  */
    lateinit var gradleCmds: List<String>

    /**
     * 그래들 명령어 간단등록
     * ex)  :service:batch:deployBatch
     *  */
    fun gradleCmds(vararg cmds: String) {
        gradleCmds = cmds.map {
            listOf(
                "/opt/gradle/gradle-$gradleVersion/bin/gradle",
                "--parallel",  //병렬처리
                "--build-cache", //캐시온
                "-g /opt/.gradle",
                it,
                "-Djib.console=plain",  //JIB 로그 제거
            ).joinToString(" ")
        }
    }

    /** 내부에서 테스트 등을 수행할 수 있음으로 프로그램 가동용 역할 넣으면됨 */
    lateinit var role: IRole

    /** VPC. 이게 없어도 됨 */
    var vpc: IVpc? = null

    /** 코드빌드용 SG */
    var securityGroups: List<ISecurityGroup>? = null

    /**
     * 동시에 빌드 가능한 숫자.
     * 롤링 배포의 겅우에 높이면 좋다.
     *  */
    var concurrentBuildLimit: Int = 2

    /**
     * 코드빌드 콘솔에서 최신버전 확인가능.
     * https://ap-northeast-2.console.aws.amazon.com/codesuite/codebuild/project/new?region=ap-northeast-2#
     * */
    var buildImage = "aws/codebuild/amazonlinux2-x86_64-standard:5.0"


    /**
     * 컨테이너 환경변수
     *  */
    var environment: Map<String, BuildEnvironmentVariable> = mapOf(
        DeploymentType::class.simpleName!! to BuildEnvironmentVariable.builder().type(BuildEnvironmentVariableType.PLAINTEXT).value(deploymentType.name).build(), //필수
        "BRANCH_NAME" to BuildEnvironmentVariable.builder().type(BuildEnvironmentVariableType.PLAINTEXT).value(branchName).build(), //이건 참고용
    )


    /** 결과 */
    lateinit var codeBuild: Project

    fun create(stack: Stack, block: ProjectProps.Builder.() -> Unit = {}): CdkCodeBuild {
        //일단 커스텀 로그그룹 무시.. 디폴트 써보자
        codeBuild = Project(
            stack, logicalName, ProjectProps.builder()
                .projectName(logicalName)
                .vpc(vpc)
                .subnetSelection(SubnetSelection.builder().subnetType(SubnetType.PRIVATE_WITH_EGRESS).build())
                .securityGroups(securityGroups)
                .source(Source.codeCommit(CodeCommitSourceProps.builder().repository(codeRepository).branchOrRef(branchName).build()))
                .description("push -> build -> deploy")
                .concurrentBuildLimit(concurrentBuildLimit)
                .environment(
                    // https://docs.aws.amazon.com/codebuild/latest/userguide/available-runtimes.html
                    // node는 최신으로 개발 & java 는 컴파일할때 최신 버전(하위호환성 유지됨) 사용하면 됨
                    BuildEnvironment.builder()
                        .buildImage(LinuxBuildImage.fromCodeBuildImageId(buildImage))
                        .computeType(ComputeType.MEDIUM) // react build 때문에 small 사용하면 안됨
                        .environmentVariables(environment)
                        .build()
                )
                //.logging()
                .role(role)
                .cache(Cache.bucket(chacheBucket, BucketCacheOptions.builder().prefix("codebuild_cache").build()))
                .buildSpec(
                    /** 빌드스펙 구성에 순서가 있는지 확인필요 */
                    BuildSpec.fromObject(
                        mapOf(
                            "version" to 0.2,
                            "env" to mapOf(
                                "variables" to mapOf(
                                    "codebuild" to true
                                ),
                                "parameter-store" to mapOf() //파라메터 스토어 값을 환경변수로 매핑할 수 있음
                            ),
                            //"reports" to  -> 리포트는 일단 스킵
                            "phases" to mapOf(
                                "install" to mapOf(
                                    "runtime-versions" to mapOf(
                                        "java" to javaVersion,
                                        //"docker" to "latest",  //dock는 머지?
                                        "nodejs" to "latest",
                                    ),
                                    "run-as" to "root",
                                    "commands" to listOf(
                                        // 한글 파일명 깨짐 방지용
                                        "export LC_ALL=\"en_US.utf8\"",
                                        // gradle 설치. 최신 버전으로 하면 됨.. 이렇게 하는게 맞나?
                                        "[ -f /opt/gradle/gradle-$gradleVersion/bin/gradle ] || { wget https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip ; unzip -d /opt/gradle gradle-$gradleVersion-bin.zip ; } ;",
                                    )
                                ),
                                "pre_build" to mapOf(
                                    "commands" to emptyList<String>()
                                ),
                                "build" to mapOf(
                                    "commands" to gradleCmds
                                ),
                            ),
                            "cache" to mapOf(
                                "paths" to listOf(
                                    "/root/.npm/**/*",
                                    "/opt/.gradle/**/*",
                                    "/opt/gradle/**/*",
                                )
                            )
                        )
                    )
                )
                .apply(block)
                .build()
        )

        TagUtil.tag(codeBuild, deploymentType)
        return this
    }

}