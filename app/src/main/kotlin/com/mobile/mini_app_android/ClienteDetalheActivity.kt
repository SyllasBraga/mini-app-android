package com.mobile.mini_app_android

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ClienteDetalheActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val codigo = intent.getIntExtra(MainActivity.EXTRA_CODIGO, 0)
        val nome = intent.getStringExtra(MainActivity.EXTRA_NOME).orEmpty()
        val cpf = intent.getStringExtra(MainActivity.EXTRA_CPF).orEmpty()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F6F8FB"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(14) + statusBarHeight(), dp(16), dp(14))
            setBackgroundColor(Color.parseColor("#12355B"))
        }

        val voltar = Button(this).apply {
            text = "Voltar"
            textSize = 12f
            minWidth = 0
            minimumWidth = 0
            minimumHeight = dp(40)
            setPadding(dp(12), 0, dp(12), 0)
            setOnClickListener { finish() }
        }

        val titulo = TextView(this).apply {
            text = "Detalhes"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            setPadding(dp(12), 0, 0, 0)
        }

        header.addView(
            voltar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(44)
            )
        )
        header.addView(
            titulo,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = criarFundoConteudo()
            setPadding(dp(18), dp(18), dp(18), dp(18))
        }

        content.addView(criarCampo("Codigo", codigo.toString()))
        content.addView(criarCampo("Nome", nome))
        content.addView(criarCampo("CPF", cpf))

        root.addView(
            header,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        root.addView(
            content,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(16), dp(16), dp(16), 0)
            }
        )

        setContentView(root)
    }

    private fun criarCampo(label: String, valor: String): TextView {
        return TextView(this).apply {
            text = "$label\n$valor"
            textSize = 17f
            setTextColor(Color.parseColor("#102A43"))
            setPadding(0, 0, 0, dp(18))
        }
    }

    private fun criarFundoConteudo(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = dp(8).toFloat()
            setStroke(dp(1), Color.parseColor("#D6DEE8"))
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun statusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }
}
