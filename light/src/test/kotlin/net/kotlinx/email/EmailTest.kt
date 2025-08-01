package net.kotlinx.email

import jakarta.mail.Folder
import jakarta.mail.Session
import jakarta.mail.Store
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import java.util.*


class EmailTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.PROJECT)

        Given("LazyLoadFileProperty") {


            When("IMAP") {


                Then("이메일 리스팅") {

                    fun fetchRecentEmails(username: String, password: String, host: String = "imap.dooray.com", port: Int = 993) {
                        val props = Properties().apply {
                            put("mail.store.protocol", "imaps")
                            put("mail.imaps.host", host)
                            put("mail.imaps.port", port.toString())
                            put("mail.imaps.ssl.enable", "true")
                        }

                        val session = Session.getInstance(props, null)

                        var store: Store? = null
                        var inbox: Folder? = null

                        try {
                            store = session.getStore("imaps")
                            store.connect(host, username, password)

                            inbox = store.getFolder("INBOX")
                            inbox.open(Folder.READ_ONLY)

                            // 최근 메시지 순서대로 가져오기 (전체 메시지 중 10개만 예로 조회)
                            val messageCount = inbox.messageCount
                            val start = if (messageCount - 9 > 0) messageCount - 9 else 1
                            val messages = inbox.getMessages(start, messageCount)

                            for (msg in messages) {
                                println("From: ${msg.from.joinToString()}")
                                println("Subject: ${msg.subject}")
                                println("Received Date: ${msg.receivedDate}")
                                println("-------------------------------")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            inbox?.close(false)
                            store?.close()
                        }
                    }

                    fetchRecentEmails("yy", "xx")


                }

            }
        }
    }


}