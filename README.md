
### AWS 샘플
```kotlin
val awsConfig = AwsConfig(profileName = "sin")
val was = awsConfig.toAwsClient()
was.lambda.listFunctions{ maxItems = 10 }.functions?.map {
    arrayOf(
        it.functionName, it.codeSize, it.functionArn
    )
}?.also {
    listOf("함수명", "코드사이즈", "ARN").toTextGrid(it).print()
}

was.s3.listBuckets{}.buckets?.map {
    arrayOf(
        it.name, it.creationDate?.toLocalDateTime()?.toKr01()
    )
}?.also {
    listOf("이름", "생성날짜").toTextGrid(it).print()
}
```

### AWS 결과
```text
________________________________________________________________________________________________________________________________________________________________________________________________
| 함수명                                                          | 코드사이즈 | ARN                                                                                                           |
|==============================================================================================================================================================================================|
| firehose_kr                                                     | 20699235   | arn:aws:lambda:ap-northeast-2:123456:function:firehose_kr                                                     |
| Cdk05LambdaStack-LogRetentionaae0aa3c5b4d4f87b02d8-8rID6WpQZgda | 5146       | arn:aws:lambda:ap-northeast-2:123456:function:Cdk05LambdaStack-LogRetentionaae0aa3c5b4d4f87b02d8-8rID6WpQZgda |
| Cdk14LambdaStackProd-LogRetentionaae0aa3c5b4d4f87b-NFsZfYnM2fPV | 5146       | arn:aws:lambda:ap-northeast-2:123456:function:Cdk14LambdaStackProd-LogRetentionaae0aa3c5b4d4f87b-NFsZfYnM2fPV |
                                                                                                                                                                                                      
____________________________________________________________________________________________________
| 이름                                                            | 생성날짜                        |
|==================================================================================================|
| aws-cw-widget-athena-query-results-123124124-ap-northeast-2     | 2022년11월07일(월) 14시00분06초 |
| cdk-hnb659fds-assets-123124124-ap-northeast-2                   | 2022년05월26일(목) 14시10분12초 |
| cf-templates-ohkgl1orguai-ap-northeast-2                        | 2022년05월26일(목) 14시48분42초 |
| data-dev                                                        | 2022년11월02일(수) 13시33분34초 |
| data-prod                                                       | 2022년10월21일(금) 11시34분20초 |
```


### JSON 샘플
```kotlin
val json = obj {
    "type" to "normal"
    "members" to arr[
            obj { "name" to "A"; "age" to 10; },
            obj { "name" to "B"; "age" to 20; },
    ]
}

val gsonData = GsonData.parse(json)
gsonData["members"].filter { it["name"].str == "B" }.onEach { it.put("age", 25) }

val sumOfAge = gsonData["members"].sumOf { it["age"].long ?: 0L }
println("sumOfAge : $sumOfAge")
```

### JSON 결과
```text
sumOfAge : 35
```