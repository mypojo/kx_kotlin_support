package net.kotlinx.awscdk.ecs

import net.kotlinx.awscdk.CdkEnum
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.system.DeploymentType
import software.amazon.awscdk.Duration
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ecr.*
import software.amazon.awscdk.services.ecr.TagStatus.ANY
import software.amazon.awscdk.services.ecr.TagStatus.TAGGED
import software.amazon.awscdk.services.ecs.EcrImage

/** enum 정의 */
class CdkEcr(
    val name: String,
    val expires: Duration = Duration.days(30),
    val maxImageCount: Int = 200,
    val imageTagMutability: TagMutability = TagMutability.IMMUTABLE,
    val tagPrefixList: List<String> = DeploymentType.entries.map { it.name.lowercase() },
) : CdkEnum {

    override val logicalName: String
        get() = "${projectName}-${name}"

    lateinit var iRepository: IRepository

    /** 정의하지 않으면 로지컬 이름으로 대체됨 */
    var repositoryName: String? = null

    private val _repositoryName: String
        get() = repositoryName ?: logicalName

    fun create(stack: Stack): CdkEcr {
        iRepository = Repository(
            stack, "ecr-$logicalName", RepositoryProps.builder()
                .repositoryName(_repositoryName)
                .imageTagMutability(imageTagMutability)
                .imageScanOnPush(true) //디폴트로 스캔 온
                .build()
        ).apply {
            addLifecycleRule(
                LifecycleRule.builder()
                    .rulePriority(100)
                    .maxImageAge(expires)
                    .description("Expire images older than $expires")
                    .tagStatus(TAGGED)
                    .tagPrefixList(tagPrefixList)
                    .build()
            )
            addLifecycleRule(
                LifecycleRule.builder()
                    .rulePriority(200)
                    .maxImageCount(maxImageCount)
                    .description("Expire images limit $maxImageCount")
                    .tagStatus(ANY) //ANY 는 리파지토리당 1개만 허용됨.
                    .build()
            )
        }
        TagUtil.tagDefault(iRepository)
        return this
    }

    fun load(stack: Stack): CdkEcr {
        try {
            iRepository = Repository.fromRepositoryName(stack, "ecr-$logicalName", _repositoryName)
        } catch (e: Exception) {
            println(" -> [${stack.stackName}] object already loaded -> $logicalName")
        }

        return this
    }

    /**
     * latest 확인필요
     * */
    fun imageFromStackByTag(tag: String = "latest"): EcrImage {
        return EcrImage.fromEcrRepository(iRepository, tag)
    }

}