package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.core.DeploymentType
import org.junit.jupiter.api.Test

class CdkBatchJobDefinitionTest {

    @Test
    fun test() {
        val definition = CdkBatchJobDefinition(CdkProject("", "aa"), "bb", "", 1)
        //로지컬 명이 가변적이어야함
        definition.deploymentType = DeploymentType.DEV
        check(definition.logicalName.endsWith(DeploymentType.DEV.name))
        definition.deploymentType = DeploymentType.PROD
        check(definition.logicalName.endsWith(DeploymentType.PROD.name))
    }

}