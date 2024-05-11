package net.kotlinx.domain.job


import net.kotlinx.string.TextGrid
import net.kotlinx.string.toTextGrid
import net.kotlinx.time.toKr01
import net.kotlinx.time.toTimeString

object JobPrintUtil {


    /** 간단 버전.  */
    fun job01(jobs: Collection<Job>): TextGrid {
        return jobs.sortedBy { it.reqTime }.reversed().map {
            arrayOf(
                it.pk, it.sk, it.jobStatus, it.awsInfo?.instanceType,
                it.reqTime.toKr01(),
                it.startTime?.toKr01() ?: "-",
                it.toIntervalMills()?.toTimeString(),
            )
        }.let {
            listOf("pk", "sk", "jobStatus", "instanceType", "reqTime", "startTime", "작동시간").toTextGrid(it)
        }
    }

    /** 로그링크 추가 */
    fun job02(jobs: Collection<Job>): TextGrid {
        return jobs.sortedBy { it.reqTime }.reversed().map {
            arrayOf(
                it.pk, it.sk, it.jobStatus, it.awsInfo?.instanceType,
                it.reqTime.toKr01(),
                it.startTime?.toKr01() ?: "-",
                it.toIntervalMills()?.toTimeString(),
                it.toLogLink(),
            )
        }.let {
            listOf("pk", "sk", "jobStatus", "instanceType", "reqTime", "startTime", "작동시간", "로그링크").toTextGrid(it)
        }
    }

    fun jobGroupBy01(allJobs: Collection<Job>): TextGrid {
        return allJobs.groupBy { it.pk }.values.map { it.toJobSta() }.map { jobSta ->
            arrayOf(
                jobSta.job.pk,
                jobSta.startMin.toKr01(),
                jobSta.startMax.toKr01(),
                jobSta.cnt,
                jobSta.failCnt,
                jobSta.avgOfDuration,
                jobSta.maxOfDuration,
                jobSta.sumOfDuration.toTimeString(),
                jobSta.sumOfCost
            )
        }.let {
            listOf("jobDiv", "최초실행(min)", "최근실행(max)", "실행수", "실패수", "진행시간(평균)", "진행시간(최대)", "누적실행시간", "월비용(만원)").toTextGrid(it)
        }
    }

}