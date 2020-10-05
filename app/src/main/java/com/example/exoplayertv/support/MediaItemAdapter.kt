package com.example.exoplayertv.support

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.exoplayertv.view.PlayerActivity
import com.example.exoplayertv.R
import com.example.exoplayertv.model.MediaItems

class MediaItemAdapter(
    private val context: Context,
    private val mediaList: List<MediaItems>,
    private val glide: RequestManager
) :
    RecyclerView.Adapter<MediaItemAdapter.MediaViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.media_item_layout, parent, false)

        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media: MediaItems = mediaList[position]
        holder.itemView.requestFocus()
        holder.mediaText.text = media.title
        glide.load(media.imageUrl).into(holder.mediaImage)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra(MEDIA_URL, mediaList[holder.adapterPosition].videoUrl)
            intent.putExtra(MEDIA_TITLE, mediaList[holder.adapterPosition].title)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
        /*holder.itemView.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            if (b){
                view.animate().scaleX(1.10f).scaleY(1.10f).start();
            }
            else{
                view.animate().scaleX(1.0f).scaleY(1.0f).start();
            }
        }*/
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mediaText: TextView = itemView.findViewById(R.id.media_title_text_view)
        val mediaImage: ImageView = itemView.findViewById(R.id.media_image_view)
    }
}