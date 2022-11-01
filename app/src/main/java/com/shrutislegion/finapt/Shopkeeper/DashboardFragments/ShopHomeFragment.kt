package com.shrutislegion.finapt.Shopkeeper.DashboardFragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shrutislegion.finapt.R
import com.shrutislegion.finapt.Shopkeeper.ShopCreateBillActivity
import com.shrutislegion.finapt.databinding.FragmentCustomerProfileBinding
import com.shrutislegion.finapt.databinding.FragmentShopHomeBinding
import kotlinx.android.synthetic.main.fragment_shop_home.*
import kotlinx.android.synthetic.main.fragment_shop_home.view.*
import com.shrutislegion.finapt.Shopkeeper.ShopBillsHistoryActivity
import com.shrutislegion.finapt.Shopkeeper.ShopSendBillActivity
import com.shrutislegion.finapt.Shopkeeper.ShopUpdateInventoryActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ShopHomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShopHomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //val view: View = inflater.inflate(R.layout.fragment_shop_home, container, false)

        val binding: FragmentShopHomeBinding = FragmentShopHomeBinding.inflate(inflater, container, false)
        binding.createBillForOwnCardView.setOnClickListener {
            val intent = Intent(context, ShopCreateBillActivity::class.java)
            startActivity(intent)
        }
        binding.sendBillToCusCardView.setOnClickListener {
            startActivity(Intent(context, ShopSendBillActivity::class.java))
        }

        binding.updateInventoryCardView.setOnClickListener {
            startActivity(Intent(context, ShopUpdateInventoryActivity:: class.java))
        }

        binding.billHistoryCardView.setOnClickListener {
            startActivity(Intent(context, ShopBillsHistoryActivity::class.java))
        }
        return binding.root
    }
}