package com.example.exoplayertv.view

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.VerticalGridView
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.exoplayertv.R
import com.example.exoplayertv.support.MediaItemAdapter
import com.example.exoplayertv.viewmodel.DataViewModel

class MainActivity : FragmentActivity() {

    private lateinit var dataViewModel: DataViewModel
    private lateinit var mediaRecyclerView: VerticalGridView
    private lateinit var mediaAdapter: MediaItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dataViewModel = ViewModelProvider(this).get(DataViewModel::class.java)
        mediaRecyclerView = findViewById(R.id.media_recycler_view)


        dataViewModel.getMediaListData().observe(this, {
            mediaAdapter =
                MediaItemAdapter(applicationContext, it.mediaItems, Glide.with(applicationContext))
            mediaRecyclerView.adapter = mediaAdapter
            mediaRecyclerView.requestFocus()
        })
    }
}