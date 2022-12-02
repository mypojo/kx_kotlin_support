package net.kotlinx.aws_all.lambda

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


class Lambda {


}

fun main() {
    println("이거레알")

//    val client1 = LambdaClient {
//        region = "ap-northeast-2"
//        credentialsProvider = DefaultChainCredentialsProvider(profileName = "wabiz")
//    }

//    val response = client1.listFunctions(ListFunctionsRequest { maxItems = 10 })
//    response.functions?.forEach { println(" => ${it.functionName}") }

    runBlocking {
//        val response = client1.listFunctions(ListFunctionsRequest { maxItems = 10 })
//        response.functions?.forEach { println(" => ${it.functionName}") }
        delay(100)
    }

    println("??")



//    LambdaClient {
//        region = "ap-northeast-2"
//        credentialsProvider = DefaultChainCredentialsProvider(profileName = "wabiz")
//    }
//        .use { client ->
//
//
//
//
//        val file = File("C:\\WORKSPACE\\11H11M\\wabiz_cdk\\lambda\\firehose_kr\\aws_lambda1.jar")
//
//        val resp = client.updateFunctionCode(UpdateFunctionCodeRequest {
//            functionName = "firehose_kr"
//            zipFile = file.readBytes()
//        })
//        val resp = client.updateFunctionCode(UpdateFunctionCodeRequest {
//            functionName = "firehose_kr"
//            zipFile = file.readBytes()
//        })
//
//        println(resp.state)
//        println(resp.codeSize)
//
//
//
//    }

}