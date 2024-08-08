package net.kotlinx.validation.bean

/**
 * 결과메세지를 만들기 위한 템플릿용 빈
 * 
 * 아래 규격을 맞추기 외해서 만들었다
 * #1 jakarta ConstraintViolation 자바 표준 벨리데이션
 *
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

    /**
     * 사용자가 보게되는 최종 메세지
     *  */
    val message: String,

    /** 거부된 값 */
    val invalidValue: Any? = null,

    /**
     * 어노테이션의 속성
     * ex) maxSize, limitCnt 등..
     *  */
    val attr: Map<String, Any?>? = null,


    )