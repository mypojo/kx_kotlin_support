package net.kotlinx.google

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.sheets.v4.SheetsScopes
import net.kotlinx.core.Kdsl
import net.kotlinx.file.slash
import net.kotlinx.system.ResourceHolder
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

/**
 * 아래 2가지 방식을 지원함
 * #1. API 키 -> 그냥 따면 되지만, 사용못하는 서비스가 너무 많은듯(401 Unauthorized남) -> 사용 가능한지는 해당 서비스의 "사용자 인증 정보"를 가볼것 (불확실)
 * #2. OAuth 2.0 클라이언트 ID -> 브라우저로 인증해야하는 귀찮음이 조금 있음. 호스트 권한 생성이 상당히 빡빡해짐. 유투브 링크까지 걸어야한다. -> 이경우 테스트로 전환해서 할것. 100명 제한이 있지만 넉넉함.
 *   => 이 토큰은 (주기적으로 expire 되는듯.. 쓰기 힘들다. 권한 변경시 캐싱 지우도 새로 받을것)
 *
 * ...
 *
 * ###### 작업방법 ######
 * #1. google 계정 생성 ex) 호스트-myProject, 게스트-돈주는업체 ???
 * #2. 호스트 계정으로 API 권한 만들기  -> https://console.cloud.google.com/projectselector2/apis/dashboard?pli=1&supportedpurview=project
 * #3 프로젝트 생성 & 원하는 서비스 활성화 -> OAuth 2.0 클라이언트 ID -> 동의화면 만들기 (유니크이름지정,시트읽고쓰기 권한부여)
 * #4 OAuth 2.0 생성 (데스크톱앱) -> 시크릿 다운로드 -> google-sheet-secret 로 이름 바꿔서 저장 & 시크릿 스토어로 저장
 */
class GoogleSecret {

    @Kdsl
    constructor(block: GoogleSecret.() -> Unit = {}) {
        apply(block)
    }

    /** 어플 네임 (각 서비스를 인스턴스화 할때 사용됨)  */
    var applicationName: String = "Google Service"

    /** 토큰에 부여받을 권한  */
    var scopes = listOf(
        SheetsScopes.SPREADSHEETS,
        CalendarScopes.CALENDAR
    )

    /**
     * 시크릿을 저장할 디렉터리
     * 디폴트로 자주 사용되는 디렉토리 지정
     * ex) C:\Users\${userName}\.google
     *  */
    var secretDir: File = ResourceHolder.USER_ROOT.slash(".google")


    /**
     * 클라이언트 시크릿 파일.  편의상 secretDir 에 같이 쓴다.
     * SSM 등에서 데이터를 가져와서 여기에 파일로 만들것
     *  */
    var secretClientFile: File = secretDir.slash(SECRET_CLIENT_FILE_NAME)

    /**
     * 시크릿 파일
     * secretDir 를 지정해주면, 구글SDK가 내부 시크릿을 읽어가는 구조임
     * */
    val secretFile: File
        get() = secretDir.slash(SECRET_STORED_FILE_NAME)

    //==================================================== 내부 사용 ======================================================

    /** 같이 사용 */
    val transport = GoogleNetHttpTransport.newTrustedTransport()!!

    /** 걍 JSON 파싱하는애  */
    val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()

    /** 최종 크리덴셜 */
    val credential: Credential by lazy {
        val clientSecrets = GoogleClientSecrets.load(jsonFactory, InputStreamReader(FileInputStream(secretClientFile)))
        val flow =
            GoogleAuthorizationCodeFlow.Builder(transport, jsonFactory, clientSecrets, scopes)
                .setDataStoreFactory(FileDataStoreFactory(secretDir))  //디렉토리만 지정함.. 이렇게만 구글이 지원한다.
                .setAccessType("offline").build()
        val receiver: LocalServerReceiver = LocalServerReceiver.Builder().setPort(TEMP_PORT).build()
        AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    /** API KEY 방식인 경우 샘플 (사용 안함) */
    fun buildHttpRequestInitializer(apiKey: String): HttpRequestInitializer = HttpRequestInitializer { httpRequest ->
        httpRequest.headers.authorization = "Bearer $apiKey" //정확하지 않음
    }

    fun createService(): GoogleService = GoogleService(this)

    companion object {

        /** 계정 인증받을 임시포트  */
        const val TEMP_PORT = 8888

        /**
         * OAuth2 승인 후 저장되는 파일 명
         * file로 프로퍼티를 전달하는걸 모르겠어서 그냥 함
         *  */
        const val SECRET_STORED_FILE_NAME = "StoredCredential"

        /**
         * 인증에 필요한 클라이언트 시크릿 파일명 (꼭 이 이름으로 해야하는건 아님)
         * 다운로드받은 웹 애플리케이션의 클라이언트 ID 의 보안 비밀번호 파일 이름
         * 이걸로 리네이밍 해서 secretDir 에 저장할것
         * */
        const val SECRET_CLIENT_FILE_NAME: String = "secret.json"
    }
}