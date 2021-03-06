package io.github.gumil.testnavigator.common

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.zhuinden.simplestack.StateChange
import com.zhuinden.simplestack.StateChanger
import com.zhuinden.simplestack.navigator.DefaultStateChanger
import com.zhuinden.simplestack.navigator.Navigator
import com.zhuinden.simplestack.navigator.ViewChangeHandler
import com.zhuinden.simplestack.navigator.changehandlers.NoOpViewChangeHandler
import io.github.gumil.testnavigator.MainActivity

internal class ViewStateChanger(
        private val context: Context,
        private val container: ViewGroup,
        private var externalStateChanger: StateChanger = ViewStateChanger.NoOpStateChanger(),
        private var viewChangeCompletionListener: DefaultStateChanger.ViewChangeCompletionListener = ViewStateChanger.NoOpViewChangeCompletionListener()
) : StateChanger {

    private class NoOpStateChanger : StateChanger {
        override fun handleStateChange(stateChange: StateChange, completionCallback: StateChanger.Callback) {
            completionCallback.stateChangeComplete()
        }
    }

    private class NoOpViewChangeCompletionListener : DefaultStateChanger.ViewChangeCompletionListener {
        override fun handleViewChangeComplete(stateChange: StateChange,
                                              container: ViewGroup,
                                              previousView: View?,
                                              newView: View,
                                              completionCallback: DefaultStateChanger.ViewChangeCompletionListener.Callback) {
            completionCallback.viewChangeComplete()
        }
    }

    private fun finishStateChange(stateChange: StateChange,
                                  container: ViewGroup,
                                  previousView: View?,
                                  newView: View,
                                  completionCallback: StateChanger.Callback) {
        viewChangeCompletionListener.handleViewChangeComplete(stateChange, container, previousView,
                newView) { completionCallback.stateChangeComplete() }
    }

    override fun handleStateChange(stateChange: StateChange, completionCallback: StateChanger.Callback) {
        externalStateChanger.handleStateChange(stateChange, StateChanger.Callback {
            if (stateChange.topNewState<Any>() == stateChange.topPreviousState<Any>()) {
                completionCallback.stateChangeComplete()
                return@Callback
            }
            val previousKey = stateChange.topPreviousState<ViewKey>()
            val previousView = if (stateChange.direction == StateChange.BACKWARD)
                container.getChildAt(container.childCount - 1) else container.getChildAt(0)
            if (previousView != null && previousKey != null) {
                Navigator.persistViewToState(previousView)
            }
            val newKey = stateChange.topNewState<ViewKey>()
            val newContext = stateChange.createContext(context, newKey)
            val newView = if (previousKey?.shouldPreviousViewBePersisted() ?: false)
                container.getChildAt(0) else newKey.layout.inflate(newContext)

            if (newKey.shouldPreviousViewBePersisted() && stateChange.previousState.isEmpty()) {
                let { stateChange.newState }
                        .filterIsInstance<ViewKey>()
                        .addPreviousViewIfPossible(newContext)
            }

            Navigator.restoreViewFromState(newView)

            previousKey?.onViewRemoved()
            newKey.onChangeStarted()
            (context as? MainActivity)?.invalidateOptionsMenu()
            setAnimating(context, true)
            if (previousView == null) {
                container.addView(newView)
                finishStateChange(stateChange, container, previousView, newView, completionCallback)
                newKey.onChangeEnded()
                (context as? MainActivity)?.invalidateOptionsMenu()
                setAnimating(context, false)
            } else {
                val viewChangeHandler: ViewChangeHandler
                if (stateChange.direction == StateChange.FORWARD) {
                    viewChangeHandler = newKey.viewChangeHandler()
                } else if (previousKey != null && stateChange.direction == StateChange.BACKWARD) {
                    viewChangeHandler = previousKey.viewChangeHandler()
                } else {
                    viewChangeHandler = NoOpViewChangeHandler()
                }
                viewChangeHandler.performViewChange(container,
                        previousView,
                        newView,
                        stateChange.direction
                ) {
                    finishStateChange(stateChange, container, previousView, newView, completionCallback)
                    newKey.onChangeEnded()
                    (context as? MainActivity)?.invalidateOptionsMenu()
                    setAnimating(context, false)
                }
            }
        })
    }

    private fun List<ViewKey>.addPreviousViewIfPossible(newContext: Context) {
        toList().takeIf { it.size >= 2 }
                ?.getOrNull(size - 2)
                ?.apply {
                    container.addView(layout.inflate(newContext))
                    restoreState()
                }
    }

    private fun setAnimating(context: Context, isAnimating: Boolean) {
        (context as? MainActivity)?.isAnimating = isAnimating
    }
}