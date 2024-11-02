package net.kotlinx.domain.batchTask

import net.kotlinx.domain.job.Job

/**
 * 이 경로는 파일명으로 사용할 수 있어야 한다
 * ENUM 으로 못쓰는 . 으로 연결함.
 *  */
val Job.batchTaskSk: String
    get() = "job.${pk}.${sk}"