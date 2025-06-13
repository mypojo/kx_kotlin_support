package net.kotlinx.awscdk.cicd

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.collection.mapOf
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

    /** 브랜치 명 */
    var branchName: String = deploymentType.name.lowercase()

    /** 캐싱용 버킷 */
    lateinit var chacheBucket: IBucket

    /** 그래들 버전. 항상 최신버전 사용할것! */
    var gradleVersion: String = "8.13"

    /** 자바 런타임버전. 항상 최신버전 써도됨 */
    var javaVersion: String = "corretto21"

    /**
     * 그래들 명령어들
     *  */
    lateinit var gradleCmds: List<String>

    /**
     * 그래들 명령어 간단등록
     * ex)  :service:batch:deployBatch
     * 경고!! gradleVersion 수정후 이거 호출할것!!
     *  */
    fun gradleCmds(vararg cmds: String) {
        gradleCmds = cmds.map {
            listOf(
                "/opt/gradle/gradle-$gradleVersion/bin/gradle", //압축푸는경로 애매해서 걍 이렇게 함
                "--parallel",  //병렬처리
                "--build-cache", //캐시온
                "-g /opt/.gradle",  //글로벌설정 -> 그래들 루트 설정 (의존성 저장 등)
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
     * 관리형 이미지는 코드빌드 콘솔에서 최신버전 확인가능.
     * https://ap-northeast-2.console.aws.amazon.com/codesuite/codebuild/project/new?region=ap-northeast-2#
     * ex) LinuxBuildImage.fromEcrRepository(..)
     *
     * 이미지를 잘 구워놔다면 람다로 빌드해도 될거같음
     * */
    var buildImage: IBuildImage = LinuxBuildImage.AMAZON_LINUX_2_5

    /**
     * 컨테이너 환경변수
     *  */
    var environment: Map<String, BuildEnvironmentVariable?> = mapOf {
        DeploymentType::class.simpleName!! to BuildEnvironmentVariable.builder().type(BuildEnvironmentVariableType.PLAINTEXT).value(deploymentType.name).build() //필수
        "BRANCH_NAME" to BuildEnvironmentVariable.builder().type(BuildEnvironmentVariableType.PLAINTEXT).value(branchName).build() //이건 참고용
    }

    /**
     * S3 캐시 파일은 주기적으로 삭제 해줘야 한다 (S3 라이프사이클 달아도 되는데 혹시 메이븐 고장날수 있어서 추천하지는 않음)
     * ex) AWS 버전을 2.3 -> 2.4로 올리면 모든버전 파일이 다 있음으로 크기가 두배가 된다..
     * 일반 프로젝트의 경우 1.5G 정도 됨. 캐시결과 업로드에는 2.6분 정도 걸리는듯
     * 캐시 미히트 = 10분
     * 캐시 히트 = 6분 (실제 빌드는 4.5분. 1.5분은 서버준비,  캐시 다운받고 업로드 하고.. 등등 )
     * 참고 : 람다 스냅스타트 기다리는 시간이 웹서버 번들링 보다 1.5분 더 걸릴정도로 김
     * */
    var cachePaths = listOf(
        /** NPM 패키기 캐시 (디폴트로 루트) */
        "/root/.npm/**/*",
        /** 그래들 메이븐 의존성 캐시 */
        "/opt/.gradle/**/*",
        /** 그래들 설치파일 캐시 */
        "/opt/gradle/**/*",
    )

    /** 소스코드 */
    lateinit var source: ISource

    /** 코드커밋 사용 */
    fun byCodeCommit(codeRepository: IRepository) {
        source = Source.codeCommit(CodeCommitSourceProps.builder().repository(codeRepository).branchOrRef(branchName).build())
    }

    /** 깃헙 사용 (토큰방식, 커넥트방식 둘다 지원) */
    fun byGithub(owner: String, repo: String) {
        source = Source.gitHub(
            GitHubSourceProps.builder()
                .branchOrRef(branchName)
                .owner(owner)
                .repo(repo)
                .build()
        )
    }

    /** 결과 */
    lateinit var codeBuild: Project

    fun create(stack: Stack, block: ProjectProps.Builder.() -> Unit = {}): CdkCodeBuild {
        //일단 커스텀 로그그룹 무시.. 디폴트 써보자
        codeBuild = Project(
            stack, logicalName,
            ProjectProps.builder()
                .projectName(logicalName)
                .vpc(vpc)
                .subnetSelection(SubnetSelection.builder().subnetType(SubnetType.PRIVATE_WITH_EGRESS).build())
                .securityGroups(securityGroups)
                .source(source)
                .description("push -> build -> deploy")
                .concurrentBuildLimit(concurrentBuildLimit)
                .environment(
                    // https://docs.aws.amazon.com/codebuild/latest/userguide/available-runtimes.html
                    // node는 최신으로 개발 & java 는 컴파일할때 최신 버전(하위호환성 유지됨) 사용하면 됨
                    BuildEnvironment.builder()
                        .buildImage(buildImage)
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
                        mapOf {
                            "version" to 0.2
                            "env" to mapOf {
                                "variables" to mapOf {
                                    "codebuild" to true
                                }
                                /** 파라메터 스토어 값을 환경변수로 매핑할 수 있음 */
                                "parameter-store" to mapOf {

                                }
                            }
                            //"reports" to  -> 리포트는 일단 스킵
                            "phases" to mapOf {
                                "install" to mapOf {
                                    /** 런타임을 지정하면 해당 토커를 커스텀 해주는듯? */
                                    "runtime-versions" to mapOf {
                                        "java" to javaVersion
                                        "nodejs" to "latest"
                                    }
                                    "run-as" to "root"
                                    "commands" to listOf(
                                        // gradle 설치.  [..] 문법으로 해당 파일이 없을경우 설치
                                        "[ -f /opt/gradle/gradle-$gradleVersion/bin/gradle ] || { wget https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip ; unzip -d /opt/gradle gradle-$gradleVersion-bin.zip ; } ;",
                                    )
                                }
                                "pre_build" to mapOf {
                                    "commands" to emptyList<String>()
                                }
                                "build" to mapOf {
                                    "commands" to gradleCmds
                                }
                            }
                            "cache" to mapOf {
                                "paths" to cachePaths
                            }
                        }
                    )
                )
                .apply(block)
                .build()
        )

        TagUtil.tagDefault(codeBuild)
        return this
    }

}