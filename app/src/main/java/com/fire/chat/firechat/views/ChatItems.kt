package com.fire.chat.firechat.views

import android.graphics.Bitmap
import android.os.Environment
import android.view.View
import com.fire.chat.firechat.R
import com.fire.chat.firechat.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import java.io.File
import java.io.FileOutputStream


class ChatFromItem(val text: String,val file:String, val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        if(text != "") {
            viewHolder.itemView.textview_from_row.text = text
        }else{
            viewHolder.itemView.textview_from_row.visibility = View.GONE
            Picasso.get().load(file).resize(200,200)
                .centerCrop().into(viewHolder.itemView.chat_from_imageView)
        }

        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageview_chat_from_row
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, var file:String, val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        if(text.isNotEmpty()) {
            viewHolder.itemView.textview_to_row.text = text
        }else if(file.isNotEmpty()){
            viewHolder.itemView.textview_to_row.visibility = View.GONE
            Picasso.get().load(file).resize(200,200)
                .centerCrop().into(viewHolder.itemView.chat_to_imageView)
        }

        // load our user image into the star
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageview_chat_to_row
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}


