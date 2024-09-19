import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.crop_campanion_care_sample.ChatItem
import com.example.crop_campanion_care_sample.R

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val messages = mutableListOf<ChatItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatItem = messages[position]
        if (chatItem.isUserMessage) {
            holder.leftChatLayout.visibility = View.GONE
            holder.rightChatLayout.visibility = View.VISIBLE
            holder.rightChatTextView.text = chatItem.message
        } else {
            holder.rightChatLayout.visibility = View.GONE
            holder.leftChatLayout.visibility = View.VISIBLE
            holder.leftChatTextView.text = chatItem.message
        }
    }

    override fun getItemCount() = messages.size

    fun setMessages(newMessages: List<ChatItem>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    fun addMessage(chatItem: ChatItem) {
        messages.add(chatItem)
        notifyItemInserted(messages.size - 1)
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val leftChatLayout: LinearLayout = itemView.findViewById(R.id.left_chat)
        val rightChatLayout: LinearLayout = itemView.findViewById(R.id.right_chat)
        val leftChatTextView: TextView = itemView.findViewById(R.id.left_chat_text)
        val rightChatTextView: TextView = itemView.findViewById(R.id.right_chat_text)
    }
}
