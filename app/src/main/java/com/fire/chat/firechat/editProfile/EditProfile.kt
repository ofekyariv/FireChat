package com.fire.chat.firechat.editProfile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.fire.chat.firechat.R
import com.fire.chat.firechat.messages.LatestMessagesActivity
import com.fire.chat.firechat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        getData()
       update_profile.setOnClickListener {
           updateProfile()
           Toast.makeText(this,"Profile Updated",Toast.LENGTH_LONG).show()
           startActivity(Intent(this,LatestMessagesActivity::class.java))
           finish()

       }
    }
    private fun updateProfile(){
        val user = FirebaseAuth.getInstance().currentUser
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(username_tv.text.toString().isNotEmpty() && email_tv.text.toString().isNotEmpty()
                    && password_tv.text.toString().isNotEmpty()) {
                    ref.child("username").setValue(username_tv.text.toString())
                    user?.updateEmail(email_tv.text.toString())
                    user?.updatePassword(password_tv.text.toString())
                }
                else{
                    Toast.makeText(this@EditProfile,"Please Fill all fields",Toast.LENGTH_LONG).show()
                }
            }

        })
    }
    private fun getData(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)
                Picasso.get().load(user?.profileImageUrl).resize(200,200)
                    .centerCrop()
                    .into(profile_image)
                username_heading.text = user?.username
                username_tv.setText(user?.username)
                email_tv.setText(FirebaseAuth.getInstance().currentUser?.email)
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}