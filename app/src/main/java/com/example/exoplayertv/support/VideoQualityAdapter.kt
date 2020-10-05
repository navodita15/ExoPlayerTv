package com.example.exoplayertv.support

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.exoplayertv.R
import com.example.exoplayertv.interfaces.QualitySelection
import com.example.exoplayertv.model.VideoHeightWidth
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter

class VideoQualityAdapter(
    private val list: ArrayList<VideoHeightWidth>,
    private val parametersBuilder: DefaultTrackSelector.ParametersBuilder,
    private val overridePlayer: DefaultTrackSelector.SelectionOverride?,
    private var trackSelector: DefaultTrackSelector,
    private val qualitySelection: QualitySelection
) :
    RecyclerView.Adapter<VideoQualityAdapter.VideoViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.video_quality_item_layout, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val videoDetails = list[position]
        holder.videoItemTextView.text = videoDetails.quality
        holder.itemView.setOnClickListener {
            applySelection(videoDetails.height, videoDetails.width, videoDetails.quality)
            qualitySelection.selected(true)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoItemTextView: TextView = itemView.findViewById(R.id.item_video_quality_text_view)


    }

    private fun applySelection(width: Int, height: Int, option: String) {
        parametersBuilder.setRendererDisabled(0, false)

        if (overridePlayer != null) {
            if (option == "Auto") {
                DefaultTrackSelector(AdaptiveTrackSelection.Factory(DefaultBandwidthMeter()))
            } else {
                val parameters =
                    DefaultTrackSelector.ParametersBuilder().setMaxVideoSize(height, width).build()
                trackSelector.parameters = parameters
            }

        } else {
            trackSelector.clearSelectionOverrides(0)
        }

    }

}