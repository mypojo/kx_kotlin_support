package net.kotlinx.dropbox

import com.dropbox.core.v2.DbxClientV2

fun DbxClientV2.listAll(path: String): List<com.dropbox.core.v2.files.Metadata> {
    val list = mutableListOf<com.dropbox.core.v2.files.Metadata>()
    var result = this.files().listFolder(path)
    while (true) {
        for (metadata in result.entries) {
            list.add(metadata!!)
        }

        if (!result.hasMore) {
            break
        }

        result = this.files().listFolderContinue(result.cursor)
    }
    return list
}