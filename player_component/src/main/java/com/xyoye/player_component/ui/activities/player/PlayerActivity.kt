package com.xyoye.player_component.ui.activities.player

import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.bridge.PlayTaskBridge
import com.xyoye.common_component.config.DanmuConfig
import com.xyoye.common_component.config.PlayerConfig
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.config.SubtitleConfig
import com.xyoye.common_component.receiver.BatteryBroadcastReceiver
import com.xyoye.common_component.receiver.HeadsetBroadcastReceiver
import com.xyoye.common_component.receiver.PlayerReceiverListener
import com.xyoye.common_component.receiver.ScreenBroadcastReceiver
import com.xyoye.common_component.utils.JsonHelper
import com.xyoye.common_component.weight.dialog.CommonDialog
import com.xyoye.data_component.bean.PlayParams
import com.xyoye.data_component.enums.*
import com.xyoye.player.controller.VideoController
import com.xyoye.player.info.PlayerInitializer
import com.xyoye.player_component.BR
import com.xyoye.player_component.R
import com.xyoye.player_component.databinding.ActivityPlayerBinding
import java.io.File

@Route(path = RouteTable.Player.PlayerCenter)
class PlayerActivity : BaseActivity<PlayerViewModel, ActivityPlayerBinding>(),
    PlayerReceiverListener {

    @Autowired
    @JvmField
    var playParams: PlayParams? = null

    //电量广播
    private lateinit var batteryReceiver: BatteryBroadcastReceiver

    //锁屏广播
    private lateinit var screenLockReceiver: ScreenBroadcastReceiver

    //耳机广播
    private lateinit var headsetReceiver: HeadsetBroadcastReceiver

    private val videoController by lazy { VideoController(this) }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            PlayerViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_player

    override fun initStatusBar() {
        ImmersionBar.with(this)
            .fullScreen(true)
            .hideBar(BarHide.FLAG_HIDE_BAR)
            .init()
    }

    override fun initView() {
        ARouter.getInstance().inject(this)

        if (!checkPlayParams())
            return

        registerReceiver()

        initListener()

        initPlayerConfig()

        initPlayer(playParams!!)
    }

    override fun onPause() {
        dataBinding.danDanPlayer.pause()
        super.onPause()
    }

    override fun onDestroy() {
        beforePlayExit()
        dataBinding.danDanPlayer.release()
        unregisterReceiver()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (dataBinding.danDanPlayer.onBackPressed()) {
            return
        }
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return dataBinding.danDanPlayer.onKeyDown(keyCode, event) or super.onKeyDown(keyCode, event)
    }

    override fun onScreenLocked() {

    }

    override fun onBatteryChanged(status: Int, progress: Int) {
        videoController.setBatteryChanged(progress)
    }

    override fun onHeadsetRemoved() {
        dataBinding.danDanPlayer.pause()
    }

    private fun checkPlayParams(): Boolean {
        if (playParams == null || playParams?.videoPath.isNullOrEmpty()) {
            CommonDialog.Builder().run {
                content = "解析播放参数失败"
                addPositive("退出重试") {
                    it.dismiss()
                    finish()
                }
                build()
            }.show(this)
            return false
        }

        return true
    }

    private fun initListener() {

    }

    private fun initPlayer(params: PlayParams) {
        videoController.apply {
            setVideoTitle(params.videoTitle)
            setDanmuPath(params.danmuPath)
            setLastPosition(params.currentPosition)
            //播放错误
            observerPlayError {
                showPlayErrorDialog()
            }
            //退出播放
            observerPlayExit {
                finish()
            }
            //绑定资源
            observerBindSource { sourcePath, isSubtitle ->
                if (isSubtitle) {
                    params.subtitlePath = sourcePath
                } else {
                    params.danmuPath = sourcePath
                }
                viewModel.bindSource(sourcePath, params.videoPath, isSubtitle)
            }
            //发送弹幕
            observerSendDanmu {
                viewModel.sendDanmu(params, it)
            }
            //弹幕屏蔽
            observerDanmuBlock(
                cloudBlock = viewModel.cloudDanmuBlockLiveData,
                add = { keyword, isRegex -> viewModel.addDanmuBlock(keyword, isRegex) },
                remove = { id -> viewModel.removeDanmuBlock(id) },
                queryAll = { viewModel.localDanmuBlockLiveData }
            )
        }

        dataBinding.danDanPlayer.apply {
            setController(videoController)
            setProgressObserver { position, duration ->
                viewModel.addPlayHistory(params, position, duration)
            }
            val headerJson = params.getHttpHeaderJson()
            val header = JsonHelper.parseJsonMap(headerJson)
            setUrl(params.videoPath, header)
            start()
        }

        videoController.setSubtitlePath(params.subtitlePath)
    }

    private fun registerReceiver() {
        batteryReceiver = BatteryBroadcastReceiver(this)
        screenLockReceiver = ScreenBroadcastReceiver(this)
        headsetReceiver = HeadsetBroadcastReceiver(this)
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        registerReceiver(screenLockReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
        registerReceiver(headsetReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
    }

    private fun unregisterReceiver() {
        if (this::batteryReceiver.isInitialized) {
            unregisterReceiver(batteryReceiver)
        }
        if (this::screenLockReceiver.isInitialized) {
            unregisterReceiver(screenLockReceiver)
        }
        if (this::headsetReceiver.isInitialized) {
            unregisterReceiver(headsetReceiver)
        }
    }

    private fun initPlayerConfig() {
        //播放器类型
        PlayerInitializer.playerType = PlayerType.valueOf(PlayerConfig.getUsePlayerType())
        //IJKPlayer像素格式
        PlayerInitializer.Player.pixelFormat =
            PixelFormat.valueOf(PlayerConfig.getUsePixelFormat())
        //IJKPlayer硬解码
        PlayerInitializer.Player.isMediaCodeCEnabled = PlayerConfig.isUseMediaCodeC()
        //IJKPlayer H265硬解码
        PlayerInitializer.Player.isMediaCodeCH265Enabled = PlayerConfig.isUseMediaCodeCH265()
        //IJKPlayer OpenSlEs
        PlayerInitializer.Player.isOpenSLESEnabled = PlayerConfig.isUseOpenSlEs()
        //是否使用SurfaceView
        PlayerInitializer.surfaceType =
            if (PlayerConfig.isUseSurfaceView()) SurfaceType.VIEW_SURFACE else SurfaceType.VIEW_TEXTURE
        //视频速度
        PlayerInitializer.Player.videoSpeed = PlayerConfig.getNewVideoSpeed()

        //VLCPlayer像素格式
        PlayerInitializer.Player.vlcPixelFormat =
            VLCPixelFormat.valueOf(PlayerConfig.getUseVLCPixelFormat())
        PlayerInitializer.Player.vlcHWDecode =
            VLCHWDecode.valueOf(PlayerConfig.getUseVLCHWDecoder())

        //弹幕配置
        PlayerInitializer.Danmu.size = DanmuConfig.getDanmuSize()
        PlayerInitializer.Danmu.speed = DanmuConfig.getDanmuSpeed()
        PlayerInitializer.Danmu.alpha = DanmuConfig.getDanmuAlpha()
        PlayerInitializer.Danmu.stoke = DanmuConfig.getDanmuStoke()
        PlayerInitializer.Danmu.topDanmu = DanmuConfig.isShowTopDanmu()
        PlayerInitializer.Danmu.mobileDanmu = DanmuConfig.isShowMobileDanmu()
        PlayerInitializer.Danmu.bottomDanmu = DanmuConfig.isShowBottomDanmu()
        PlayerInitializer.Danmu.maxLine = DanmuConfig.getDanmuMaxLine()
        PlayerInitializer.Danmu.maxNum = DanmuConfig.getDanmuMaxCount()
        PlayerInitializer.Danmu.cloudBlock = DanmuConfig.isCloudDanmuBlock()
        PlayerInitializer.Danmu.updateInChoreographer = DanmuConfig.isDanmuUpdateInChoreographer()

        //字幕配置
        PlayerInitializer.Subtitle.textSize = (40f * SubtitleConfig.getTextSize() / 100f).toInt()
        PlayerInitializer.Subtitle.strokeWidth =
            (10f * SubtitleConfig.getStrokeWidth() / 100f).toInt()
        PlayerInitializer.Subtitle.textColor = SubtitleConfig.getTextColor()
        PlayerInitializer.Subtitle.strokeColor = SubtitleConfig.getStrokeColor()

        //设置本地视频文件的父文件夹，用于选取弹、字幕
        if (playParams!!.mediaType == MediaType.LOCAL_STORAGE) {
            File(playParams!!.videoPath).parentFile?.absolutePath?.let {
                PlayerInitializer.selectSourceDirectory = it
            }
        }
    }

    private fun showPlayErrorDialog() {
        val isMagnetSource = playParams?.mediaType == MediaType.MAGNET_LINK

        val tips = if (isMagnetSource) {
            val taskLog = PlayTaskBridge.getTaskLog(playParams?.getPlayTaskId())
            "播放失败，资源已失效或暂时无法访问，请尝试切换资源$taskLog"
        } else {
            "播放失败，请尝试更改播放器设置，或者切换其它播放内核"
        }

        val builder = AlertDialog.Builder(this@PlayerActivity)
            .setTitle("错误")
            .setCancelable(false)
            .setMessage(tips)
            .setNegativeButton("退出播放") { dialog, _ ->
                dialog.dismiss()
                this@PlayerActivity.finish()
            }

        if (isMagnetSource.not()) {
            builder.setPositiveButton("播放器设置") { dialog, _ ->
                dialog.dismiss()
                ARouter.getInstance()
                    .build(RouteTable.User.SettingPlayer)
                    .navigation()
                this@PlayerActivity.finish()
            }
        }

        builder.create().show()
    }

    private fun beforePlayExit() {
        if (playParams == null)
            return

        if (playParams!!.mediaType == MediaType.MAGNET_LINK) {
            PlayTaskBridge.sendTaskRemoveMsg(playParams!!.getPlayTaskId())
        }
    }
}