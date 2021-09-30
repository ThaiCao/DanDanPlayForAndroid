package com.xyoye.stream_component.utils.manager.smb

import com.hierynomus.smbj.auth.AuthenticationContext
import com.xyoye.stream_component.utils.manager.StreamParams

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2021/9/30
 *     desc  :
 * </pre>
 */
data class SmbParams(
    val libraryId: Int,
    val url: String,
    val isAnonymous: Boolean,
    val account: String?,
    val password: String?,
    val sharePath: String?
) : StreamParams {

    fun standard(): Boolean {
        return isAnonymous || (account.isNullOrEmpty().not() && password.isNullOrEmpty().not())
    }

    fun getAuthentication(): AuthenticationContext {
        return if (standard().not() || isAnonymous)
            AuthenticationContext.anonymous()
        else
            AuthenticationContext(account, password!!.toCharArray(), null)
    }
}