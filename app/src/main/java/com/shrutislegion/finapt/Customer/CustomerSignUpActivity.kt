// Customer Sign Up 1) via email and password 2) Google Sign In

package com.shrutislegion.finapt.Customer

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserInfo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.shrutislegion.finapt.AppCompat
import com.shrutislegion.finapt.Customer.Modules.CustomerInfo
import com.shrutislegion.finapt.R
import com.shrutislegion.finapt.Shopkeeper.Modules.ShopkeeperInfo
import com.shrutislegion.finapt.SignInActivity
import com.shrutislegion.finapt.databinding.ActivityCustomerSignUpBinding

@Suppress("DEPRECATION")
class CustomerSignUpActivity : AppCompat() {

    private lateinit var binding: ActivityCustomerSignUpBinding
    // Initialize Firebase Auth
    var auth: FirebaseAuth = Firebase.auth
    var database = Firebase.database

    // For Google SignIn
    private val RC_SIGN_IN = 100
    private val TAG = "CUSTOMER_SIGN_IN_ACTIVITY"
    private lateinit var googleSignInClient: GoogleSignInClient
    private val customerInfo: CustomerInfo = CustomerInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_sign_up)
        binding = ActivityCustomerSignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Adding Animations
        val topAnim = AnimationUtils.loadAnimation(this , R.anim.topanim)
        val rightAnim = AnimationUtils.loadAnimation(this, R.anim.rightanim)
        val leftAnim = AnimationUtils.loadAnimation(this, R.anim.leftanim)
        val bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottomanimation)

        // Setting Animations
        binding.profileAnim!!.animation = topAnim
        binding.helloText!!.animation = topAnim
        binding.missedText!!.animation = topAnim
        binding.emailLayout!!.animation = rightAnim
        binding.passwordLayout.animation = leftAnim
        binding.signUpButton!!.animation = bottomAnim
        binding.orLinearLayout!!.animation = bottomAnim
        binding.googleSignUp!!.animation = bottomAnim

        database = FirebaseDatabase.getInstance()
        val dialog = ProgressDialog(this)
        // Creating a dialog while the user is being added to the Firebase storage
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog.setTitle("Creating Account")
        dialog.setMessage("Please Wait")
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        binding.signUpButton.setOnClickListener {

            this.currentFocus?.let { view ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }

            if(binding.email.text == null || binding.email.text.toString().trim().isEmpty() || binding.email.text.toString().trim().length <= 10){
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            }
            else if(binding.password.text == null || binding.password.text.toString().trim().isEmpty()){
                Toast.makeText(this, "Please enter a valid password", Toast.LENGTH_SHORT).show()
            }
            else {

                dialog.show()
                auth.createUserWithEmailAndPassword(
                    binding.email.text!!.trim().toString(),
                    binding.password.text!!.trim().toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        dialog.dismiss()
                        val id = it.result.user?.uid
                        val user = Firebase.auth.currentUser
                        val customer: CustomerInfo = CustomerInfo(
                            binding.email.text.toString(),
                            binding.password.text.toString(),
                            user!!.uid
                        )

                        user!!.sendEmailVerification()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(TAG, "Email sent.")
                                    database.reference.child("Customers").child(id.toString())
                                        .setValue(customer)
                                    Toast.makeText(
                                        this,
                                        "Registered Successfully, Please Verify Your Account and login!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this, SignInActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        task.exception!!.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                    } else {
                        dialog.dismiss()
                        Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        //google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleSignUp.setOnClickListener {
            auth.signOut()
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = completedTask.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Google sign in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val googleCredential = GoogleAuthProvider.getCredential(idToken, null)
        when {
            idToken != null -> {

                binding.customerSignUpConstraintLayout!!.visibility = View.GONE
                binding.loadingAnimation.setAnimation(R.raw.loading)
                binding.loadingAnimation.visibility = View.VISIBLE
                binding.loadingAnimation.playAnimation()

                // Got an ID token from Google. Use it to authenticate
                // with Firebase.
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                auth.signInWithCredential(firebaseCredential)
                    .addOnSuccessListener { task ->
                        if(task.additionalUserInfo!!.isNewUser){
                            customerInfo.idToken = idToken
                            saveNewUserGoogleData(auth, binding)
                        }
                        else{
                            updateUI(auth, binding)
                        }
                    }
                    .addOnFailureListener {
                        Log.e("tag", "${it.message}")

                        binding.customerSignUpConstraintLayout!!.visibility = View.VISIBLE
                        binding.loadingAnimation.visibility = View.GONE
                        binding.loadingAnimation.cancelAnimation()

                        Toast.makeText(this, "Try Again", Toast.LENGTH_SHORT).show()
                    }
            }
            else -> {
                // Shouldn't happen.
                Log.d(TAG, "No ID token!")
                binding.customerSignUpConstraintLayout!!.visibility = View.VISIBLE
                binding.loadingAnimation.visibility = View.GONE
                binding.loadingAnimation.cancelAnimation()
                Toast.makeText(this, "Try Again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveNewUserGoogleData(auth: FirebaseAuth, binding: ActivityCustomerSignUpBinding) {

        customerInfo.id = auth.currentUser!!.uid
        customerInfo.mail = auth.currentUser!!.email
        customerInfo.profilePic = auth.currentUser!!.photoUrl.toString()

        FirebaseDatabase.getInstance().reference.child("Customers").child(customerInfo.id.toString())
            .setValue(customerInfo)

        FirebaseDatabase.getInstance().reference.child("GoogleUsers").child(customerInfo.id.toString()).setValue(customerInfo)
            .addOnSuccessListener {

                Firebase.auth.signOut()
                googleSignInClient.signOut()
                    .addOnCompleteListener {
                        binding.loadingAnimation.visibility = View.GONE
                        binding.loadingAnimation.cancelAnimation()

                        Toast.makeText(this@CustomerSignUpActivity, "Registration Successful. Please Login!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@CustomerSignUpActivity, SignInActivity::class.java))
                        finish()
                    }

            }
            .addOnFailureListener {

                userSignOut()

                binding.customerSignUpConstraintLayout!!.visibility = View.VISIBLE
                binding.loadingAnimation.visibility = View.GONE
                binding.loadingAnimation.cancelAnimation()

                Toast.makeText(this@CustomerSignUpActivity, "Some error occurred. Please try again!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI(auth: FirebaseAuth, binding: ActivityCustomerSignUpBinding) {
        val user = auth.currentUser!!.uid

        FirebaseDatabase.getInstance().reference.child("GoogleUsers")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        if(snapshot.child(user).exists()){
                            // this means user has already registered previously with google
                            Firebase.auth.signOut()
                            googleSignInClient.signOut()
                                .addOnCompleteListener {
                                    binding.loadingAnimation.visibility = View.GONE
                                    binding.loadingAnimation.cancelAnimation()

                                    Toast.makeText(this@CustomerSignUpActivity, "User already registered by Google SignIn. Please Login!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@CustomerSignUpActivity, SignInActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener {
                                    binding.customerSignUpConstraintLayout!!.visibility = View.VISIBLE
                                    binding.loadingAnimation.visibility = View.GONE
                                    binding.loadingAnimation.cancelAnimation()
                                }
                        }
                        else{
                            checkAllUsersForUser(auth)
                        }
                    }
                    else{
                        checkAllUsersForUser(auth)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                    userSignOut()

                    binding.customerSignUpConstraintLayout!!.visibility = View.VISIBLE
                    binding.loadingAnimation.visibility = View.GONE
                    binding.loadingAnimation.cancelAnimation()

                    Toast.makeText(this@CustomerSignUpActivity, "Some error occurred. Please try again!", Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun checkAllUsersForUser(auth: FirebaseAuth) {

        val user = auth.currentUser!!.uid

        FirebaseDatabase.getInstance().reference.child("Customers")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        if(snapshot.child(user).exists()){
                            // this means user has already registered previously with custom email and password
                            unlinkGoogleProvider(auth.currentUser!!.providerData) // unlinks Google as a provider

                            Firebase.auth.signOut()

                            binding.loadingAnimation.visibility = View.GONE
                            binding.loadingAnimation.cancelAnimation()

                            Toast.makeText(this@CustomerSignUpActivity, "User is already registered. Please Login!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@CustomerSignUpActivity, SignInActivity::class.java))
                            finish()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                    unlinkGoogleProvider(auth.currentUser!!.providerData) // unlinks Google as a provider
                    userSignOut()

                    binding.customerSignUpConstraintLayout!!.visibility = View.VISIBLE
                    binding.loadingAnimation.visibility = View.GONE
                    binding.loadingAnimation.cancelAnimation()

                    Toast.makeText(this@CustomerSignUpActivity, "Some error occurred. Please try again!", Toast.LENGTH_SHORT).show()
                }

            })

        FirebaseDatabase.getInstance().reference.child("Shopkeepers")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        if(snapshot.child(user).exists()){
                            // this means user has already registered previously with custom email and password
                            unlinkGoogleProvider(auth.currentUser!!.providerData) // unlinks Google as a provider

                            Firebase.auth.signOut()
                            binding.loadingAnimation.visibility = View.GONE
                            binding.loadingAnimation.cancelAnimation()

                            Toast.makeText(this@CustomerSignUpActivity, "User is already registered. Please Login!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@CustomerSignUpActivity, SignInActivity::class.java))
                            finish()

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                    unlinkGoogleProvider(auth.currentUser!!.providerData)
                    userSignOut()

                    binding.customerSignUpConstraintLayout!!.visibility = View.VISIBLE
                    binding.loadingAnimation.visibility = View.GONE
                    binding.loadingAnimation.cancelAnimation()

                    Toast.makeText(this@CustomerSignUpActivity, "Some error occurred. Please try again!", Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun unlinkGoogleProvider(providerData: List<UserInfo>) {
        for(data in providerData){
            val providerId = data.providerId
            if(providerId == "google.com"){
                Firebase.auth.currentUser!!.unlink(providerId)
                    .addOnSuccessListener {
                        Log.e("tag", "Removed Google as Provider")
                    }
                    .addOnFailureListener {
                        Log.e("tag", "${it.message}")
                    }
            }
        }
    }

    private fun userSignOut() {
        Firebase.auth.signOut()
        googleSignInClient.signOut()
            .addOnCompleteListener {
                Log.e("SIGN_IN_ACTIVITY", "googleSignInClient logged out successfully..")
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        googleSignInClient.signOut()
    }
}