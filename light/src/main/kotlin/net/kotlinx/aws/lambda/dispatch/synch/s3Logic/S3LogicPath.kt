package net.kotlinx.aws.lambda.dispatch.synch.s3Logic

/**
 * S3에 결과 디렉토리 변환
 * 경로에 SFN ID 가 반드시 있어야 한다
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
     * 뒤에서 2번째부터 읽어가면서 '-'가 포함된 첫 번째 요소를 반환
     *  */
    val pathId: String by lazy {
        inputPaths.asReversed()
            .drop(1) // 마지막 요소(파일명) 제외
            .firstOrNull { it.contains('-') && it.contains('.') }
            ?: throw IllegalStateException("'-'가 포함된 pathId를 찾을 수 없습니다: $s3InputDataKey")
    }

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