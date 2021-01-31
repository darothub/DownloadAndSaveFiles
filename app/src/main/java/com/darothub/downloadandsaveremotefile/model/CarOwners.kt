package com.darothub.downloadandsaveremotefile.model

import java.io.Serializable

data class CarOwners(
    val id:String?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val country: String?,
    val carModel: String?,
    val carModelYear: String?,
    val carColor: String?,
    val gender: String?,
    val jobTitle: String?
):Serializable