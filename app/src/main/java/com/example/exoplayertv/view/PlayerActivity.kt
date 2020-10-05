package com.example.exoplayertv.view

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.VerticalGridView
import androidx.lifecycle.ViewModelProvider
import androidx.media.session.MediaButtonReceiver
import com.example.exoplayertv.R
import com.example.exoplayertv.interfaces.PlayerEventListener
import com.example.exoplayertv.interfaces.QualitySelection
import com.example.exoplayertv.model.VideoHeightWidth
import com.example.exoplayertv.support.MEDIA_TITLE
import com.example.exoplayertv.support.MEDIA_URL
import com.example.exoplayertv.support.VideoQualityAdapter
import com.example.exoplayertv.viewmodel.DataViewModel
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory

class PlayerActivity : FragmentActivity(), QualitySelection {

    companion object {
        const val VIDEO_POSITION = "PlayerActivity.POSITION"
        const val VIDEO_URL = MEDIA_URL
        const val VIDEO_TITLE = MEDIA_TITLE
    }

    private lateinit var playerView: PlayerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var buttonVideoQuality: ImageView
    private lateinit var buttonPIP: ImageView
    private lateinit var videoTitleTextView: TextView
    private lateinit var videoQualityLayout: LinearLayout
    private lateinit var playerControls: LinearLayout
    private lateinit var playerBottomBar: LinearLayout
    private lateinit var videoVerticalGridView: VerticalGridView
    private lateinit var exoPlayer: SimpleExoPlayer
    private var videoPosition: Long = 0L
    private lateinit var videoUrl: String
    private lateinit var dataViewModel: DataViewModel
    private var isInPIPMode: Boolean = false
    private var isPIPModeEnabled: Boolean = true
    private lateinit var trackSelector: DefaultTrackSelector
    private lateinit var videoTitleText: String
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        if (intent.extras == null || !intent.hasExtra(VIDEO_URL)) {
            Toast.makeText(applicationContext, "Unable to play video", Toast.LENGTH_SHORT).show()
            finish()
        }
        dataViewModel = ViewModelProvider(this).get(DataViewModel::class.java)
        videoUrl = intent.getStringExtra(VIDEO_URL)!!
        videoTitleText = intent.getStringExtra(VIDEO_TITLE)!!

        savedInstanceState?.let { videoPosition = savedInstanceState.getLong(VIDEO_POSITION) }
        initializeViews()

