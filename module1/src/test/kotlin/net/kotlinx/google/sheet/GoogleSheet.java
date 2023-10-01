//package net.kotlinx.google.sheet;
//
//import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
//import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
//import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.client.util.store.FileDataStoreFactory;
//import com.google.api.services.sheets.v4.Sheets;
//import com.google.api.services.sheets.v4.SheetsScopes;
//import com.google.api.services.sheets.v4.model.ValueRange;
//
//import java.io.*;
//import java.util.Collections;
//import java.util.List;
//
///**
// * 주의!.
// * 제한적인 값만 사용 가능하다. 색칠놀이는 미리 템플릿화 해놓고 읽기/오버라이드 하는 구조로 사용할것!
// *
// * https://developers.google.com/sheets/api/guides/authorizing  : API 키만 받아서는 안되고  OAuth 2.0 token 인가를 받아야 하는듯
// * https://developers.google.com/sheets/api/guides/values
// * https://developers.google.com/sheets/api/guides/conditional-format
// *
// *
// * 탭 이름은 직사각형 형태의 구간을 입력. 형식 맞워야함
// * Sheet1!1:2  <--  라인 1~2
// * 작업!A3:C3  <-- 작업 시트의 A3 부터 C3 까지
// *
// * ###### 제한 ######
// * 최대 로우 수 : 40,000
// *
// * ###### 권한 ######
// * 호스트 권한 생성이 상당히 빡빡해짐. 유투브 링크까지 걸어야한다.
// * 이경우 테스트로 전환해서 할것. 100명 제한이 있지만 넉넉함.
// *
// * ###### 작업방법 ######
// * 1. google 계정 생성 ex) 호스트-11h11m, 게스트-NHNAD
// * 2. 호스트 계정으로 API 권한 만들기
// *   https://console.cloud.google.com/projectselector2/apis/dashboard?pli=1&supportedpurview=project
// * 	 프로젝트 생성 - google-sheet
// * 	 google-sheet-api 사용 -> 인증정보 만들기 -> OAuth 2.0 클라이언트 ID -> 동의화면 만들기 (유니크이름지정,시트읽고쓰기 권한부여)
// * 	 OAuth 2.0 생성 (데스크톱앱) -> 시크릿 다운로드 -> google-sheet-secret 로 이름 바꿔서 저장
// * 2. 호스트에 시트 만들고 게스트한테 권한 줌
// * 3. 로컬 PC에서 구글시트 편집 시작 -> WAS가 임시로 올라가면서 호스트 계정에서 권한 할당받음 -> StoredCredential 가 생성됨 (주기적으로 expire 되는듯.. 쓰기 힘들다)
// * */
//public class GoogleSheet {
//
//	/** 걍 JSON 파싱하는애 */
//	private static final JsonFactory JSON_FACTORY = JsonFactory.getDefaultInstance();
//	/** 계정 인증받을 임시포트 */
//	public static final int TEMP_PORT = 8888;
//
//	///==================================================== 설정 ======================================================
//
//	/** 어플 네임 */
//	@Setter private String applicationName = "Google Sheet";
//	/** 토큰에 부여받을 권한 */
//	@Setter private List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);
//
//	///==================================================== 내부사용 ======================================================
//
//	/** 시트 본체 */
//	private Sheets service;
//
//
//	//==================================================== 생성 ======================================================
//	/**
//	 * ex) InputStream in = GoogleSheet.class.getResourceAsStream(sourcePath);
//	 * */
//	public GoogleSheet init(InputStream in, File dataDirectory){
//		InputStreamReader secret = new InputStreamReader(in);
//		try {
//			final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
//			Credential credential = getOrMakeCredentials(netHttpTransport, secret, dataDirectory);
//			service = new Sheets.Builder(netHttpTransport, JSON_FACTORY, credential).setApplicationName(applicationName).build();
//		} catch (Exception e) {
//			throw ExceptionUtil.toRuntimeException(e);
//		}
//		return this;
//	}
//
//	public GoogleSheet init(File file, File dataDirectory){
//		try {
//			return init(new FileInputStream(file), dataDirectory);
//		} catch (FileNotFoundException e) {
//			throw ExceptionUtil.toRuntimeException(e);
//		}
//	}
//
//	/**
//	 * 인증 정보가 캐시되어있지 않다면 8888로 로컬 WAS를 올려서 구글에 인증 후 리턴을 받는다. 신박한 방법!
//	 * */
//	private Credential getOrMakeCredentials(final NetHttpTransport netHttpTransport, Reader secret, File dataDirectory) throws Exception {
//		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, secret);
//		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(netHttpTransport, JSON_FACTORY, clientSecrets, scopes)
//				//.setDataStoreFactory(new FileDataStoreFactory(dataDirectory)).setAccessType("offline").build();
//				.setDataStoreFactory(new FileDataStoreFactory(dataDirectory)).setAccessType("offline").build();
//		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(TEMP_PORT).build();
//		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
//	}
//
//
//	///==================================================== 조회 ======================================================
//
//	/**
//	 * @param  sheetId : 구글 시트의 ID 문자열 https://docs.google.com/spreadsheets/d/spreadsheetId/edit#gid=sheetId
//	 * @param tabName : 탭의 이름(한글 가능)
//	 * */
//	public List<List<Object>> load(String sheetId,String tabName){
//		try {
//			ValueRange response = service.spreadsheets().values().get(sheetId, tabName).setValueRenderOption("UNFORMATTED_VALUE").execute(); //https://developers.google.com/sheets/api/reference/rest/v4/ValueRenderOption
//			return response.getValues();
//		} catch (IOException e) {
//			throw ExceptionUtil.toRuntimeException(e);
//		}
//	}
//
//	///==================================================== 쓰기 ======================================================
//
//	/**
//	 * 해당 구간에 value만 오버라이드 한다. (컬러같은건 안바뀜)
//	 * */
//	public void write(String sheetId, String range,List<List<Object>> values){
//		try {
//			Sheets.Spreadsheets.Values sheet = service.spreadsheets().values();
//			ValueRange body = new ValueRange().setValues(values);
//			sheet.update(sheetId, range, body).setValueInputOption("USER_ENTERED").execute();
//		} catch (IOException e) {
//			throw ExceptionUtil.toRuntimeException(e);
//		}
//	}
//
//
//
//}
