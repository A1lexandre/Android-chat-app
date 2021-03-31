package com.android.chatapp
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.chatapp.ContactsActivity.Companion.USER
import com.android.chatapp.databinding.ActivityMessagesBinding
import com.android.chatapp.databinding.LastMessageItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.viewbinding.BindableItem

class MessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesBinding
    private val gAdapter = GroupieAdapter()

    val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        application.registerActivityLifecycleCallbacks(ChatApplication())

        verifyAuthentication()
    }

    private fun verifyAuthentication() {
        if(auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            return
        }
        setupRecyclerView()
        fetchLastMessages()
        updateToken()
    }

    private fun setupRecyclerView() {
        gAdapter.setOnItemClickListener { item, view ->
            Firebase.firestore.collection("users")
                    .document((item as ContactItem).contact.uuid)
                    .get().addOnCompleteListener {
                        if(it.isSuccessful) {
                            val intent = Intent(this@MessagesActivity, ChatActivity::class.java)
                            intent.putExtra(USER, it.result?.toObject(User::class.java))
                            startActivity(intent)
                        }
                    }
        }
        binding.lastMessageList.apply {
            layoutManager = LinearLayoutManager(this@MessagesActivity)
            adapter = gAdapter
        }
    }

    private fun fetchLastMessages() {
        Firebase.firestore.collection("/last-messages")
                .document(auth.uid as String).collection("contacts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->
                    if(error != null) {
                        Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    val documentChanges = value?.documentChanges

                    if(documentChanges != null)
                        for(doc in documentChanges)
                            if(doc.type == DocumentChange.Type.ADDED){
                                val contact = doc.document.toObject(Contact::class.java)
                                gAdapter.add(ContactItem(contact))
                            }
                }
    }

    private fun updateToken() {
        if(auth.currentUser != null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if(it.isSuccessful) {
                    val uid = auth.uid
                    Firebase.firestore.collection("users")
                            .document(uid as String).update("token", it.result)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.contacts -> {
                startActivity(Intent(this, ContactsActivity::class.java))
            }
            R.id.logout -> {
                Firebase.firestore.collection("users")
                        .document(auth.uid as String).update("online", false)
                        .addOnCompleteListener {
                            if(it.isSuccessful) {
                                auth.signOut()
                                verifyAuthentication()
                            }
                        }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class ContactItem(val contact: Contact): BindableItem<LastMessageItemBinding>() {

        override fun getLayout(): Int = R.layout.last_message_item

        override fun bind(viewBinding: LastMessageItemBinding, position: Int) {
            viewBinding.apply {
                contactName.text = contact.userName
            lastMessage.text = contact.lastMessage
            Picasso.get()
                    .load(contact.userPhotoUrl)
                    .into(contactPhoto)
            }
        }

        override fun initializeViewBinding(view: View): LastMessageItemBinding {
            return LastMessageItemBinding.bind(view)
        }


    }
}