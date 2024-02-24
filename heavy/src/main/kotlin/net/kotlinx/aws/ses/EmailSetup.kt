package net.kotlinx.aws.ses


class EmailSetup {
    //==================================================== 공통 ======================================================
    val smtpUsername: String? = null
    val smtpPassword: String? = null

    //==================================================== 송신 ======================================================
    /** 서울 리즌이 아직 없음.   */
    val smtpHost = "smtps.hiworks.com"
    val smtpPort = 465

    //==================================================== 수신 ======================================================
    val pop3Host = "pop3s.hiworks.com"
    val pop3Port = 995

    //==================================================== 기타 ======================================================
    /** ex) no-reply@xxx.com  */
    var fromEmail: String? = null

    /** ex) 관리자XX  */
    val fromName: String? = null

    /** 아직은??  */
    val encoding = "euc-kr"

    companion object {
        /** AWS 서울리전 이메일 호수트 주소  */
        const val AWS_HOST: String = "email-smtp.ap-northeast-2.amazonaws.com"

        fun from(fromEmail: String?): EmailSetup {
            val emailSetup = EmailSetup()
            emailSetup.fromEmail = fromEmail
            return emailSetup
        }
    }
}
