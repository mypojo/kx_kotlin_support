package net.kotlinx.email

import jakarta.mail.Folder
import jakarta.mail.Session
import mu.KotlinLogging
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.core.Kdsl
import java.io.File
import java.util.*

/**
 * SES가 아닌, 일반적인 사내 이메일을 조회하는 도구
 * ex) 서버에서 매일새벽 이메일로 전달된 데이터를 읽어서 정리
 */
class EmailReader {

    @Kdsl
    constructor(block: EmailReader.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정 ======================================================

    /** 계정 */
    lateinit var username: String

    /** 비번 */
    lateinit var password: String

    /** 이메일 서버 호스트 */
    var host: String = "imap.dooray.com"

    /** 기본 프로토콜 */
    var protocol: String = "imaps"

    /** 기본 IMAP 포트 */
    var port: Int = 993

    /** SSL */
    var useSSL: Boolean = true

    /** 작업공간 */
    var workspace: File = AwsInstanceTypeUtil.INSTANCE_TYPE.tmpDir()

    //==================================================== 내부사용 ======================================================

    /** 이름이 session이지만 실제로는 설정정보 저장소 */
    private val session by lazy {
        val props = Properties().apply {
            setProperty("mail.store.protocol", protocol)
            setProperty("mail.imaps.host", host)
            setProperty("mail.imaps.port", port.toString())
            setProperty("mail.imaps.ssl.enable", useSSL.toString())
            setProperty("mail.imaps.ssl.trust", "*")
        }
        Session.getDefaultInstance(props, null)
    }

    /**
     * 사용할때마다 연결 후 close 해줘야함
     * 모드가 동일하기때문에 1개의 store , 1개의 folder를 재사용해서 대량처리한다고 가정한다
     * 폴더 접근 모드가 다를경우, 별도 메소드를 만들것
     *
     * 한번의 커넥트 후 템플릿을 전달하는 전형적인 코드 샘플임
     *  */
    fun <T> connect(block: EmailFolder.() -> T): T {
        val store = session.getStore(protocol)
        log.info { "스토어를 연결합니다. protocol=$protocol host=$host, port=$port, useSSL=$useSSL" }
        store.connect(host, username, password)

        val folder = store.getFolder("INBOX")
        folder.open(Folder.READ_ONLY)

        val emailFolder = EmailFolder(this, folder)

        return try {
            block(emailFolder)
        } finally {
            folder.close(false)  // true인경우 폴더를 닫을 때 삭제 표시된(deleted flag가 설정된) 메시지들을 영구적으로 제거
            store.close() // 예전꺼라 그런지 use 를 사용할 수 없음. 이때문에 콜백 처리
        }
    }


    companion object {
        private val log = KotlinLogging.logger {}
    }

}