package com.example.metaappgallery.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import com.example.metaappgallery.R
import com.example.metaappgallery.utils.formatMessage
import com.example.metaappgallery.utils.isNetworkProblem

class StatusLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    class Status(val status: String, val any: Any? = null) {
        companion object {
            const val STATUS_EMPTY = "status_empty"
            const val STATUS_LOADING = "status_loading"
            const val STATUS_CONTENT = "status_content"
            const val STATUS_ERROR = "status_error"
        }
    }

    private var status: Status = Status(Status.STATUS_LOADING)
    private var statusView: View? = null

    var whenBuildChild: ((status: Status) -> View?)? = null
    var onRetry: (() -> Unit)? = null

    var emptyMessage: String? = null
        set(value) {
            field = value
            if (this.status.status == Status.STATUS_EMPTY) {
                buildChild()
            }
        }

    fun setStatus(status: Status) {
        if (status == this.status) return

        this.status = status
        buildChild()
    }

    private fun buildChild() {
        if (statusView != null) removeView(statusView)

        val visibility = if (status.status != Status.STATUS_CONTENT) View.GONE else View.VISIBLE
        for (child in children) {
            child.visibility = visibility
        }

        var view = whenBuildChild?.invoke(status)
        if (view == null) {
            view = buildDefaultChild()
        }

        if (view == null) return

        statusView = view
        addView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private fun buildDefaultChild(): View? {
        return when (status.status) {
            Status.STATUS_LOADING -> {
                val fl = FrameLayout(context)
                fl.addView(ProgressBar(context), LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))
                fl.setBackgroundResource(android.R.color.white)
                return fl
            }
            Status.STATUS_EMPTY -> {
                Builder(context)
                        .setMessage(emptyMessage ?: "暂时没有数据")
                        .build()
            }
            Status.STATUS_ERROR -> {
                val error = status.any
                val message = if (error is Throwable) error.formatMessage() else status.any?.toString()

                val builder = Builder(context)
                        .setMessage(message.toString())
                        .setPositiveButton("点击重新加载", OnClickListener {
                            onRetry?.invoke()
                        })
                if (error is Throwable && error.isNetworkProblem()) {
                    builder.setNegativeButton("去设置网络", OnClickListener {
                        context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                    })
                }
                builder.build()
            }
            else -> null
        }
    }

    class Builder(val context: Context) {
        private var icon: Drawable? = null
        private var message: String? = null
        private var positiveBtn: String? = null
        private var negativeBtn: String? = null
        private var onPositiveListener: OnClickListener? = null
        private var onNegativeListener: OnClickListener? = null

        fun setIcon(@DrawableRes icon: Int): Builder {
            return setIcon(ResourcesCompat.getDrawable(context.resources, icon, context.theme)!!)
        }

        fun setIcon(icon: Drawable): Builder {
            this.icon = icon
            return this
        }

        fun setMessage(@StringRes message: Int): Builder {
            return setMessage(context.getString(message))
        }

        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun setPositiveButton(@StringRes positiveBtn: Int, onPositiveListener: OnClickListener): Builder {
            return setPositiveButton(context.getString(positiveBtn), onPositiveListener)
        }

        fun setPositiveButton(positiveBtn: String, onPositiveListener: OnClickListener): Builder {
            this.positiveBtn = positiveBtn
            this.onPositiveListener = onPositiveListener
            return this
        }

        fun setNegativeButton(@StringRes negativeBtn: Int, onPositiveListener: OnClickListener): Builder {
            return setNegativeButton(context.getString(negativeBtn), onPositiveListener)
        }

        fun setNegativeButton(negativeBtn: String, onNegativeListener: OnClickListener): Builder {
            this.negativeBtn = negativeBtn
            this.onNegativeListener = onNegativeListener
            return this
        }

        fun build(): View {
            val ll = View.inflate(context, R.layout.view_status_layout, null)
            val ivIcon: ImageView = ll.findViewById(R.id.iv_icon)
            val tvMsg: TextView = ll.findViewById(R.id.tv_msg)
            val tvNegativeBtn: TextView = ll.findViewById(R.id.tv_negative_btn)
            val tvPositiveBtn: TextView = ll.findViewById(R.id.tv_positive_btn)

            ivIcon.setImageDrawable(icon)
            tvMsg.text = message
            if (negativeBtn.isNullOrEmpty()) {
                tvNegativeBtn.visibility = View.GONE
            } else {
                tvNegativeBtn.visibility = View.VISIBLE
                tvNegativeBtn.text = negativeBtn
                tvNegativeBtn.setOnClickListener {
                    onNegativeListener?.onClick(tvNegativeBtn)
                }
            }
            if (positiveBtn.isNullOrEmpty()) {
                tvPositiveBtn.visibility = View.GONE
            } else {
                tvPositiveBtn.visibility = View.VISIBLE
                tvPositiveBtn.text = positiveBtn
                tvPositiveBtn.setOnClickListener {
                    onPositiveListener?.onClick(tvNegativeBtn)
                }
            }

            return ll
        }
    }
}