package net.kotlinx.openAi

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.AssistantId
import com.aallam.openai.api.assistant.AssistantTool
import com.aallam.openai.api.core.Role
import com.aallam.openai.api.core.Status
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.file.FileUpload
import com.aallam.openai.api.file.Purpose
import com.aallam.openai.api.message.Attachment
import com.aallam.openai.api.message.Message
import com.aallam.openai.api.message.MessageRequest
import com.aallam.openai.api.run.RunRequest
import com.aallam.openai.api.thread.Thread
import com.aallam.openai.api.thread.ThreadId
import com.aallam.openai.api.vectorstore.ExpirationPolicy
import com.aallam.openai.api.vectorstore.VectorStoreFileRequest
import com.aallam.openai.api.vectorstore.VectorStoreRequest
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import mu.KotlinLogging
import net.kotlinx.collection.doUntilTimeout
import net.kotlinx.concurrent.CoroutineSleepTool
import net.kotlinx.concurrent.delay
import net.kotlinx.time.TimeStart
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**

 *  */
@OptIn(BetaOpenAI::class) //스래드 사용
class OpenAiThread(
    private val client: OpenAiClient,
    val assistantId: String,
    val threadId: String,
) {

    private val ai = client.ai

    /**
     * 기본 정책
     * 참고로 기본 콘솔에서 입력시 7일로 설정됨
     *  */
    var expirationPolicy = ExpirationPolicy("last_active_at", 2)

    /** 서로 다른 JVM에서도 실행될 수 있기때문에 캐싱하지 않는다 */
    val thread: Thread by lazy {
        runBlocking {
            log.info { "thread [$threadId] 초기화" }
            client.ai.thread(id = ThreadId(threadId)) ?: client.ai.thread()
        }
    }

    /**
     * @param localFiles 모든 파일은 재사용하지 않고, 1회용으로 간주한다. 글로벌 적용은 Assistants 로 할것!
     * */
    suspend fun message(msg: String, localFiles: List<File> = emptyList()): List<Message> {

        val attachments: List<Attachment>? = run {
            if (localFiles.isEmpty()) return@run null

            /** 메세지당 하나 만들어씀 */
            val tempVectorStore = client.ai.createVectorStore(VectorStoreRequest(name = "temp-${UUID.randomUUID()}", expiresAfter = expirationPolicy))
            localFiles.map { localFile ->
                val uploadedFile = client.ai.file(
                    request = FileUpload(
                        file = FileSource(Path(localFile.absolutePath)),
                        purpose = Purpose("user_data")  //'fine-tune', 'assistants', 'batch', 'user_data', 'responses', 'vision', 'evals'
                    )
                )
                client.ai.createVectorStoreFile(tempVectorStore.id, VectorStoreFileRequest(uploadedFile.id))
            }
            val completed = doUntilTimeout(2.seconds, 1.minutes) {
                val vectorStoreFiles = client.ai.vectorStoreFiles(tempVectorStore.id)
                val allOk = vectorStoreFiles.all { it.status in setOf(Status.Processed, Status.Queued, Status.Running, Status.InProgress) }
                if (allOk) null else vectorStoreFiles
            }
            log.info { "벡터 스토어 ${tempVectorStore.id} 파일 ${completed.size}건 변환완료" }
            completed.map {
                Attachment(
                    fileId = it.id,
                    tools = listOf(AssistantTool.FileSearch) //파일서치 = 기본적인 파일 업로드
                )
            }
        }


        val start = TimeStart()
        log.trace { "메세지를 먼저 만들어야함" }
        ai.message(
            threadId = thread!!.id,
            request = MessageRequest(
                role = Role.User,
                content = msg,
                attachments = attachments
            )
        )

        val run = ai.createRun(
            threadId = thread!!.id,
            request = RunRequest(
                assistantId = AssistantId(assistantId)
            )
        )
        log.trace { " -> [$threadId] run status: ${run.status}" }

        6.seconds.delay() //기본 딜레이

        var currentRun = run
        val sleeper = CoroutineSleepTool(2.seconds)
        while (currentRun.status != Status.Completed) {
            log.trace { " -> [$threadId] run status: ${currentRun.status}" }
            sleeper.checkAndSleep()
            currentRun = ai.getRun(
                threadId = thread!!.id,
                runId = run.id
            )
        }

        val messages = ai.messages(
            threadId = thread!!.id
        )

        log.info { "결과메세지 ${messages.size}건 -> ${start}" }
        return messages.filter { it.role == Role.Assistant }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }

}