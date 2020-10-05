package com.example.exoplayertv.support

import android.content.Context
import com.example.exoplayertv.model.MediaJson
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

fun readAssets(context: Context): MediaJson {
    val jsonFileString = getJsonDataFromAsset(context, FILE_NAME)

    val gson = Gson()
    val listBlockType = object : TypeToken<MediaJson>() {}.type
    return gson.fromJson(jsonFileString, listBlockType)
}

fun getJsonDataFromAsset(context: Context, fileName: String): String? {
    val jsonString: String
    try {
        jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return null
    }
    return jsonString
}

