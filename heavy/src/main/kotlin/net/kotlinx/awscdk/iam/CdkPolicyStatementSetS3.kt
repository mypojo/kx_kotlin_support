package net.kotlinx.awscdk.iam

/**
 *  자주 사용되는 PolicyStatement 모음
 *  */
object CdkPolicyStatementSetS3 {

    /**
     * 콘솔에서 S3에 특정경로 이상을 포함하기 위한 정책
     * @param bucketName ex) xx-work-dev
     * @param keys ex) upload/abc/\*  ( 시작문자가 '/'로 시작하지 않음!!)
     *  */
    fun s3ReadOnlyAccessConsole(bucketName: String, keys: List<String>): List<CdkPolicyStatement> = listOf(
        /**  AmazonS3ReadOnlyAccess 2024-04 기반 */
        CdkPolicyStatement {
            actions = listOf(
                "s3:Get*",
                "s3:List*",
                "s3:Describe*",
                "s3-object-lambda:Get*",
                "s3-object-lambda:List*"
            )
            this.resources = keys.map { "arn:aws:s3:::${bucketName}/${it}" }
        },
        /**
         * 추가로 AWS 콘솔에서는 리스팅 해주는 역할이 필요하다.
         *  -> 이게 리소스 경로를 지정하는게 아니라, 버킷 + condition 으로 지정된다.
         *  */
        CdkPolicyStatement {
            actions = listOf(
                "s3:ListBucket",
            )
            this.resources = listOf("arn:aws:s3:::${bucketName}") //리소스에는 버킷을 지정해야함
            conditions = mapOf(
                "StringLike" to mapOf(
                    "s3:prefix" to keys
                )
            )
        },
    )


    /**
     * 특정 경로 읽기 부여.
     * 읽기 리소스에 버킷도 같이 포함되어 있어야 한다.
     * ex) athena 테이블 조회 권한
     *  */
    @Deprecated("s3ReadOnlyAccess 사용하세요")
    fun s3Read(resources: List<String>): CdkPolicyStatement {
        return CdkPolicyStatement {
            actions = listOf(
                "s3:GetBucketLocation",
                "s3:GetObject",
                "s3:ListBucket",
                "s3:ListBucketMultipartUploads",
                "s3:ListMultipartUploadParts",
            )
            this.resources = resources
        }
    }

    /**
     * 특정 경로 읽기/쓰기 부여
     * ex) athena result write
     *  */
    @Deprecated("")
    fun s3ReadWrite(resources: List<String>): CdkPolicyStatement {
        return CdkPolicyStatement {
            actions = listOf(
                "s3:GetBucketLocation",
                "s3:GetObject",
                "s3:ListBucket",
                "s3:ListBucketMultipartUploads",
                "s3:ListMultipartUploadParts",
                "s3:PutObject",
                "s3:PutBucketPublicAccessBlock"
            )
            this.resources = resources
        }
    }

}
