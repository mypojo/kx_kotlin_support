package net.kotlinx.aws.lambda.dispatch.synch.s3Logic

/**
 * S3에 결과 디렉토리 변환
 * */
data class S3LogicPath(
    /** 전달된 S3 path */
    var s3InputDataKey: String = ""
) {

    private val inputPaths by lazy { s3InputDataKey.split("/") }

    /** 파일 명 (path에서 추출) */
    val fileName: String by lazy { inputPaths.last() }

    /**
     * 파일 디렉토리 폴더
     * ex) sfnId
     *  */
    val pathId: String by lazy { inputPaths[inputPaths.size - 2] }

    /**
     * 결과가 저장될 S3 디렉토리 key  (/로 끝나야함)
     * 여기에 ID와 파일 이름을 더해서 경로를 완성하면됨
     * {버킷}/{outputDir}{id}/xxx.csv.gz 이런식으로 저장됨
     * 좀 이상하지만.. 이런 규칙으로 쓰자
     *  */
    val outputDir: String by lazy {
        val orgPath = inputPaths.dropLast(2)
        val newPath = orgPath.dropLast(1) + orgPath.last().replace("Input", "Output")
        newPath.joinToString("/") + "/"
    }

}