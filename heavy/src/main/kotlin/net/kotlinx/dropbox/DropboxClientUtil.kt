package net.kotlinx.dropbox

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2


/**
 * https://github.com/dropbox/dropbox-sdk-java#setup
 * https://www.dropbox.com/developers/apps
 * */
object DropboxClientUtil {

    fun create(clientIdentifier: String, accessToken: String): DbxClientV2 {
        val config = DbxRequestConfig.newBuilder("kotlinx").build()
        return DbxClientV2(config, accessToken)
    }


}
