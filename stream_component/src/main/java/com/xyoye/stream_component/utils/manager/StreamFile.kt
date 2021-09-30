package com.xyoye.stream_component.utils.manager

import java.io.File
import java.io.InputStream

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2021/9/30
 *     desc  :
 * </pre>
 */
class StreamFile(
    private val wrapper: StreamWrapper<*>,
    private val parent: StreamFile?,
    private val fileName: String,
    private val isDirectory: Boolean = false,
    private val fileSize: Long = 0
) {

    companion object {
        val ROOT = File.separator
    }

    private val path: String

    init {
        path = parent?.getChildPath(fileName) ?: fileName
    }

    fun getName(): String {
        return fileName
    }

    fun getParentName(): String? {
        if (isRootDir())
            return null
        return parent!!.getName()
    }

    fun getParent(): StreamFile? {
        if (isRootDir())
            return null
        return parent
    }

    fun getPath(): String {
        return path
    }

    fun isDirectory(): Boolean {
        return isDirectory
    }

    fun isFile(): Boolean {
        return isDirectory.not()
    }

    fun isRootDir(): Boolean {
        return parent == null
    }

    fun getInputStream(): InputStream? {
        return wrapper.getInputStream(this)
    }

    fun listFiles(): List<StreamFile> {
        return wrapper.listFiles(this)
    }

    fun getSize(): Long {
        return fileSize
    }

    fun getChildPath(fileName: String): String {
        if (isRootDir() && ROOT == fileName)
            return path + fileName
        return "$path/$fileName"
    }
}