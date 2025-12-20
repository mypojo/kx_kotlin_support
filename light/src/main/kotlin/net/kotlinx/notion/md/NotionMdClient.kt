package net.kotlinx.notion.md

import com.petersamokhin.notionsdk.Notion
import com.petersamokhin.notionsdk.markdown.NotionMarkdownExporter
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import mu.KotlinLogging
import org.koin.core.component.KoinComponent

/**
 * 노션 페이지를 마크다운 형식으로 변환하는 클라이언트
 * - notion-sdk-kotlin 라이브러리를 사용하여 페이지를 마크다운으로 변환
 * - AWS 베드락 날리지 베이스 연동을 위한 간소화된 포맷 제공
 */
class NotionMdClient(
    /** 영구키임!! 주의! */
    private val secretValue: String,
) : KoinComponent {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    /**
     * 페이지 ID를 받아서 마크다운 문자열로 변환
     * @param pageId 노션 페이지 ID
     * @param recursive 하위 블록을 재귀적으로 포함할지 여부 (기본값: true)
     * @param depthLevel 재귀 깊이 (기본값: 3)
     * @return 마크다운 형식의 문자열
     */
    suspend fun toMarkdown(
        pageId: String,
        recursive: Boolean = true,
        depthLevel: Int = 3,
    ): String {

        val httpClient = HttpClient(OkHttp) {
            engine {
                // OkHttp 전용 설정
                config {
                    retryOnConnectionFailure(true)
                    connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                }
            }
        }

        return try {
            // Notion 클라이언트 초기화
            val notion = Notion.fromToken(
                token = secretValue,
                httpClient = httpClient
            )

            // 페이지의 블록 조회
            val blocks = notion.retrieveBlockChildren(pageId).results

            // 마크다운 익스포터 생성
            val exporter = NotionMarkdownExporter.create()

            // 마크다운 변환
            if (recursive) {
                exporter.exportRecursively(
                    blocks = blocks,
                    notion = notion,
                    depthLevel = depthLevel,
                )
            } else {
                exporter.export(blocks = blocks)
            }
        } catch (e: Exception) {
            log.error(e) { "페이지 ID[$pageId] 마크다운 변환 실패" }
            throw e
        } finally {
            httpClient.close()
        }
    }

}
