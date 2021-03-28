package com.android.chatapp
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.chatapp.ContactsActivity.Companion.USER
import com.android.chatapp.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView

class ChatActivity : AppCompatActivity() {

    lateinit var binding: ActivityChatBinding
    private lateinit var userContact: User
    private lateinit var me: User
    private val gAdapter = GroupieAdapter()

    val auth by lazy {
        FirebaseAuth.getInstance().currentUser
    }

    val firestore by lazy {
        Firebase.firestore
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getParcelableExtra<User>(USER)?.let { userContact = it }

        supportActionBar?.title = userContact.name

        binding.msgList.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = gAdapter
            }

        binding.btnSend.setOnClickListener {
            val msg = binding.editMsg.text.toString().trim()
            binding.editMsg.text = null
            if(msg.isNotEmpty())
                sendMessage(msg)
            }

        firestore.collection("users").document(auth.uid).get()
            .addOnSuccessListener {
                me = it.toObject(User::class.java) as User
                fetchMessages()
            }
        }

    private fun sendMessage(msg: String) {
        val fromId = auth.uid
        val toId = userContact.uuid

        val timestamp = System.currentTimeMillis()

        val message =  Message(msg, fromId, toId, timestamp)

        val myMessageRef = firestore.collection("/conversations")
            .document(fromId).collection(toId).document()

        val contactMessageRef = firestore.collection("/conversations")
            .document(toId).collection(fromId).document()

        val myLastMessageRef = firestore.collection("/last-messages")
                .document(fromId).collection("contacts").document(toId)

        val contactLastMessageRef = firestore.collection("/last-messages")
                .document(toId).collection("contacts").document(fromId)

        val batch = firestore.batch()
        batch.set(myMessageRef, message)
        batch.set(contactMessageRef, message)
        batch.set(myLastMessageRef, Contact(toId, userContact.name, userContact.profileUrl, msg, timestamp))
        batch.set(contactLastMessageRef, Contact(fromId, me.name, me.profileUrl, msg, timestamp))
        batch.commit()
    }

    private fun fetchMessages() {
        val fromId = me.uuid
        val toId = userContact.uuid

        firestore.collection("/conversations")
            .document(fromId)
            .collection(toId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val documentChanges = value?.documentChanges

                if (documentChanges != null)
                    for (doc in documentChanges)
                        if (doc.type == DocumentChange.Type.ADDED) {
                            val msg = doc.document.toObject(Message::class.java)
                            gAdapter.add(MessageItem(msg))
                        }
            }
    }

    inner class MessageItem(val msg: Message): Item<GroupieViewHolder>() {

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.apply {
                val photo = findViewById<CircleImageView>(R.id.user_photo)
                val txt = findViewById<AppCompatTextView>(R.id.msg_content)
                val photourl = if(msg.fromId.equals(me.uuid)) me.profileUrl else userContact.profileUrl
                Picasso.get().load(photourl).into(photo)
                txt.text = msg.txt
            }
        }

        override fun getLayout(): Int {
            return if(msg.fromId.equals(me.uuid))
                        R.layout.msg_to_contact
                    else
                        R.layout.msg_from_contact
        }

    }
}