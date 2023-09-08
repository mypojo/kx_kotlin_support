package net.kotlinx.aws_cdk.component

/**
 *  자주 사용되는 PolicyStatement 모음
 *  */
object CdkPolicyStatementSetS3 {

    /**
     * 특정 경로 읽기 부여.
     * 읽기 리소스에 버킷도 같이 포함되어 있어야 한다.
     * ex) athena 테이블 조회 권한
     *  */
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
