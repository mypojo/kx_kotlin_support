package net.kotlinx.notion.work

import kotlinx.coroutines.runBlocking
import net.kotlinx.core.string.toTextGrid
import net.kotlinx.core.test.TestRoot
import net.kotlinx.test.MyLightKoinStarter
import org.junit.jupiter.api.Test
import java.io.File

class WorkGroupLogic_Test : TestRoot() {

    val workGroupLogic = WorkGroupLogic {
        inputDir = File("D:\\data_sin\\nov")
        backupDir = File("D:\\data_sin\\nov_backup")
        dbId = "4175c466280841dea8918a1c69b80c55"
    }

    init {
        MyLightKoinStarter.startup()
    }

    @Test
    fun `삭제후입력`() = runBlocking {
        workGroupLogic.updateAll()
    }


    @Test
    fun `확인`() {
        val file = workGroupLogic.inputDir.listFiles().first()!!
        val workOutputs = workGroupLogic.read(file)
        workOutputs.map {
            arrayOf(it.workGroup.groupName, it.date, it.name, it.startTime, it.endTime, it.workTimeString, it.valid, it.workType)
        }.let {
            listOf("팀", "날짜", "이름", "출근", "퇴근", "체류시간(점심시간제외)", "정상근무", "근무타입").toTextGrid(it).print()
        }
    }


}