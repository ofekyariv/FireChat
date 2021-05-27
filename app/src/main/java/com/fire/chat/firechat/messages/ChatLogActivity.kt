package com.fire.chat.firechat.messages

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.fire.chat.firechat.R
import com.fire.chat.firechat.RegisterActivity
import com.fire.chat.firechat.models.ApiService
import com.fire.chat.firechat.models.ChatMessage
import com.fire.chat.firechat.models.Data
import com.fire.chat.firechat.models.User
import com.fire.chat.firechat.notifications.Client
import com.fire.chat.firechat.notifications.MyResponse
import com.fire.chat.firechat.notifications.Sender
import com.fire.chat.firechat.notifications.Token
import com.fire.chat.firechat.views.ChatFromItem
import com.fire.chat.firechat.views.ChatToItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create
import java.util.*

class ChatLogActivity : AppCompatActivity() {
    companion object {
        val TAG = "ChatLog"
    }
    val adapter = GroupAdapter<GroupieViewHolder>()
    private var imageUrl:String = ""
    var toUser: User? = null
    var notify = false
    var firebaseUser: FirebaseUser? = null
    var apiService: ApiService? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser?.username

//    setupDummyData()
        listenForMessages()

        send_button_chat_log.setOnClickListener {
            Log.d(TAG, "Attempt to send message....")
            performSendMessage()
        }
        send_media_chat_log.setOnClickListener {
            notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Select Image"),400)
        }

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(ApiService::class.java)

    }
    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text,chatMessage.file, currentUser))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text,chatMessage.file, toUser!!))
                    }
                }

                recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)

            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })

    }
    var selectedPhotoUri: Uri? = null
    private fun performSendMessage() {
        notify = true
        // how do we actually send a message to firebase...
        val text = edittext_chat_log.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        if (fromId == null) return

//    val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
       val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

       val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

       val chatMessage = ChatMessage(reference.key!!, text, fromId, toId,imageUrl, System.currentTimeMillis() / 1000)
       reference.setValue(chatMessage)
           .addOnSuccessListener {
               Log.d(TAG, "Saved our chat message: ${reference.key}")
               edittext_chat_log.text.clear()
               send_image.setImageResource(0)
               recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
               imageUrl = ""
           }

       toReference.setValue(chatMessage)

       val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
       latestMessageRef.setValue(chatMessage)

       val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
       latestMessageToRef.setValue(chatMessage)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        val referenceNoti = FirebaseDatabase.getInstance().reference
            .child("users").child(firebaseUser!!.uid)
        referenceNoti.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if(notify){
                    sendNotification(toId,user!!.username,text)
                }
                notify = false
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun sendNotification(toId: String, username: String, text: String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Tokens")
        val query = reference.orderByKey().equalTo(toId)
        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children){
                    val token: Token? = dataSnapshot.getValue(Token::class.java)
                    val data = Data(firebaseUser!!.uid,R.drawable.firechat,"$username : $text",
                    "New Message",toId)
                    val sender = Sender(data!!,token!!.getToken().toString())
                    apiService!!.sendNotification(sender)
                        .enqueue(object : Callback<MyResponse>{
                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            ) {
                                if(response.code() == 200){
                                    if(response.body()!!.success !== 1){
                                        Toast.makeText(this@ChatLogActivity,"Failed Nothing Happened",Toast.LENGTH_LONG).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {

                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 400 && resultCode == Activity.RESULT_OK && data!!.data != null){
            selectedPhotoUri = data.data
            val imageStream = contentResolver.openInputStream(selectedPhotoUri!!)
            val selectedImage = BitmapFactory.decodeStream(imageStream)
            val dialog = ProgressDialog(this@ChatLogActivity)
            dialog.setMessage("Please wait image uploading !!!")
            dialog.show()
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/chat images/$filename")
            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener { it ->
                    Log.d(RegisterActivity.TAG, "Successfully uploaded image: ${it.metadata?.path}")
                    ref.downloadUrl.addOnSuccessListener {
                        Log.d(RegisterActivity.TAG, "File Location: $it")
                        imageUrl = it.toString()
                        send_image.setImageBitmap(selectedImage)
                        dialog.dismiss()
                    }
                }
                .addOnFailureListener {
                    Log.d(RegisterActivity.TAG, "Failed to upload image to storage: ${it.message}")
                }
        }
    }
}