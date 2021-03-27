package com.android.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.chatapp.databinding.ActivityContactsBinding
import com.android.chatapp.databinding.ContactItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView

class ContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding

    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupAdapter = GroupAdapter()

        binding.contactList.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(this@ContactsActivity)
        }

        fetchContacts()
    }

    private fun fetchContacts() {
        Firebase.firestore.collection("users")
                .addSnapshotListener { value, error ->

                    error?.let {
                        Toast.makeText(this@ContactsActivity, it.localizedMessage, Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }

                    value?.let {
                        val contactList = it.toObjects(User::class.java)
                        groupAdapter.addAll(contactList.toUserItem())
                        groupAdapter.notifyDataSetChanged()
                    }
                }
    }

    inner class ContactItem(private val contact: User): Item<GroupieViewHolder>() {

        override fun getLayout(): Int = R.layout.contact_item

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val name = viewHolder.itemView.findViewById<AppCompatTextView>(R.id.contact_name)
            val photo = viewHolder.itemView.findViewById<CircleImageView>(R.id.contact_photo)

            name.text = contact.name
            Picasso.get()
                    .load(contact.profileUrl).into(photo)
        }
    }

    fun List<User>.toUserItem(): List<ContactItem> {
        return this.map {
            ContactItem(it)
        }
    }

}
