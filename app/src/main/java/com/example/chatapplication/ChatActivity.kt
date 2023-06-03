package com.example.chatapplication

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference

    //private logic
    var receiverRoom: String? = null
    var senderRoom: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val name = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().getReference()
        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid
        supportActionBar?.title = name
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sentButton)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList, senderUid = senderUid?: "")
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter


        //add data to recyclerView
        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()

                    for (postSnapshot in snapshot.children) {

                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)

                    }
                    messageAdapter.notifyDataSetChanged()

                }

                override fun onCancelled(error: DatabaseError) {


                }

            })


        sendButton.setOnClickListener {
            val message = messageBox.text.toString()
            val encryptedMessage = encryptMessage(message, senderUid!!) // Encrypt the message

            val messageObject = Message(encryptedMessage, senderUid)

            mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    mDbRef.child("chats").child(receiverRoom!!).child("messages").push()
                        .setValue(messageObject)
                }
            messageBox.setText("")
        }
    }
    // Function to encrypt the message using AES-256
    private fun encryptMessage(message: String, password: String): String {
        val secretKey: SecretKey = generateAESKey(password)
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(message.toByteArray())
        return android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT)
    }
    // Function to generate a valid AES key with a length of 256 bits
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