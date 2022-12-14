// Data Class for storing Customer Information

package com.shrutislegion.finapt.Customer.Modules

data class CustomerInfo (
    var mail: String? = null,
    var password: String = "",
    var id: String? = null,
    var name: String = "",
    var gender: String = "",
    var dob: String = "",
    var profilePic: String = "",
    var phone: String = "",
    var state: String = "",
    var city: String = "",
    var pincode: String = "",
    var address: String = "",
    var phoneVerified: Boolean = false,
    var emailVerified: Boolean = false,
    var idToken: String = ""
) : java.io.Serializable
