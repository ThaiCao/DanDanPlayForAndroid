package com.xyoye.stream_component.utils.manager.dav

import com.xyoye.common_component.network.helper.UnsafeOkHttpClient
import com.xyoye.common_component.utils.DDLog
import com.xyoye.sardine.impl.OkHttpSardine
import com.xyoye.sardine.util.SardineConfig
import com.xyoye.stream_component.utils.manager.StreamFile
import com.xyoye.stream_component.utils.manager.StreamWrapper
import java.io.InputStream

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2021/9/30
 *     desc  :
 * </pre>
 */
class DavManager: StreamWrapper<DavParams> {

    private var client: OkHttpSardine? = null

    override fun login(params: DavParams): Boolean {
        TODO("Not yet implemented")
    }

    override fun getRootFile(): StreamFile? {
        TODO("Not yet implemented")
    }

    override fun getInputStream(file: StreamFile): InputStream? {
        TODO("Not yet implemented")
    }

    override fun listFiles(parentFile: StreamFile): List<StreamFile> {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }

}