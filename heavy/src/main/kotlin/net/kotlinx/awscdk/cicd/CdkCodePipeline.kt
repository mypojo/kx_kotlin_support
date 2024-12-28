package net.kotlinx.awscdk.cicd

import net.kotlinx.aws.AwsNaming
import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.awscdk.channel.EventSets
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.codebuild.BuildEnvironmentVariable
import software.amazon.awscdk.services.codebuild.BuildEnvironmentVariableType
import software.amazon.awscdk.services.codebuild.Project
import software.amazon.awscdk.services.codecommit.IRepository
import software.amazon.awscdk.services.codepipeline.*
import software.amazon.awscdk.services.codepipeline.actions.*
import software.amazon.awscdk.services.codestarnotifications.DetailType
import software.amazon.awscdk.services.codestarnotifications.NotificationRule
import software.amazon.awscdk.services.codestarnotifications.NotificationRuleProps
import software.amazon.awscdk.services.iam.IRole
import software.amazon.awscdk.services.sns.ITopic

/**
 * AWS 코드빌드
 * CICD 블루그린 배포(코드디플로이)는 인프라로 설정하는게 아니라 gradle 에서 AWS SDK 로 실행됨!
 *
 * 깃헙 연결
 * https://docs.aws.amazon.com/ko_kr/codepipeline/latest/userguide/connections-github.html
 * */
class CdkCodePipeline : CdkInterface {

    @Kdsl
    constructor(block: CdkCodePipeline.() -> Unit = {}) {
        apply(block)
    }

    /** VPC 이름 */
    override val logicalName: String
        get() = "${projectName}-${name}-${suff}"

    /** 이름 */
    var name: String = "codepipeline"

    /** 브랜치명 */
    var branchName: String = deploymentType.name.lowercase()

    /** 코드빌드 */
    lateinit var codeBuild: Project

    /** 코드커밋 저장소 */
    lateinit var codeRepository: IRepository

    /** 내부에서 테스트 등을 수행할 수 있음으로 프로그램 가동용 역할 넣으면됨 */
    lateinit var role: IRole

    /**
     * 노티 받을 토픽
     * 람다는 안되고 챗봇이나 SNS만 됨
     *  */
    lateinit var topics: List<ITopic>

    /** 결과 */
    lateinit var pipeline: Pipeline

    /**
     * SNS 알림을 받을 이벤트
     * 라이브 서버의 경우 성공도 받을 수 있게 하면 됨
     *  */
    var events: List<String> = listOf(EventSets.CodekPipeline.FAILED)


    fun create(stack: Stack, block: PipelineProps.Builder.() -> Unit = {}): CdkCodePipeline {

        val srcArtifact = Artifact.artifact("src-art-${logicalName}")
        pipeline = Pipeline(
            stack, logicalName, PipelineProps.builder()
                .role(role)
                .pipelineName(logicalName)
                /** 파이프라인 V2  https://docs.aws.amazon.com/codepipeline/latest/userguide/pipeline-types.html */
                .pipelineType(PipelineType.V2)
                //.artifactBucket()  ECR에서 가져옴으로 artifactBucket은 필요없음
                .stages(
                    listOf(
                        StageProps.builder().stageName("load-source").actions(
                            listOf(
                                CodeCommitSourceAction(
                                    CodeCommitSourceActionProps.builder()
                                        .actionName("Load-Codecommit")
                                        .role(role)
                                        .repository(codeRepository)
                                        .branch(branchName) //트리거 시킬 브랜치
                                        .trigger(CodeCommitTrigger.EVENTS)
                                        .output(srcArtifact)
                                        .variablesNamespace(VARIABLES_NAMESPACE) //이 변수에 커밋ID 등을 담아줌
                                        .build()
                                )
                            )
                        ).build(),
                        StageProps.builder().stageName("build").actions(
                            listOf(
                                CodeBuildAction(
                                    CodeBuildActionProps.builder()
                                        .actionName("Build-Gradle")
                                        .role(role)
                                        .project(codeBuild)
                                        .type(CodeBuildActionType.BUILD)
                                        .environmentVariables(
                                            mapOf(
                                                AwsNaming.COMMIT_ID to BuildEnvironmentVariable.builder().type(BuildEnvironmentVariableType.PLAINTEXT)
                                                    .value("#{$VARIABLES_NAMESPACE.CommitId}").build()
                                            )
                                        )
                                        .input(srcArtifact)
                                        .build()
                                )
                            )
                        ).build(),
                    )
                )
                .apply(block)
                .build()
        )
        TagUtil.tag(pipeline, deploymentType)

        //성공 실패 등의 알람 추가. 이 알람은 이벤트브릿지 트리거가 아니고 , SNS를 다이렉트로 트리거한다.
        val snsName = "${projectName}-sns_${name}-${suff}"
        NotificationRule(
            stack, snsName, NotificationRuleProps.builder()
                .notificationRuleName(snsName)
                .detailType(DetailType.BASIC) //메세지 바디에 간단한 메세지만 출력
                .events(events)
                .source(pipeline)
                .targets(topics)
                .build()
        )

        return this
    }

    companion object {

        /** 코드커밋 변수 네임스페이스 */
        const val VARIABLES_NAMESPACE: String = "SourceVariables"

    }

}