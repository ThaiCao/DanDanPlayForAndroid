package com.xyoye.stream_component.utils.manager

import java.io.InputStream

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2021/9/30
 *     desc  :
 * </pre>
 */
interface StreamWrapper<P : StreamParams> {

    fun login(params: P): Boolean

    fun getRootFile(): StreamFile?

    fun getInputStream(file: StreamFile): InputStream?

    fun listFiles(parentFile: StreamFile): List<StreamFile>

    fun release()
}