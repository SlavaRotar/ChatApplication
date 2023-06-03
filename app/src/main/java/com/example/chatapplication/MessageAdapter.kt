package com.example.chatapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class MessageAdapter(val context: Context, val messageList: ArrayList<Message>, val senderUid: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_RECEIVE = 1
    val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == 1) {
            // inflate receive
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.receive, parent, false)
            return ReceiveViewHolder(view)

        } else {
            //inflate sent
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.sent, parent, false)
            return SentViewHolder(view)

        }
    }

    override fun getItemViewType(position: Int): Int {

        val currentMessage = messageList[position]

        if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)) {
            return ITEM_SENT
        } else {
            return ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        if (holder.javaClass == SentViewHolder::class.java) {
            //for sent view holder
            val viewHolder = holder as SentViewHolder
            viewHolder.bind(currentMessage, context, senderUid)
        } else {
            //for receive view holder
            val viewHolder = holder as ReceiveViewHolder
            viewHolder.bind(currentMessage, context, senderUid)
        }
    }

    companion object {
        private fun generateAESKey(password: String): SecretKey {
            val salt = byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07)
            val iterations = 4000
            val keyLength = 256
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keySpec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
            val secretKey = secretKeyFactory.generateSecret(keySpec)
            return SecretKeySpec(secretKey.encoded, "AES")
        }
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage = itemView.findViewById<TextView>(R.id.txt_sent_message)

        fun bind(message: Message, context: Context, senderUid: String) {
            //val password = senderUid //secret password
            val decryptedMessage = decryptMessage(message.message!!, senderUid)
            sentMessage.text = decryptedMessage
        }

        private fun decryptMessage(message: String, password: String): String {
            val secretKey: SecretKey = generateAESKey(password)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decryptedBytes = cipher.doFinal(android.util.Base64.decode(message, android.util.Base64.DEFAULT))
            return String(decryptedBytes)
        }
    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage = itemView.findViewById<TextView>(R.id.txt_receive_message)

        fun bind(message: Message, context: Context, senderUid: String) {
            //val password = senderUid //secret password
            val decryptedMessage = decryptMessage(message.message!!, senderUid)
            receiveMessage.text = decryptedMessage
        }

        private fun decryptMessage(message: String, password: String): String {
            val secretKey: SecretKey = generateAESKey(password)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decryptedBytes = cipher.doFinal(android.util.Base64.decode(message, android.util.Base64.DEFAULT))
            return String(decryptedBytes)
        }
    }
}
