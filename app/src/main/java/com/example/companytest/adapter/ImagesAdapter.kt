package com.example.companytest.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.example.companytest.R
import com.example.companytest.adapter.ImagesAdapter.ImagesViewHolder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.io.File

class ImagesAdapter(var context: Context?, var list: MutableList<File?>?) : RecyclerView.Adapter<ImagesViewHolder?>() {
    private var onItemDelete: OnItemDelete? = null
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ImagesViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_image, viewGroup, false)
        return ImagesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        Picasso.get().load(Uri.parse("file://" + list?.get(position)?.getAbsoluteFile())).resize(500, 500).networkPolicy(NetworkPolicy.NO_CACHE)
                .into(holder.imgFullImage)
        holder.floatDelete?.setOnClickListener(View.OnClickListener { onItemDelete?.onItemDelete(position, list?.get(position)) })
    }

    override fun getItemCount(): Int {
        return list!!.size
    }

    inner class ImagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var floatDelete: FloatingActionButton? = null
        var imgFullImage: AppCompatImageView? = null

        init {
            ButterKnife.bind(this, itemView)
            imgFullImage=itemView.findViewById(R.id.img_full_image)
            floatDelete=itemView.findViewById(R.id.float_delete)
        }
    }

    fun setonItemDelete(onItemDelete: OnItemDelete?) {
        this.onItemDelete = onItemDelete
    }

    interface OnItemDelete {
        open fun onItemDelete(position: Int, file: File?)
    }

}