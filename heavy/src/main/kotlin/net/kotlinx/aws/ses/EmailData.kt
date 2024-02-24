package net.kotlinx.aws.ses

import com.google.common.collect.Maps
import java.io.File
import java.util.*

class EmailData {
    /**
     * 받는사람 <이메일></이메일>,이름>
     */
    private var to: MutableMap<String, String>? = null

    /**
     * 참조      <이메일></이메일>,이름>
     */
    private var cc: MutableMap<String, String>? = null

    /**
     * 숨은참조 <이메일></이메일>,이름>
     */
    private var bcc: MutableMap<String, String>? = null

    //	/** 콜백을 받을 ID */
    //	private Long emailId;
    /**
     * 이메일 제목
     */
    private val subject: String? = null

    /**
     * HTML로 구성된 본문내용
     */
    private val content: String? = null

    /**
     * 첨부파일 (파일,파일명(한글X))
     */
    private var files: MutableMap<File, String>? = null


    //==================================================== 수신 데이터 ======================================================
    private val uid: String? = null
    private val sentDate: Date? = null
    private val from: List<String>? = null

    fun addTo(email: String, name: String) {
        if (to == null) to = Maps.newLinkedHashMap()
        to!![email] = name
    }

    fun addCc(email: String, name: String) {
        if (cc == null) cc = Maps.newLinkedHashMap()
        cc!![email] = name
    }

    fun addBcc(email: String, name: String) {
        if (bcc == null) bcc = Maps.newLinkedHashMap()
        bcc!![email] = name
    }

    @JvmOverloads
    fun addFile(file: File, fileName: String = file.name) {
        if (files == null) files = Maps.newLinkedHashMap()
        files!![file] = fileName
    }
}

