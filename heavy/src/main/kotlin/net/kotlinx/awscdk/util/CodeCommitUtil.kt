package net.kotlinx.awscdk.util

import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.codecommit.IRepository
import software.amazon.awscdk.services.codecommit.Repository
import software.amazon.awscdk.services.codecommit.RepositoryProps


object CodeCommitUtil {

    private fun toLogicalName(name: String) = "codecommit-$name"

    fun create(stack: Stack, name: String, desc: String = "$name sourcecode"): IRepository {
        return Repository(stack, toLogicalName(name), RepositoryProps.builder().repositoryName(name).description(desc).build())
    }

    fun load(stack: Stack, name: String): IRepository {
        return Repository.fromRepositoryName(stack, toLogicalName(name), name)
    }


}