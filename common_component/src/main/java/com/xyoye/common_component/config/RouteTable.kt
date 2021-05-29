package com.xyoye.common_component.config

/**
 * Created by xyoye on 2020/9/21.
 *
 * 路由表
 */

object RouteTable {

    object Anime {
        const val Search = "/anime/search"
        const val HomeFragment = "/anime/home_fragment"
        const val AnimeDetail = "/anime/anime_detail"
        const val AnimeSeason = "/anime/anime_season"
        const val AnimeFollow = "/anime/follow"
        const val AnimeTag = "/anime/tag"
        const val AnimeHistory = "/anime/history"
    }

    object Local {
        const val MediaFragment = "/local/media_fragment"
        const val BindDanmu = "/local/bind_danmu"
        const val BindSubtitle = "/local/bind_subtitle"
        const val LocalMediaStorage = "/local/local_media_storage"
        const val PlayHistory = "/local/play_history"
        const val BiliBiliDanmu = "/local/bilibili_danmu"
        const val ShooterSubtitle = "/local/shooter_subtitle"
    }

    object User {
        const val PersonalFragment = "/user/personal_fragment"
        const val UserLogin = "/user/login"
        const val UserRegister = "/user/register"
        const val UserForgot = "/user/forgot"
        const val UserInfo = "/user/info"
        const val SettingPlayer = "/user/setting_player"
        const val SettingApp = "/user/setting_app"
        const val WebView = "/user/web_view"
        const val ScanManager = "/user/scan_manager"
        const val CacheManager = "/user/cache_manager"
        const val CommonManager = "/user/common_manager"
        const val Feedback = "/user/feedback"
        const val AboutUs = "/user/about_us"
        const val License = "/user/license"
        const val SwitchTheme = "/user/switch_theme"
    }

    object Player {
        const val Player = "/player/player_interceptor"
        const val PlayerCenter = "/player/player"
    }

    object Download {
        const val DownloadSelection = "/download/selection"
        const val DownloadList = "/download/list"
        const val DownloadDetail = "/download/detail"
        const val PlaySelection = "/play/selection"
    }

    object Stream {
        const val WebDavLogin = "/stream/web_dav_login"
        const val WebDavFile = "/stream/web_dav_file"
        const val WebDavImage = "/stream/web_dav_image"
        const val FTPLogin = "/stream/ftp_login"
        const val FTPFile = "/stream/ftp_file"
        const val SmbLogin = "/stream/smb_login"
        const val SmbFile = "/stream/smb_file"
        const val RemoteLogin = "/stream/remote_login"
        const val RemoteScan = "/stream/remote_scan"
        const val RemoteFile = "/stream/remote_file"
        const val RemoteControl = "/stream/remote_control"
    }
}