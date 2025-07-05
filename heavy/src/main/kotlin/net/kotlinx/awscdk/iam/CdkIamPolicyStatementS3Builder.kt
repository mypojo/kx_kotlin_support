package net.kotlinx.awscdk.iam

import net.kotlinx.aws.lakeformation.LakeformationTagManager
import net.kotlinx.core.Kdsl


/**
 * S3 관련 정책 생성기
 * 다수의 버킷 세팅을 한번에 처리한다
 *
 * 데이터 레이크에 권한 부여시 별도 태그 기반의 역할부여가 필요함
 * @see LakeformationTagManager
 *  */
class CdkIamPolicyStatementS3Builder {

    @Kdsl
    constructor(block: CdkIamPolicyStatementS3Builder.() -> Unit = {}) {
        apply(block)
    }

    /** S3 설정 */
    var datas: List<CdkIamPolicyStatementS3Data> = emptyList()

    /**
     * KMS 복호화 키. (S3 공용) 이게 있어야 S3 읽어서 복호화 가능하다
     * https://ap-northeast-2.console.aws.amazon.com/kms/home?region=ap-northeast-2#/kms/defaultKeys
     * ex) arn:aws:kms:ap-northeast-2:8888:key/yyyy-399b-xxyxx-9031-xxxyx
     *  */
    var kmsArn: String? = null

    /**
     * get 권한을 의미함
     * S3 구조가 특이해서 객체 list를 막으려면 따로 설정해야함
     * */
    private val readonlyPaths: List<String> by lazy { datas.flatMap { d -> d.readonlyPath.map { "arn:aws:s3:::${d.bucketName}/${it}" } } }

    /**
     * athena 읽기 권한 부여시, athena 결과 패스에 쓰기 권한이 있어야함
     * */
    private val writePaths: List<String> by lazy { datas.flatMap { d -> d.writePath.map { "arn:aws:s3:::${d.bucketName}/${it}" } } }


    fun build(): List<CdkIamPolicyStatement> {

        return buildList {

            //================================================= Bucket-Level Permissions (버킷 경로로는 권한이 부여되지 않음) ===================================================
            /**
             * 하위 도메인에 객체 리스팅 권한을 막으려면  별도 s3:prefix 컨디선 추가 작업이 필요함
             *  */
            add(
                CdkIamPolicyStatement {
                    actions = listOf(
                        "s3:List*",
                        "s3:Describe*",
                        "s3:GetBucketLocation", //실제 버킷의 리전 확인용
                    )
                    resources = datas.map { "arn:aws:s3:::${it.bucketName}" } //버킷 자체에 리소스를 할당
                }
            )

            //==================================================== Object-Level Permissions ======================================================
            if (readonlyPaths.isNotEmpty()) {
                add(CdkIamPolicyStatement {
                    actions = listOf(
                        "s3:Get*",
                        "s3-object-lambda:Get*",
                        "s3-object-lambda:List*"
                    )
                    resources = readonlyPaths
                })
            }
            if (writePaths.isNotEmpty()) {
                add(CdkIamPolicyStatement {
                    actions = listOf(
                        "s3:Put*",
                    )
                    resources = writePaths
                })
            }

            //==================================================== 기타 ======================================================

            if (kmsArn != null) {
                this += CdkIamPolicyStatement {
                    actions = listOf(
                        "kms:Decrypt",
                    )
                    resources = listOf(kmsArn!!)
                }
            }

        }
    }


}
