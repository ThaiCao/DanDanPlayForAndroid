package com.xyoye.stream_component.utils.manager.dav

import com.xyoye.stream_component.utils.manager.StreamParams

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2021/9/30
 *     desc  :
 * </pre>
 */
data class DavParams(
    val libraryId: Int,
    val url: String,
    val isAnonymous: Boolean,
    val account: String?,
    val password: String?,
    val webDavStrict: Boolean
) : StreamParams {

    fun standard(): Boolean {
        return isAnonymous || (account.isNullOrEmpty().not() && password.isNullOrEmpty().not())
    }
}