package com.ricardomodino.impostorgame.managers

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.ricardomodino.impostorgame.R

class GameDialog(private val context: Context) {

    private var icon: String = ""
    private var iconRes: Int? = null
    private var titleText: String = ""
    private var messageText: String = ""
    private var posText: String = "OK"
    private var posAction: () -> Unit = {}
    private var negText: String? = null
    private var negAction: (() -> Unit)? = null
    private var isCancelable: Boolean = false

    fun icon(emoji: String)      = apply { icon = emoji; iconRes = null }
    fun iconRes(drawableRes: Int) = apply { iconRes = drawableRes; icon = "" }
    fun title(t: String)         = apply { titleText = t }
    fun message(m: String)       = apply { messageText = m }
    fun cancelable(c: Boolean)   = apply { isCancelable = c }

    fun positiveButton(text: String, action: () -> Unit = {}) =
        apply { posText = text; posAction = action }

    fun negativeButton(text: String, action: (() -> Unit)? = null) =
        apply { negText = text; negAction = action }

    fun show() {
        val d = Dialog(context)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        d.setContentView(R.layout.dialog_game)
        d.setCancelable(isCancelable)
        d.window?.apply {
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(android.R.color.transparent)
        }

        d.findViewById<View>(R.id.dlgRoot)
            .setBackgroundResource(ThemeManager.getBgMain(context))

        val emojiIcon = d.findViewById<TextView>(R.id.dlgIcon)
        val imageIcon = d.findViewById<ImageView>(R.id.dlgIconImage)
        val accentColor = ThemeManager.getAccentColor(context)

        if (iconRes != null) {
            imageIcon.visibility = View.VISIBLE
            imageIcon.setImageResource(iconRes!!)
            imageIcon.setColorFilter(accentColor)
            emojiIcon.visibility = View.GONE
        } else {
            emojiIcon.visibility = View.VISIBLE
            emojiIcon.text = icon
            imageIcon.visibility = View.GONE
        }

        d.findViewById<TextView>(R.id.dlgTitle).apply {
            text = titleText
            setShadowLayer(20f, 0f, 0f, accentColor)
        }

        d.findViewById<TextView>(R.id.dlgMessage).text = messageText

        d.findViewById<Button>(R.id.dlgBtnPositive).apply {
            text = posText
            setBackgroundResource(ThemeManager.getBtnNeon(context))
            setOnClickListener { d.dismiss(); posAction() }
        }

        negText?.let { label ->
            d.findViewById<Button>(R.id.dlgBtnNegative).apply {
                visibility = View.VISIBLE
                text = label
                setTextColor(Color.parseColor("#BFC4FF"))
                setOnClickListener { d.dismiss(); negAction?.invoke() }
            }
        }

        d.show()
    }
}
