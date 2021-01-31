package com.darothub.downloadandsaveremotefile.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.darothub.downloadandsaveremotefile.R
import com.darothub.downloadandsaveremotefile.databinding.CarsItemLayoutBinding
import com.darothub.downloadandsaveremotefile.model.CarOwners

@ModelView(
    autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT,
    defaultLayout = R.layout.cars_item_layout
)
class CarListView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    CardView(context, attr, defStyleAttr) {

    var binding: CarsItemLayoutBinding = CarsItemLayoutBinding.inflate(
        LayoutInflater.from(context),
        this, true
    )

    @ModelProp
    fun setData(carOwners: CarOwners) {
        binding.nameTv.text = "${carOwners.firstName}\n${carOwners.lastName}"
        binding.countryTv.text = "${carOwners.country}"
        binding.occupationTv.text = "${carOwners.jobTitle}"
        binding.emailTv.text = "${carOwners.email}"
        binding.genderTv.text = "${carOwners.gender}"
        binding.carTv.text = "${carOwners.carColor} ${carOwners.carModel} ${carOwners.carModelYear}"
    }

}