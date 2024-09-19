import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.crop_campanion_care_sample.ChatItem
import com.example.crop_campanion_care_sample.R

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {
    private val messages = mutableListOf<ChatItem>()

    // Add a new message to the list
    fun addMessage(message: ChatItem) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    // Update the last message in the list (used for typing effect)
    fun updateLastMessage(newContent: String) {
        if (messages.isNotEmpty()) {
            messages[messages.size - 1].message = newContent
            notifyItemChanged(messages.size - 1)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        // If it's a user message, show it on the left and hide the bot message view
        if (message.isUserMessage) {
            holder.userMessageView.text = message.message
            holder.userMessageView.visibility = View.VISIBLE
            holder.botMessageView.visibility = View.GONE
        }
        // If it's a bot message, show it on the right and hide the user message view
        else {
            holder.botMessageView.text = message.message
            holder.botMessageView.visibility = View.VISIBLE
            holder.userMessageView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = messages.size

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userMessageView: TextView = view.findViewById(R.id.userMessageView)
        val botMessageView: TextView = view.findViewById(R.id.botMessageView)
    }
}
