package com.epiphany.callshow.dialog

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.epiphany.callshow.R

abstract class BaseDialogFragment : DialogFragment() {

    companion object {
        const val BASE_DIALOG_STATE = "base_dialog_state"
    }

    private var isShowing: Boolean = false

    private lateinit var dialogConfig: DialogConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.base_dialog_style)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return createView(inflater)
    }

    abstract fun createView(inflater: LayoutInflater): View

    fun getDialogConfig(): DialogConfig = DialogConfig()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isShowing = savedInstanceState?.getBoolean(BASE_DIALOG_STATE) ?: false

        dialogConfig = getDialogConfig()
        dialog?.window?.let {
            it.decorView!!.setPadding(0, 0, 0, 0)

            var params = it.attributes
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.horizontalMargin = 0f
            if (dialogConfig.dimAmount >= 0.0f) {
                params.dimAmount = dialogConfig.dimAmount
            }
            it.attributes = params
            it.setGravity(dialogConfig.gravity)
        }

        isCancelable = dialogConfig.isCancelable
    }

    fun show(manager: FragmentManager) {
        show(manager, javaClass.simpleName)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.isDestroyed) {
            return
        }
        if (isShowing()) {
            return
        }
        if (isResumed) {
            super.show(manager, tag)
        } else {
            manager.beginTransaction().add(this, tag).commitAllowingStateLoss()
        }
    }

    fun isShowing(): Boolean = isShowing

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(BASE_DIALOG_STATE, isShowing)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isShowing = false
    }
}

data class DialogConfig(var isCancelable: Boolean = false, var gravity: Int = Gravity.CENTER, var dimAmount: Float = -1.0f)