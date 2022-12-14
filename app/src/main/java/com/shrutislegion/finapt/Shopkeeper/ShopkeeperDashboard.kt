// User gets directed here after creating profile, it contains the Bottom Navigation

package com.shrutislegion.finapt.Shopkeeper

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.shrutislegion.finapt.AppCompat
import com.shrutislegion.finapt.Notifications.MyReceiver
import com.shrutislegion.finapt.R
import com.shrutislegion.finapt.RegistrationActivity
import com.shrutislegion.finapt.Shopkeeper.DashboardFragments.*
import com.shrutislegion.finapt.ShowAllUsersFragment
import com.shrutislegion.finapt.databinding.ActivityShopkeeperDashboardBinding
import java.util.*


@Suppress("DEPRECATION")
class ShopkeeperDashboard : AppCompat() {
    lateinit var auth: FirebaseAuth
    lateinit var bottomNav: ChipNavigationBar

    var registrationActivityObject: RegistrationActivity = RegistrationActivity()

    companion object {
        const val EXTRA_FRAGMENT = "name_extra"
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopkeeper_dashboard)
        auth = Firebase.auth

        val binding: ActivityShopkeeperDashboardBinding = ActivityShopkeeperDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = Intent("finish_activity")
        sendBroadcast(intent)

        bottomNav = findViewById(R.id.bottom_nav)

        binding.messageShopFragmentFAB!!.setOnClickListener {

            //  show all the users excluding the current signed in user
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.shop_fragment_container, ShowAllUsersFragment())
            transaction.addToBackStack(null)
            transaction.commit()

            binding.messageShopFragmentFAB.visibility = View.GONE
        }

        var frag = intent.getStringExtra(ShopkeeperDashboard.EXTRA_FRAGMENT)

        if(frag == "1") {
            bottomNav.setItemSelected(R.id.shopHome, true)
            supportFragmentManager.beginTransaction()
                .replace(R.id.shop_fragment_container, ShopHomeFragment(), "HOME_FRAGMENT").commitAllowingStateLoss()

            binding.messageShopFragmentFAB.visibility = View.GONE
            supportActionBar!!.title = "Home"
        }
        else if(frag == "2"){
            bottomNav.setItemSelected(R.id.shopPendingRequest,true)
            supportFragmentManager.beginTransaction().replace(R.id.shop_fragment_container, ShopPendingReqFragment(), "PENDING_FRAGMENT").commitAllowingStateLoss()

            binding.messageShopFragmentFAB.visibility = View.GONE
            supportActionBar!!.title = "Pending Requests"
        }
        else if(frag == "3"){
            bottomNav.setItemSelected(R.id.shopPastBills,true)
            supportFragmentManager.beginTransaction().replace(R.id.shop_fragment_container, ShopPastBillsFragment(), "BILLS_FRAGMENT").commitAllowingStateLoss()

            binding.messageShopFragmentFAB.visibility = View.GONE
            supportActionBar!!.title = "Bills"
        }
        else if(frag == "4"){
            bottomNav.setItemSelected(R.id.shopChat,true)
            supportFragmentManager.beginTransaction().replace(R.id.shop_fragment_container, ShopChatFragment(), "CHAT_FRAGMENT").commitAllowingStateLoss()

            binding.messageShopFragmentFAB.visibility = View.VISIBLE
            supportActionBar!!.title = "Chats"
        }
        else if(frag == "5"){
            bottomNav.setItemSelected(R.id.shopProfile,true)
            supportFragmentManager.beginTransaction().replace(R.id.shop_fragment_container, ShopProfileFragment(), "PROFILE_FRAGMENT").commitAllowingStateLoss()

            binding.messageShopFragmentFAB.visibility = View.GONE
            supportActionBar!!.title = "Profile"
        }
        // By default the home page should be selected on opening the app
        else if(savedInstanceState==null){
            bottomNav.setItemSelected(R.id.shopHome,true)
            supportFragmentManager.beginTransaction().replace(R.id.shop_fragment_container, ShopHomeFragment(), "HOME_FRAGMENT").commitAllowingStateLoss()

            binding.messageShopFragmentFAB.visibility = View.GONE
        }

        // Listener on the bottomNav, and selecting the fragment according to their ids
        bottomNav.setOnItemSelectedListener {
            var fragment: Fragment? = null
            when(it){
                R.id.shopHome ->{
                    supportFragmentManager.beginTransaction().replace(R.id.shop_fragment_container, ShopHomeFragment(), "HOME_FRAGMENT").commitAllowingStateLoss()
                    binding.messageShopFragmentFAB.visibility = View.GONE
                    supportActionBar!!.title = "Home"
                }
                R.id.shopPendingRequest -> {
                    supportFragmentManager.beginTransaction().replace(R.id.shop_fragment_container, ShopPendingReqFragment(), "PENDING_FRAGMENT").commitAllowingStateLoss()
                    binding.messageShopFragmentFAB.visibility = View.GONE
                    supportActionBar!!.title = "Pending Requests"
                }
                R.id.shopPastBills -> {
                    supportFragmentManager.beginTransaction().replace(R.id.shop_fragment_container, ShopPastBillsFragment(), "BILLS_FRAGMENT").commitAllowingStateLoss()
                    binding.messageShopFragmentFAB.visibility = View.GONE
                    supportActionBar!!.title = "Bills"
                }
                R.id.shopChat -> {
                    supportFragmentManager.beginTransaction().replace(R.id.shop_fragment_container, ShopChatFragment(), "CHAT_FRAGMENT").commitAllowingStateLoss()
                    binding.messageShopFragmentFAB.visibility = View.VISIBLE
                    supportActionBar!!.title = "Chats"
                }
                R.id.shopProfile -> {
                    supportFragmentManager.beginTransaction().replace(R.id.shop_fragment_container, ShopProfileFragment(), "PROFILE_FRAGMENT")
                        .commitAllowingStateLoss()
                    binding.messageShopFragmentFAB.visibility = View.GONE
                    supportActionBar!!.title = "Profile"
                }
            }
        }

        try {
            val i = Intent(applicationContext, MyReceiver::class.java) // intent to be launched

            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                0,  // id (optional)
                i,  // intent to launch
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Date(System.currentTimeMillis()).time, AlarmManager.INTERVAL_HALF_DAY, pendingIntent)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onBackPressed() {

        val myFragment: ShopHomeFragment? =
            supportFragmentManager.findFragmentByTag("HOME_FRAGMENT") as ShopHomeFragment?
        if (myFragment != null && myFragment.isVisible) {
            super.onBackPressed()
        }
        else{
            supportFragmentManager.beginTransaction().replace(R.id.shop_fragment_container, ShopHomeFragment(), "HOME_FRAGMENT").commitAllowingStateLoss()
            supportActionBar!!.title = "Home"
            bottomNav.setItemSelected(R.id.shopHome, true)
        }
    }

}