package io.github.gumil.testnavigator.common

import android.content.Intent
import android.os.Parcelable
import android.support.annotation.MenuRes
import android.view.Menu
import android.view.MenuItem
import com.zhuinden.simplestack.navigator.ViewChangeHandler

internal abstract class ViewKey : Parcelable {

    val layout by lazy {
        layout()
    }

    abstract protected fun layout(): ViewLayout

    abstract fun viewChangeHandler(): ViewChangeHandler

    /**
     * Lifecycle: When animation is started. By this time view is already attached.
     */
    open fun onChangeStarted() {}

    /**
     * Lifecycle: When animation is ended
     */
    open fun onChangeEnded() {}

    /**
     * Lifecycle: When view is removed
     */
    open fun onViewRemoved() {}

    open fun onBackPressed() = false

    /**
     * Lifecycle: Called from host activity, handling for activityResults
     */
    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {}

    override fun describeContents() = hashCode()

    /**
     * Lifecycle: Called when creating options menu
     * @return Menu Resource
     */
    @MenuRes
    open fun onCreateOptionsMenu(): Int = -1

    open fun onOptionsItemSelected(item: MenuItem) = false

    /**
     * @return flag to know if the Previous View should be removed from the container
     * Sometimes we need to make the previous view visible like we do with dialogs
     */
    open fun shouldPreviousViewBePersisted() = false

    /**
     * Lifecycle: Called when view was destroyed and readded immediately
     * Only called when shouldPreviousViewBePersisted() is set to true
     */
    open fun restoreState() {}
}