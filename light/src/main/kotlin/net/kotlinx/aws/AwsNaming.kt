package net.kotlinx.aws

/**
 * AWS 공통으로 사용되는 키값 네이밍 통일
 * 범용적인 네이밍으로 사용할것
 *  */
object AwsNaming {

    //==================================================== 로직의 옵션값 관련 ======================================================

    /**
     * 각종 내부 로직의 분기 구분값
     * ex) 공용 람다 사용시 분기처리 키값
     *  */
    const val METHOD = "method"

    /** 모드 */
    const val MODE = "mode"

    /**
     * json 옵션값의 키값
     * * ex) 공용 람다 사용시 입력 옵션 json
     *  */
    const val OPTION: String = "option"

    /**
     * 스케쥴된 시간
     * ex) SFN 예약시간
     * */
    const val SCHEDULE_TIME: String = "scheduleTime"

    /**
     * 대기 시간
     * ex) SFN 대기시간
     * */
    const val WAIT_SECONDS: String = "waitSeconds"

    /** 콜드스타트 대기시간 */
    const val WAIT_COLDSTART_SECONDS: String = "waitColdstartSeconds"

    /** 결과 등의 본문 */
    const val BODY: String = "body"

    //==================================================== 버킷 관련 ======================================================
    const val BUCKET = "bucket"

    const val KEY = "key"

    //==================================================== 문자 패턴 ======================================================

    /** 리트라이 */
    const val CHOICE_RETRY: String = "retry"

    //==================================================== JOB ======================================================

    /** 여러가지 용도로 사용되는 잡의 pk. */
    const val JOB_PK = "jobPk"

    /** 여러가지 용도로 사용되는 잡의 sk.  */
    const val JOB_SK = "jobSk"

    /** 여러가지 용도로 사용되는 잡의 soption  */
    const val JOB_OPTION = "jobOption"

    /** 스프링 관련 */
    object Spring {

        /** JVM 파라메터 프로파일 키 */
        const val JVM_PROFILE = "-Dspring.profiles.active"

        /** 환경변수 프로파일 키*/
        const val ENV_PROFILE = "SPRING_PROFILES_ACTIVE"
    }

    //==================================================== CICD ======================================================

    /** 환경변수의 커밋ID  */
    const val COMMIT_ID = "COMMIT_ID"

    //==================================================== 이벤트 ======================================================

    object EventBridge {

        /**
         * AWS 이벤트브릿지 표쥰 형식의 소스
         *  */
        const val SOURCE = "source"

        /** 디테일 타입  */
        const val DETAIL_TYPE = "detail-type"

        /** SNS를 거치면 카멜로 변경됨 */
        const val DETAIL_TYPE_SNS = "detailType"

    }

    object Event {

        /**
         * 이벤트 소스로 넘어오는것들
         * ex) SQS
         * 보통 Records 안에 배열 형태로 넘어옴 -> 각각 분리해서 이벤트 발생
         *  */
        const val EVENT_SOURCE = "eventSource"

        /**
         * 아마존이 네이밍 통일전에 만든거 그냥 쓰는용도. 카멜 형식이 맞는 표현임
         * ex) SNS
         * */
        const val EVENT_SOURCE2 = "EventSource"


    }


}