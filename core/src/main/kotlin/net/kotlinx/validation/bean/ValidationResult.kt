package net.kotlinx.validation.bean

/**
 * 결과메세지를 만들기 위한 템플릿용 빈
 * jakarta ConstraintViolation 와 호환된다.
 */
data class ValidationResult(

    /**
     * 예외 코드 (어노테이션 이름)
     * ex) maxFileCnt
     */
    val code: String,

    /**
     * 프로퍼티 영문명 (실제 bean의 필드 name)
     * form에서 예외 하이라이트 등을 할때 사용함
     *  */
    val fieldId: String,

    /**
     * 필드명 (한글명)
     *  */
    val fieldName: String,

    /** 거부된 값 */
    val invalidValue: Any? = null,

    /**
     * 어노테이션의 속성
     * ex) maxSize, limitCnt 등..
     *  */
    val attr: Map<String, Any?>? = null,

    /**
     * 사용자가 보게되는 최종 메세지
     *  */
    val message: String,


    )