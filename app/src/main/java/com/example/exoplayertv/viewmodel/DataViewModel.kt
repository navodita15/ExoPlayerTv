package com.example.exoplayertv.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.exoplayertv.model.MediaJson
import com.example.exoplayertv.support.readAssets
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class DataViewModel(application: Application) : AndroidViewModel(application) {
    private var list: MutableLiveData<MediaJson> = MutableLiveData()

    init {
        fetchData(application.applicationContext)

    }

    private fun fetchData(applicationContext: Context) {
        val subscribe = Single.fromCallable { readAssets(applicationContext) }
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe { t ->
                list.value = t
            }
    }


    fun getMediaListData(): LiveData<MediaJson> {
        return list

    }

    fun preparePlayer(
        url: String,
        dataSourceFactory: DefaultHttpDataSourceFactory,
        player: SimpleExoPlayer,
        context: Context
    ) {
        val videoSubscribe = Single.fromCallable { mediaSourceForVideo(url, dataSourceFactory) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                player.prepare(it, true, false)
            }, {
                Toast.makeText(context, "Cant play this video", Toast.LENGTH_SHORT).show()
            })
    }

    private fun mediaSourceForVideo(
        url: String,
        dataSourceFactory: DefaultHttpDataSourceFactory
    ): MediaSource? {
        return when {
            Util.inferContentType(Uri.parse(url)) == C.TYPE_HLS -> {
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url))

            }
            Util.inferContentType(Uri.parse(url)) == C.TYPE_OTHER -> {
                ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(url))
            }
            else -> {
                null
            }
        }
    }
}
