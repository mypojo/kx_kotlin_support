package net.kotlinx.awscdk.component

import net.kotlinx.awscdk.CdkEnum
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
    val tagPrefixList: List<String> = DeploymentType.entries.map { it.name },
) : CdkEnum {

    override val logicalName: String
        get() = "${project.projectName}-${name}"

    lateinit var iRepository: IRepository

    fun create(stack: Stack): CdkEcr {
        iRepository = Repository(stack, "ecr-$logicalName", RepositoryProps.builder().repositoryName(logicalName).imageTagMutability(imageTagMutability).build()).apply {
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
        return this
    }

    fun load(stack: Stack): CdkEcr {
        iRepository = Repository.fromRepositoryName(stack, "ecr-$logicalName", logicalName)
        return this
    }

    fun imageFromStackByTag(tag: String): EcrImage {
        return EcrImage.fromEcrRepository(iRepository, tag)
    }

}