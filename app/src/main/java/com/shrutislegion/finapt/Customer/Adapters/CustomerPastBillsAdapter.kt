package com.shrutislegion.finapt.Customer.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.shrutislegion.finapt.CommonAdapters.ViewBillItemDetailsAdapter
import com.shrutislegion.finapt.Customer.Modules.CustomerInfo
import com.shrutislegion.finapt.Modules.BillInfo
import com.shrutislegion.finapt.Modules.ItemInfo
import com.shrutislegion.finapt.R
import com.shrutislegion.finapt.Shopkeeper.Adapters.ShopBillHistoryAdapter
import com.shrutislegion.finapt.Shopkeeper.Modules.ShopkeeperInfo
import kotlinx.android.synthetic.main.item_customer_past_bills.view.*
import kotlinx.android.synthetic.main.item_view_bill.view.*
import kotlinx.android.synthetic.main.item_view_bill.view.shopName
import kotlinx.android.synthetic.main.item_view_bill.view.totalAmount
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CustomerPastBillsAdapter (val options: ArrayList<BillInfo>)
    : RecyclerView.Adapter<CustomerPastBillsAdapter.myViewHolder>(){
    inner class myViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // creating viewHolder and getting all the required views by their Ids
        // shopName, category, totalAmount, isAccepted, phone, billID, shopkeeperUID, timeStampBillSend
        val shopName = itemView.findViewById<TextView>(R.id.shopName)
        val number = itemView.findViewById<TextView>(R.id.phoneNumber)
        val category = itemView.findViewById<TextView>(R.id.category)
        val totalAmount = itemView.findViewById<TextView>(R.id.totalAmount)
        //val status = itemView.findViewById<TextView>(R.id.status)
        val viewBill = itemView.findViewById<Button>(R.id.viewBill)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_customer_past_bills, parent, false)
        return myViewHolder(view)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val itemmodel = options[position]
        val auth = Firebase.auth
        var shopkeeperUid = itemmodel.shopkeeperUid
        var CustomerUid = ""
        val newRef = FirebaseDatabase.getInstance().reference.child("AllPhoneNumbers")
            .child(itemmodel.sentTo)
        newRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    CustomerUid = snapshot.getValue<String>() as String

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


            val ref = FirebaseDatabase.getInstance().reference.child("Shopkeeper").child(shopkeeperUid)
            .child(shopkeeperUid)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val shopkeeper = snapshot.getValue<ShopkeeperInfo>() as ShopkeeperInfo
                        holder.shopName.text = shopkeeper.shopName
                        holder.number.text = shopkeeper.phone.toString()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
//        if(itemmodel.pending == true) {
//            holder.status.text = holder.status.context.getString(R.string.pending)
//            holder.status.setTextColor(Color.RED)
//        }
//        else {
//            //Toast.makeText(holder.status.context, itemmodel.accepted.toString(), Toast.LENGTH_SHORT).show()
//            if (itemmodel.accepted == true) {
//                holder.status.text = holder.status.context.getString(R.string.accepted)
//                holder.status.setTextColor(Color.GREEN)
//            }
//            else {
//                holder.status.text = holder.status.context.getString(R.string.rejected)
//                holder.status.setTextColor(Color.RED)
//
//            }
//        }
        holder.category.text = itemmodel.category
        holder.totalAmount.text = itemmodel.totalAmount

        holder.viewBill.setOnClickListener {
            val adapter: ViewBillItemDetailsAdapter
            val dialogPlus = DialogPlus.newDialog ( holder.viewBill.context!! )
                .setContentHolder(ViewHolder(R.layout.item_view_bill))
                .setExpanded(true, 1500)
                .create()
            val newView = dialogPlus.holderView
            newView.invoiceNo.text = itemmodel.invoice
            FirebaseDatabase.getInstance().reference.child("Shopkeepers").child(itemmodel.shopkeeperUid.toString())
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val shopkeeper = snapshot.getValue<ShopkeeperInfo>()!!
                            //Toast.makeText(newView.context, shopkeeper.toString(), Toast.LENGTH_SHORT).show()
                            newView.shopName.text = shopkeeper.shopName
                            newView.shopkeeperAddress.text = shopkeeper.address
                            newView.shopkeeperPhoneNumber.text = shopkeeper.phone
                            newView.shopkeeperEmail.text = shopkeeper.mail
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            if (itemmodel.sentTo == auth.currentUser!!.uid) {
                FirebaseDatabase.getInstance().reference.child("Customers")
                    .child(CustomerUid.toString())
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val customer = snapshot.getValue<CustomerInfo>() as CustomerInfo
                                newView.custName.text = customer.name
                                newView.customerAddress.text = customer.address
                                newView.customerPhoneNumber.text = customer.phone
                                newView.customerEmail.text = customer.mail

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
            }
            val itemList: ArrayList<ItemInfo> = itemmodel.items as ArrayList<ItemInfo>
            adapter = ViewBillItemDetailsAdapter(itemList)
            adapter.notifyDataSetChanged()
            newView.totalAmount.text = itemmodel.totalAmount
            newView.date.text = SimpleDateFormat("dd/MM/yyyy").format(Date(itemmodel.date.toLong()))
            newView.itemDetailsView.layoutManager = LinearLayoutManager(newView.context)
            newView.itemDetailsView.adapter = adapter
            newView.status.isClickable = false
            if(itemmodel.pending == true) {
                newView.status.text = "Pending"
                newView.status.setBackgroundColor(Color.RED)
            }
            else {
                if (itemmodel.accepted == true) {
                    newView.status.text = "Accepted"
                    newView.status.setBackgroundColor(Color.GREEN)
                }
                else {
                    newView.status.text = "Rejected"
                    newView.status.setBackgroundColor(Color.BLACK)
                }
            }
            dialogPlus.show()
        }

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return options.size
    }

}