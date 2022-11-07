package com.shrutislegion.finapt.Shopkeeper.DashboardFragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.shrutislegion.finapt.Modules.LoggedInUserInfo
import com.shrutislegion.finapt.R
import com.shrutislegion.finapt.Shopkeeper.Adapters.ShopChatUserFragmentAdapter
import com.shrutislegion.finapt.ShowAllUsersFragment
import com.shrutislegion.finapt.databinding.FragmentShopChatBinding

@Suppress("DEPRECATION")
class ShopChatFragment : Fragment() {

    var storeUsers: ArrayList<LoggedInUserInfo> = ArrayList<LoggedInUserInfo>()
    lateinit var adapter: ShopChatUserFragmentAdapter
    lateinit var auth: FirebaseAuth

    // To override LinearLayoutManager by Wrapper, as it crashes the application sometimes
    inner class LinearLayoutManagerWrapper : LinearLayoutManager {
        constructor(context: Context?) : super(context) {}
        constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(
            context,
            orientation,
            reverseLayout
        ) {
        }

        constructor(
            context: Context?,
            attrs: AttributeSet?,
            defStyleAttr: Int,
            defStyleRes: Int
        ) : super(context, attrs, defStyleAttr, defStyleRes) {
        }

        override fun supportsPredictiveItemAnimations(): Boolean {
            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val binding: FragmentShopChatBinding = FragmentShopChatBinding.inflate(inflater, container, false)
        auth = Firebase.auth

        var linearLayoutManager = LinearLayoutManagerWrapper(context, LinearLayoutManager.VERTICAL, false)

        binding.shopChatFragmentRV!!.layoutManager = linearLayoutManager
        binding.shopChatFragmentRV.isNestedScrollingEnabled = false

        FirebaseDatabase.getInstance().reference
            .child("Chats").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(item in snapshot.children){
                            val userIds: List<String> = item.key!!.split(",")
                            if(auth.currentUser!!.uid == userIds[0]){
                                FirebaseDatabase.getInstance().reference.child("Logged In Users")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                for(dss in dataSnapshot.children) {
                                                    val info =
                                                        dss.getValue<LoggedInUserInfo>()
                                                    if (info!!.id == userIds[1]) {
                                                        storeUsers.add(info)
                                                    }
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Toast.makeText(context, getString(R.string.unable_to_fetch), Toast.LENGTH_SHORT).show()
                                            fragmentManager!!.beginTransaction().replace(R.id.shopChatFragmentFrameLayout, ShopHomeFragment()).commitAllowingStateLoss()
                                        }

                                    })
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, getString(R.string.unable_to_fetch), Toast.LENGTH_SHORT).show()
                    requireFragmentManager().beginTransaction().replace(R.id.shopChatFragmentFrameLayout, ShopHomeFragment()).commitAllowingStateLoss()
                }

            })

        Handler(Looper.getMainLooper()).postDelayed({

            if(storeUsers.isEmpty()){
                binding.noChatHistoryTextView!!.visibility = View.VISIBLE
                binding.progressBarShopChat!!.visibility = View.GONE
            }
            else {
                adapter = ShopChatUserFragmentAdapter(storeUsers, container!!.context)
                binding.shopChatFragmentRV.adapter = adapter

                adapter.notifyDataSetChanged()

                binding.progressBarShopChat!!.visibility = View.GONE
                binding.shopChatFragmentRV.visibility = View.VISIBLE
            }

        }, 2500)

        return binding.root
    }
}