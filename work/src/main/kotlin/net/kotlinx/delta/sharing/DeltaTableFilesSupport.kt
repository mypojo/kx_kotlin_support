package net.kotlinx.delta.sharing

import io.delta.sharing.client.model.DeltaTableFiles
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.toGsonData


//==================================================== 데이터 3종. 매우 이상하다.. ======================================================

val DeltaTableFiles.protocol: GsonData get() = this.lines().toKoltinList()[0].toGsonData()["protocol"]

val DeltaTableFiles.metaData: GsonData get() = this.lines().toKoltinList()[1].toGsonData()["metaData"]

/** 메타데이터의 스키마 */
val DeltaTableFiles.metaDataSchemaFields: GsonData get() = this.metaData["deltaMetadata"]["schemaString"].str!!.toGsonData()["fields"]

val DeltaTableFiles.file: GsonData get() = this.lines().toKoltinList()[2].toGsonData()["file"]




