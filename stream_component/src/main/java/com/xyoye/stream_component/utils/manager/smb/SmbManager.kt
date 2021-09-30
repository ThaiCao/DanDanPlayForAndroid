package com.xyoye.stream_component.utils.manager.smb

import com.hierynomus.msfscc.fileinformation.FileStandardInformation
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.Directory
import com.hierynomus.smbj.share.DiskShare
import com.rapid7.client.dcerpc.mssrvs.ServerService
import com.rapid7.client.dcerpc.transport.SMBTransportFactories
import com.xyoye.common_component.utils.DDLog
import com.xyoye.common_component.utils.IOUtils
import com.xyoye.stream_component.utils.manager.StreamFile
import com.xyoye.stream_component.utils.manager.StreamType
import com.xyoye.stream_component.utils.manager.StreamWrapper
import com.xyoye.stream_component.utils.smb.v2.SMBFileHelper
import java.io.InputStream

/**
 * <pre>
 *     author: xieyy@anjiu-tech.com
 *     time  : 2021/9/30
 *     desc  :
 * </pre>
 */

class SmbManager(val type: StreamType) : StreamWrapper<SmbParams> {

    private var loginParams: SmbParams? = null

    private var client: SMBClient? = null
    private var connection: Connection? = null
    private var session: Session? = null

    private var diskShare: DiskShare? = null

    private val shares = mutableListOf<StreamFile>()

    override fun login(params: SmbParams): Boolean {
        release()

        if (params.standard().not()) {
            DDLog.e("SMB连接参数错误: $params")
            return false
        }

        try {
            loginParams = params
            client = SMBClient()
            connection = client?.connect(params.url)
            session = connection?.authenticate(params.getAuthentication())
        } catch (e: Exception) {
            DDLog.e("SMB服务连接失败: ${e.message}")
            release()
            return false
        }
        return true
    }

    override fun getRootFile(): StreamFile? {
        if (session == null)
            return null
        return StreamFile(this, null, StreamFile.ROOT, true)
    }

    override fun getInputStream(file: StreamFile): InputStream? {
        if (file.isDirectory())
            return null

        if (confirmConnection().not())
            return null

        if (confirmDiskShare(file).not())
            return null

        return SMBFileHelper.openFile(
            diskShare!!, file.getPath()
        ).inputStream
    }

    override fun listFiles(parentFile: StreamFile): List<StreamFile> {
        if (parentFile.isFile())
            return emptyList()

        if (confirmConnection().not())
            return emptyList()

        if (parentFile.isRootDir())
            return listRoot(parentFile)

        if (confirmDiskShare(parentFile).not())
            return emptyList()

        val smbDirectory = if (shares.contains(parentFile))
            SMBFileHelper.openDirectory(diskShare!!, "")
        else
            SMBFileHelper.openDirectory(diskShare!!, parentFile.getPath())

        return listDirectory(parentFile, smbDirectory)
    }

    override fun release() {
        shares.clear()
        loginParams = null
        IOUtils.closeIO(diskShare)
        IOUtils.closeIO(session)
        IOUtils.closeIO(connection)
        IOUtils.closeIO(client)
    }

    private fun confirmConnection(): Boolean {
        if (connection?.isConnected == true)
            return true

        if (loginParams == null)
            return false

        return login(loginParams!!)
    }

    private fun confirmDiskShare(file: StreamFile): Boolean {
        if (diskShare?.isConnected == true)
            return true

        if (diskShare?.smbPath?.shareName == getPathShare(file.getPath())) {
            return true
        }

        IOUtils.closeIO(diskShare)
        try {
            diskShare = session?.connectShare(file.getPath()) as DiskShare
        } catch (e: Exception) {
            DDLog.e("打开SMB共享目录失败: ${e.message}")
            return false
        }
        return true
    }

    private fun getPathShare(path: String): String {
        var share = path
        if (share.startsWith("/"))
            share = share.substring(1)
        val shareIndex = share.lastIndexOf("/")
        if (shareIndex >= 0)
            share = share.substring(0, shareIndex)
        return share
    }

    private fun listRoot(root: StreamFile): List<StreamFile> {
        shares.clear()

        val transport = SMBTransportFactories.SRVSVC.getTransport(session)
        val serverService = ServerService(transport)
        serverService.shares0.forEach {
            val share = StreamFile(this, root, it.netName, true)
            shares.add(share)
        }
        return shares
    }

    private fun listDirectory(dir: StreamFile, smbDir: Directory): List<StreamFile> {
        val childFiles = mutableListOf<StreamFile>()
        for (childFile in smbDir.list()) {
            if ("." == childFile.fileName || ".." == childFile.fileName)
                continue

            val childInfo = smbDir.getFileInformation(FileStandardInformation::class.java)
            val smbFile = StreamFile(
                this,
                dir,
                childFile.fileName,
                childInfo.isDirectory,
                childInfo.endOfFile
            )

            childFiles.add(smbFile)
        }
        return childFiles
    }
}