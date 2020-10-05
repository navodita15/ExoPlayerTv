package com.example.exoplayertv.model

import com.google.gson.annotations.SerializedName

open class MediaItems(
    @SerializedName("Title") val title: String,
    @SerializedName("Image") val imageUrl: String,
    @SerializedName("Url") val videoUrl: String
){

    override fun toString(): String {
        return "MediaItems(title='$title', imageUrl='$imageUrl', videoUrl='$videoUrl')"
    }
}