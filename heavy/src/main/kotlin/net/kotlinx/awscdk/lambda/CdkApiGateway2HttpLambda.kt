package net.kotlinx.awscdk.lambda

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegration
import software.amazon.awscdk.services.apigatewayv2.*
import software.amazon.awscdk.services.certificatemanager.ICertificate
import software.amazon.awscdk.services.lambda.IFunction


/**
 * 일단 간단한 데모
 * API 게이트웨이 람다 버전
 * ex) 리다이렉트
 * domain의 경우 만들어진 경로로 cname을 매핑 해줘야함 (도메인은 타 계정으로 관리하는 경우가 많으니 수작업)
 *
 * 간단 풀스택 백엔드 참고
 * https://www.notion.so/mypojo/c01abb48124a411f8d516ee2c64e64fc
 * */
class CdkApiGateway2HttpLambda : CdkInterface {

    @Kdsl
    constructor(block: CdkApiGateway2HttpLambda.() -> Unit = {}) {
        apply(block)
    }

    /** 언더바 포함금지 */
    override val logicalName: String
        get() = "${apiName}-${suff}"

    lateinit var apiName: String

    var description: String? = null

    lateinit var lambda: IFunction

    /**
     * 도메인 & CERT ARN
     * ex) "abc.com" to Certificate.fromCertificateArn(stack, "aaa.bbbb.cccc", CERT_PROD)
     *  */
    lateinit var domain: Pair<String, ICertificate>

    /** 결과물1 */
    lateinit var domainLink: DomainName

    /** 결과물2 */
    lateinit var httpApi: HttpApi

    fun create(stack: Stack, block: HttpApiProps.Builder.() -> Unit = {}) {

        domainLink = DomainName(
            stack, "${domain.first}-domainName", DomainNameProps.builder()
                .domainName(domain.first)
                .certificate(domain.second)
                .build()
        )

        val lambdaIntegration = HttpLambdaIntegration("$logicalName-defaultlambda01", lambda)

        httpApi = HttpApi(
            stack, "$logicalName-apiGateway", HttpApiProps.builder()
                .apiName(logicalName)
                .description(description)
                .defaultIntegration(lambdaIntegration) //별도의
                .defaultDomainMapping(
                    DomainMappingOptions.builder()
                        .domainName(domainLink)
                        .build()
                )
                .apply(block)
                .build()
        )

//        httpApi.addRoutes(
//            AddRoutesOptions.builder()
//                .path("/")  //$default 설정하는거 아님.
//                .methods(listOf(HttpMethod.ANY))
//                .integration(lambdaIntegration)
//                .build()
//        )

    }


}


