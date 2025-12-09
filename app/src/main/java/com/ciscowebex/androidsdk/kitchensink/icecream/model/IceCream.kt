package com.ciscowebex.androidsdk.kitchensink.icecream.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IceCream(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val imageResName: String,
    val colorHex: String
) : Parcelable {
    
    companion object {
        fun getSampleFlavors(): List<IceCream> = listOf(
            IceCream(
                id = 1,
                name = "Vanilla Dream",
                description = "Classic Madagascar vanilla bean ice cream with a silky smooth texture",
                price = 4.99,
                imageResName = "ic_vanilla",
                colorHex = "#FFF8DC"
            ),
            IceCream(
                id = 2,
                name = "Chocolate Fudge",
                description = "Rich Belgian dark chocolate with swirls of fudge",
                price = 5.49,
                imageResName = "ic_chocolate",
                colorHex = "#5D4037"
            ),
            IceCream(
                id = 3,
                name = "Strawberry Bliss",
                description = "Fresh strawberry ice cream made with real berries",
                price = 5.29,
                imageResName = "ic_strawberry",
                colorHex = "#FF6B9D"
            ),
            IceCream(
                id = 4,
                name = "Mint Chip",
                description = "Cool peppermint ice cream loaded with chocolate chips",
                price = 5.49,
                imageResName = "ic_mint",
                colorHex = "#98FF98"
            ),
            IceCream(
                id = 5,
                name = "Caramel Swirl",
                description = "Buttery caramel ice cream with golden caramel ribbons",
                price = 5.79,
                imageResName = "ic_caramel",
                colorHex = "#FFD700"
            ),
            IceCream(
                id = 6,
                name = "Double Chocolate Chip",
                description = "Creamy milk chocolate loaded with chocolate chips and cocoa swirls",
                price = 5.99,
                imageResName = "ic_double_chocolate",
                colorHex = "#8B4513"
            )
        )
    }
}

