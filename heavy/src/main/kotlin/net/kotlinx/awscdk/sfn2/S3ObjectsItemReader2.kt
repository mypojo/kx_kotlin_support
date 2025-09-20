package net.kotlinx.awscdk.sfn2

import net.kotlinx.core.Kdsl
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.s3.IBucket
import software.amazon.awscdk.services.stepfunctions.IItemReader
import software.amazon.awscdk.services.stepfunctions.QueryLanguage

/**
 * 커스텀 S3 Objects ItemReader
 * CDK에서 아직 지원안해서 커스텀하게 만들었음
 */
class S3ObjectsItemReader2 : IItemReader {

    @Kdsl
    constructor(block: S3ObjectsItemReader2.() -> Unit = {}) {
        apply(block)
    }

    lateinit var bucketPath: String
    lateinit var prefixPath: String

    override fun getBucket(): IBucket = throw UnsupportedOperationException("Bucket construct not available for CustomS3ObjectsItemReader")

    override fun getResource(): String = "arn:aws:states:::s3:listObjectsV2"

    override fun providePolicyStatements(): MutableList<PolicyStatement> = mutableListOf()

    override fun render(queryLanguage: QueryLanguage?): Any = render()

    override fun render(): Any {
        return mapOf(
            "Resource" to getResource(),
            "Parameters" to mapOf(
                "Bucket.$" to bucketPath,
                "Prefix.$" to prefixPath,
            )
        )
    }

    override fun validateItemReader(): MutableList<String> = mutableListOf()
}
