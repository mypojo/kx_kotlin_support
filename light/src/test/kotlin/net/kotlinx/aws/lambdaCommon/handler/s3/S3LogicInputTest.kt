package net.kotlinx.aws.lambdaCommon.handler.s3

import net.kotlinx.kotest.BeSpecLog
import org.junit.jupiter.api.Test


class S3LogicInputTest : BeSpecLog(){
    init {
        @Test
        fun test() {

            val input = S3LogicInput("defaultLogic", listOf("a", "b", "c"))
            val json = input.toJson()
            println(json)
            val out = S3LogicInput.parseJson(json)
            println(out)


            val path = S3LogicPath("upload/sfnBatchModuleInput/16a23f53-15b1-4b48-a628-7708b5b3daee/00000.txt")
            println(path.fileName)
            println(path.pathId)
            println(path.outputDir)

        }
    }
}