package com.example.exoplayertv.model

import com.google.gson.annotations.SerializedName

data class MediaJson(@SerializedName("MediaItems") val mediaItems: List<MediaItems>) {

    override fun toString(): String {
        return "MediaJson(mediaItems=$mediaItems)"
    }
}