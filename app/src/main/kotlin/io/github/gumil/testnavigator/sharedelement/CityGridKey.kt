package io.github.gumil.testnavigator.sharedelement

import android.os.Parcel
import android.os.Parcelable
import io.github.gumil.testnavigator.changehandler.ArcFadeMoveChangeHandler
import io.github.gumil.testnavigator.common.ViewKey
import io.github.gumil.testnavigator.home.HomeDemoModel

internal class CityGridKey(
        private val homeDemoModel: HomeDemoModel,
        private val position: Int
) : ViewKey() {
    override fun layout() = CityGridLayout(homeDemoModel.title, homeDemoModel.color, position)

    override fun viewChangeHandler() = ArcFadeMoveChangeHandler()

    constructor(parcel: Parcel) : this(
            HomeDemoModel.values()[parcel.readInt()],
            parcel.readInt()
    )

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CityGridKey> = object : Parcelable.Creator<CityGridKey> {
            override fun createFromParcel(`in`: Parcel) = CityGridKey(`in`)

            override fun newArray(size: Int) = arrayOfNulls<CityGridKey>(size)
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(homeDemoModel.ordinal)
        dest.writeInt(position)
    }

}