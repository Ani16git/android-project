package com.example.instagramclone

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.set
import com.example.instagramclone.Models.User
import com.example.instagramclone.databinding.ActivitySignUpBinding
import com.example.instagramclone.utils.USER_NODE
import com.example.instagramclone.utils.USER_PROFILE_FOLDER
import com.example.instagramclone.utils.uploadImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class SignUpActivity : AppCompatActivity() {
    val binding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }
    lateinit var user: User
    private val launcher= registerForActivityResult(ActivityResultContracts.GetContent()){
        uri->
        uri?.let{
            uploadImage(uri, USER_PROFILE_FOLDER){
                if (it!=null){
                    user.image=it
                    binding.profileImage.setImageURI(uri)
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val text = "<font color=#FF000000>Already have an Account</font> <font color=#1E88E5>Login ?</font>"
        binding.login.setText(Html.fromHtml(text))
        user = User()
        if (intent.hasExtra("MODE")){
            if (intent.getIntExtra("MODE",-1)==1){

                binding.signUpBtn.text="Update Profile"
                Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid).get().addOnSuccessListener {

                     user=it.toObject<User>()!!
                    if (!user.image.isNullOrEmpty()){
                        Picasso.get().load(user.image).into(binding.profileImage)
                    }
                        //binding.name.editableText?.clear()
                       // binding.email.editableText?.clear()
                        //binding.password.editableText?.clear()
                }
            }
        }
        binding.signUpBtn.setOnClickListener {
            if (intent.hasExtra("MODE")) {
                if (intent.getIntExtra("MODE", -1) == 1) {
                    Firebase.firestore.collection(USER_NODE)
                        .document(Firebase.auth.currentUser!!.uid).set(user)
                        .addOnSuccessListener {
                            startActivity(
                                Intent(
                                    this@SignUpActivity,
                                    HomeActivity::class.java
                                )
                            )
                            finish()
                        }
                }
            } else {
                if (binding.name.editableText?.toString().equals("") or
                    binding.email.editableText?.toString().equals("") or
                    binding.password.editableText?.toString().equals("")
                ) {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Please fill the all Information",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                        binding.email.editableText.toString(),
                        binding.password.editableText.toString()
                    ).addOnCompleteListener { result ->

                        if (result.isSuccessful) {
                            user.name = binding.name.editableText?.toString()
                            user.password = binding.password.editableText?.toString()
                            user.email = binding.email.editableText?.toString()
                            Firebase.firestore.collection(USER_NODE)
                                .document(Firebase.auth.currentUser!!.uid).set(user)
                                .addOnSuccessListener {
                                    startActivity(
                                        Intent(
                                            this@SignUpActivity,
                                            HomeActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            Toast.makeText(
                                this@SignUpActivity,
                                "Account created",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@SignUpActivity,
                                result.exception?.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
        binding.addImage.setOnClickListener {
            launcher.launch("image/*")
        }
        binding.login.setOnClickListener{
            startActivity(Intent(this@SignUpActivity,LoginActivity::class.java))
            finish()
        }
    }
}