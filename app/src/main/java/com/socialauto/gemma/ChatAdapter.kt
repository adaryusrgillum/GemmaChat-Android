package com.socialauto.gemma

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_ASSISTANT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].role == "user") TYPE_USER else TYPE_ASSISTANT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            UserViewHolder(inflater.inflate(R.layout.item_message_user, parent, false))
        } else {
            AssistantViewHolder(inflater.inflate(R.layout.item_message_assistant, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is UserViewHolder -> holder.bind(msg)
            is AssistantViewHolder -> holder.bind(msg)
        }
    }

    override fun getItemCount(): Int = messages.size

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.messageText)
        fun bind(msg: ChatMessage) {
            textView.text = msg.content
        }
    }

    class AssistantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.messageText)
        private val loadingView: View = itemView.findViewById(R.id.loadingIndicator)
        fun bind(msg: ChatMessage) {
            textView.text = msg.content
            loadingView.visibility = if (msg.isLoading && msg.content.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
