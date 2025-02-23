package net.kotlinx.awscdk.cicd

import net.kotlinx.awscdk.basic.TagUtil
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.codecommit.IRepository
import software.amazon.awscdk.services.codecommit.Repository
import software.amazon.awscdk.services.codecommit.RepositoryProps


object CodeCommitUtil {

    private fun toLogicalName(name: String) = "codecommit-$name"

    fun create(stack: Stack, name: String, desc: String = "$name sourcecode"): IRepository {
        val repository = Repository(stack, toLogicalName(name), RepositoryProps.builder().repositoryName(name).description(desc).build())
        TagUtil.tagDefault(repository)
        return repository
    }

    fun load(stack: Stack, name: String): IRepository {
        return Repository.fromRepositoryName(stack, toLogicalName(name), name)
    }


}