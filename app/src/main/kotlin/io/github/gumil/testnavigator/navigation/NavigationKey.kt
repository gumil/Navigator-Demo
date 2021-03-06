package io.github.gumil.testnavigator.navigation

import android.os.Parcel
import android.os.Parcelable
import com.zhuinden.simplestack.navigator.ViewChangeHandler
import com.zhuinden.simplestack.navigator.changehandlers.SegueViewChangeHandler
import io.github.gumil.testnavigator.changehandler.FadeChangeHandler
import io.github.gumil.testnavigator.common.ViewKey

internal data class NavigationKey(
        private val index: Int = 0,
        private val displayUpMode: DisplayUpMode = DisplayUpMode.SHOW_FOR_CHILDREN_ONLY
) : ViewKey() {

    var changeHandler: ViewChangeHandler = SegueViewChangeHandler()

    override fun layout() = NavigationLayout(index, displayUpMode)

    override fun viewChangeHandler() = changeHandler

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            DisplayUpMode.values()[parcel.readInt()]
    )

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<NavigationKey> = object : Parcelable.Creator<NavigationKey> {
            override fun createFromParcel(`in`: Parcel) = NavigationKey(`in`)

            override fun newArray(size: Int) = arrayOfNulls<NavigationKey>(size)
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(index)
        dest.writeInt(displayUpMode.ordinal)
    }

    override fun onChangeStarted() {
        (layout as? NavigationLayout)?.setButtonsEnabled(false)
    }

    override fun onChangeEnded() {
        (layout as? NavigationLayout)?.setButtonsEnabled(true)
    }
}