        initializePlayer()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(VIDEO_POSITION, exoPlayer.currentPosition)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        videoPosition = savedInstanceState.getLong(VIDEO_POSITION)
    }

    private fun initializeViews() {

        playerView = findViewById(R.id.player_view)
        loadingProgressBar = findViewById(R.id.loading_progress_bar)
        buttonVideoQuality = playerView.findViewById(R.id.btn_video_quality)
        buttonPIP = playerView.findViewById(R.id.lb_control_picture_in_picture)
        videoTitleTextView = playerView.findViewById(R.id.video_title_text_view)
        videoQualityLayout = playerView.findViewById(R.id.video_quality_layout)
        playerControls = playerView.findViewById(R.id.player_buttons)
        playerBottomBar = playerView.findViewById(R.id.player_bottom_controls)
        videoVerticalGridView = playerView.findViewById(R.id.video_vgv)
        imageView = playerView.findViewById(R.id.exo_pause)
        imageView.requestFocus()

        //full screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        videoTitleTextView.text = videoTitleText
    }

    private fun initializePlayer() {
        val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
        trackSelector =
            DefaultTrackSelector(AdaptiveTrackSelection.Factory(bandwidthMeter))
        exoPlayer = ExoPlayerFactory.newSimpleInstance(
            applicationContext,
            DefaultRenderersFactory(this),
            trackSelector,
        )
        val httpFactory: DefaultHttpDataSourceFactory =
            DefaultHttpDataSourceFactory("exoplayer_video")

        playerView.player = exoPlayer
        playerView.keepScreenOn = true
        dataViewModel.preparePlayer(videoUrl, httpFactory, exoPlayer, applicationContext)
        exoPlayer.playWhenReady = true
        exoPlayer.addListener(PlayerEventListener(applicationContext, loadingProgressBar))
        setMediaComponents()

        buttonVideoQuality.setOnClickListener {

            playerBottomBar.visibility = View.GONE
            playerControls.visibility = View.GONE
            videoTitleTextView.visibility = View.GONE
            videoQualityLayout.visibility = View.VISIBLE

            videoQualityOptionDialog(it)
        }

        buttonPIP.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
                && isPIPModeEnabled
            ) {
                enterPIPMode()
            }
        }

    }


    private fun setMediaComponents() {
        val mediaComponent = ComponentName(
            applicationContext,
            MediaButtonReceiver::class.java.name
        )

        val mediaSession = MediaSessionCompat(this, packageName, mediaComponent, null)

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        val mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(exoPlayer, null)
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.setClass(applicationContext, MediaButtonReceiver::class.java)
        val mbrIntent: PendingIntent =
            PendingIntent.getBroadcast(applicationContext, 0, mediaButtonIntent, 0)
        mediaSession.setMediaButtonReceiver(mbrIntent)
        mediaSession.isActive = true
    }


    override fun onPause() {
        videoPosition = exoPlayer.currentPosition
        if (isInPictureInPictureMode) {
            playerControls.visibility = View.GONE
            playerBottomBar.visibility = View.GONE
            videoTitleTextView.visibility = View.GONE
        } else {
            playerControls.visibility = View.VISIBLE
            playerBottomBar.visibility = View.VISIBLE
            videoTitleTextView.visibility = View.VISIBLE
        }
        super.onPause()
        exoPlayer.playWhenReady = false
        exoPlayer.playWhenReady
    }

    override fun onResume() {
        super.onResume()
        if (videoPosition > 0L && !isInPIPMode) {
            exoPlayer.seekTo(videoPosition)
        }
        //Makes sure that the media controls pop up on resuming and when going between PIP and non-PIP states.
        playerView.useController = true
        exoPlayer.playWhenReady = true
        exoPlayer.playWhenReady
    }

    override fun onStop() {
        super.onStop()
//        finishAndRemoveTask()
        playerView.player = null
        exoPlayer.release()
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (videoQualityLayout.visibility == View.VISIBLE) {
                playerBottomBar.visibility = View.GONE
                playerControls.visibility = View.GONE
                videoTitleTextView.visibility = View.GONE
                videoVerticalGridView.requestFocus();
            } else if (videoQualityLayout.visibility == View.GONE) {
                imageView.requestFocus()
            }
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (videoQualityLayout.visibility == View.VISIBLE) {
                videoQualityLayout.visibility = View.GONE
                playerBottomBar.visibility = View.VISIBLE
                playerControls.visibility = View.VISIBLE
                videoTitleTextView.visibility = View.VISIBLE
                imageView.requestFocus()

            } else {
                super.onBackPressed()
            }

        } else {
            Log.d("Player", "Do Nothing")
        }
        return false
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        if (newConfig != null) {
            videoPosition = exoPlayer.currentPosition
            isInPIPMode = !isInPictureInPictureMode
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }

    private fun enterPIPMode() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        ) {
            videoPosition = exoPlayer.currentPosition
            playerView.useController = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder()
                this.enterPictureInPictureMode(params.build())

            } else {
                this.enterPictureInPictureMode()
            }
            Handler(Looper.getMainLooper()).postDelayed({ checkPIPPermission() }, 30)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun checkPIPPermission() {
        isPIPModeEnabled = isInPictureInPictureMode
        if (!isInPictureInPictureMode) {
            onBackPressed()
        }
    }

    private fun videoQualityOptionDialog(view: View) {
        val trackInfo = trackSelector.currentMappedTrackInfo ?: return
        val parameters = trackSelector.parameters
        val trackGroups = trackInfo.getTrackGroups(0)
        val parametersBuilder = trackSelector.buildUponParameters()
        val overridePlayer = parameters.getSelectionOverride(0, trackGroups)

        val list = setVideoQualityPopUp(trackGroups)
        videoVerticalGridView.adapter =
            VideoQualityAdapter(
                list,
                parametersBuilder,
                overridePlayer,
                trackSelector,
                this
            )

        videoVerticalGridView.requestFocus()
    }

    private fun setVideoQualityPopUp(
        trackGroups: TrackGroupArray
    ): ArrayList<VideoHeightWidth> {
        val list = ArrayList<VideoHeightWidth>()
        list.add(VideoHeightWidth(0, 0, "Auto"))
        for (i in 0 until trackGroups[0].length) {
            val currentQuality = trackGroups[0].getFormat(i).height
            val selectedTrack = currentQuality.toString() + "p"
            list.add(
                VideoHeightWidth(
                    trackGroups[0].getFormat(i).width,
                    trackGroups[0].getFormat(i).height, selectedTrack
                )
            )

        }
        return list

    }

    override fun selected(selected: Boolean) {
        if (selected) {
            videoQualityLayout.visibility = View.GONE
            playerBottomBar.visibility = View.VISIBLE
            playerControls.visibility = View.VISIBLE
            videoTitleTextView.visibility = View.VISIBLE
            imageView.requestFocus()
        }
    }

}