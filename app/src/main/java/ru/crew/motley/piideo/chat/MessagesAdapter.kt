package ru.crew.motley.piideo.chat

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import ru.crew.motley.piideo.R
import ru.crew.motley.piideo.fcm.FcmMessage
import ru.crew.motley.piideo.fcm.MessagingService

/**
 * Created by vas on 1/20/18.
 */
class MessagesAdapter(
        private val ownerUid: String,
        private val loaderCallback: PiideoLoaderCallback,
//        private val viewCalback: PiideoViewCallback,
        options: FirebaseRecyclerOptions<FcmMessage>) :
        FirebaseRecyclerAdapter<FcmMessage, MessagesAdapter.MessageViewHolder>(options) {

    companion object {
        private const val VIEW_TYPE_SENT = 0
        private const val VIEW_TYPE_RECEIVED = 1
        private const val VIEW_TYPE_PIIDEO_SENT = 2
        private const val VIEW_TYPE_PIIDEO_RECEIVE = 3
        const val VIEW_TYPE_HELLO = 4
    }

    interface PiideoLoaderCallback {
        fun send(message: FcmMessage, piideoImage: ImageView, progressBar: View)
        fun receive(message: FcmMessage, piideoImage: ImageView, progressBar: View)
    }

    interface PiideoViewCallback {
        fun onClick(piideoFileName: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val itemView = when (viewType) {
            VIEW_TYPE_SENT -> {
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_message_sent, parent, false)
            }
            VIEW_TYPE_RECEIVED -> {
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_message_received, parent, false)
            }
            VIEW_TYPE_PIIDEO_SENT -> {
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_chat, parent, false)
            }
            VIEW_TYPE_PIIDEO_RECEIVE -> {
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_chat, parent, false)
            }
            VIEW_TYPE_HELLO -> {
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_message_received, parent, false)
            }
            else -> throw RuntimeException("Message view type is unsupported")
        }
        return MessageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: FcmMessage) {
        holder.bind(model)
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        if (position == itemCount - 1 && message.content == "Hello_stub") {
            return VIEW_TYPE_HELLO
        }
        return if (message.type == MessagingService.PDO) {
            if (message.from.equals(ownerUid))
                VIEW_TYPE_PIIDEO_SENT
            else
                VIEW_TYPE_PIIDEO_RECEIVE
        } else {
            if (message.from.equals(ownerUid))
                VIEW_TYPE_SENT
            else
                VIEW_TYPE_RECEIVED
        }
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val messageBody: TextView by lazy {
            itemView.findViewById(R.id.messageBody) as TextView
        }

        private val piideoImage: ImageView by lazy {
            itemView.findViewById(R.id.player_toggler) as ImageView
        }

        private val progressBar: ProgressBar by lazy {
            itemView.findViewById(R.id.piideo_progress) as ProgressBar
        }

        fun bind(message: FcmMessage) {
            val viewType = this@MessagesAdapter.getItemViewType(layoutPosition)
            when (viewType) {
                VIEW_TYPE_PIIDEO_SENT -> {
                    progressBar.visibility = View.VISIBLE
                    loaderCallback.send(message, piideoImage, progressBar)
                }
                VIEW_TYPE_PIIDEO_RECEIVE -> {
                    progressBar.visibility = View.VISIBLE
                    loaderCallback.receive(message, piideoImage, progressBar)
                }
                VIEW_TYPE_SENT -> messageBody.text = message.content
                VIEW_TYPE_RECEIVED -> messageBody.text = message.content
                VIEW_TYPE_HELLO -> messageBody.setText(R.string.chat_message_stub_text)
            }
            //todo("check message type and in case it's piideo do a request to the loader, perhaps by loaderCallback")
        }
    }
}