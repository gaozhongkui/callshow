package com.epiphany.callshow.function.video

import android.content.Context
import android.os.Build
import com.google.android.exoplayer2.ExoPlayerLibraryInfo
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.ext.cronet.CronetDataSource
import com.google.android.exoplayer2.ext.cronet.CronetEngineWrapper
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.*
import java.io.File
import java.util.concurrent.Executors

/**
 * 视频播放的辅助类
 */
object VideoPlayHelper {
    private const val DOWNLOAD_CONTENT_DIRECTORY = "c_s_d_l"
    private val USER_AGENT = ("CallShow/"
            + ExoPlayerLibraryInfo.VERSION
            + " (Linux; Android "
            + Build.VERSION.RELEASE
            + ") "
            + ExoPlayerLibraryInfo.VERSION_SLASHY)
    private var mDataSourceFactory: DataSource.Factory? = null

    /**
     * 获取数据源的工厂实例
     */
    fun getDataSourceFactory(cxt: Context): DataSource.Factory {
        synchronized(VideoPlayHelper::class) {
            if (mDataSourceFactory == null) {
                val upstreamFactory = DefaultDataSourceFactory(
                    cxt,
                    getHttpDataSourceFactory(cxt)
                )

                mDataSourceFactory = buildReadOnlyCacheDataSource(
                    upstreamFactory, getDownloadCache(cxt)
                )
            }
            return mDataSourceFactory!!
        }
    }

    private fun getHttpDataSourceFactory(context: Context): HttpDataSource.Factory {
        val croNetEngineWrapper = CronetEngineWrapper(
            context,
            USER_AGENT,
            false
        )
        return CronetDataSource.Factory(croNetEngineWrapper, Executors.newSingleThreadExecutor())
    }

    private fun buildReadOnlyCacheDataSource(
        upstreamFactory: DataSource.Factory,
        cache: Cache
    ): CacheDataSource.Factory {
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(null)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    /**
     * 获取下载的缓存目录
     */
    private fun getDownloadCache(context: Context): Cache {
        val downloadContentDirectory =
            File(context.filesDir, DOWNLOAD_CONTENT_DIRECTORY)
        return SimpleCache(
            downloadContentDirectory, LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024),
            ExoDatabaseProvider(context)
        )
    }

}