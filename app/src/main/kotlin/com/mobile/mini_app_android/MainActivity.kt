package com.mobile.mini_app_android

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: ClienteDbHelper
    private lateinit var listaContainer: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var btnSincronizar: Button
    private lateinit var btnExcluir: Button

    private val selecionados = mutableSetOf<Int>()
    private var clientes: List<Cliente> = emptyList()
    private var operacaoEmAndamento = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = ClienteDbHelper(this)
        montarTela()
    }

    override fun onResume() {
        super.onResume()
        if (!operacaoEmAndamento) {
            carregarClientesDoBanco()
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }

    private fun montarTela() {
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

        val titulo = TextView(this).apply {
            text = "Clientes"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        }

        btnSincronizar = criarBotaoHeader("Sincronizar").apply {
            setOnClickListener { sincronizarClientes() }
        }

        btnExcluir = criarBotaoHeader("Excluir").apply {
            setOnClickListener { excluirSelecionados() }
        }

        header.addView(
            titulo,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        header.addView(btnSincronizar, criarParamsBotaoHeader())
        header.addView(btnExcluir, criarParamsBotaoHeader())

        statusText = TextView(this).apply {
            visibility = View.GONE
            textSize = 15f
            setTextColor(Color.parseColor("#0F172A"))
            setBackgroundColor(Color.parseColor("#DDEBFF"))
            setPadding(dp(16), dp(10), dp(16), dp(10))
        }

        listaContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(6), 0, dp(18))
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(
                listaContainer,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        root.addView(
            header,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        root.addView(
            statusText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun carregarClientesDoBanco() {
        clientes = dbHelper.buscarTodos()
        selecionados.retainAll(clientes.map { it.codigo }.toSet())
        renderizarClientes()
        atualizarEstadoAcoes()
    }

    private fun renderizarClientes() {
        listaContainer.removeAllViews()

        if (clientes.isEmpty()) {
            listaContainer.addView(criarMensagemVazia())
            return
        }

        clientes.forEach { cliente ->
            listaContainer.addView(criarLinhaCliente(cliente), criarParamsLinha())
        }
    }

    private fun criarLinhaCliente(cliente: Cliente): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = criarFundoLinha()
            setPadding(dp(8), dp(10), dp(12), dp(10))
            isClickable = true
        }

        val checkBox = CheckBox(this).apply {
            isChecked = selecionados.contains(cliente.codigo)
            setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    selecionados.add(cliente.codigo)
                } else {
                    selecionados.remove(cliente.codigo)
                }
                atualizarEstadoAcoes()
            }
        }

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), 0, 0, 0)
            isClickable = true
            setOnClickListener { abrirDetalhes(cliente) }
        }

        val nome = TextView(this).apply {
            text = cliente.nome
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#102A43"))
        }

        val documento = TextView(this).apply {
            text = "Codigo: ${cliente.codigo}  CPF: ${cliente.cpf}"
            textSize = 14f
            setTextColor(Color.parseColor("#52606D"))
            setPadding(0, dp(3), 0, 0)
        }

        info.addView(nome)
        info.addView(documento)

        row.addView(
            checkBox,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        row.addView(
            info,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )
        row.setOnClickListener {
            checkBox.isChecked = !checkBox.isChecked
        }

        return row
    }

    private fun sincronizarClientes() {
        if (operacaoEmAndamento) return

        operacaoEmAndamento = true
        exibirStatus("Buscando dados...")
        Toast.makeText(this, "Buscando dados...", Toast.LENGTH_SHORT).show()
        atualizarEstadoAcoes()

        Thread {
            val inicio = SystemClock.elapsedRealtime()
            try {
                Log.d(TAG, "Iniciando sincronizacao com a API externa.")
                val clientesApi = ClienteApi.buscarClientes()
                dbHelper.salvarClientes(clientesApi)
                val clientesAtualizados = dbHelper.buscarTodos()

                esperarTempoMinimo(inicio)

                runOnUiThread {
                    clientes = clientesAtualizados
                    selecionados.clear()
                    renderizarClientes()
                    operacaoEmAndamento = false
                    exibirStatus("Sincronizacao concluida: ${clientesApi.size} cliente(s).")
                    Toast.makeText(
                        this,
                        "Sincronizacao concluida.",
                        Toast.LENGTH_SHORT
                    ).show()
                    atualizarEstadoAcoes()
                }
            } catch (erro: Exception) {
                Log.e(TAG, "Erro ao sincronizar clientes.", erro)
                esperarTempoMinimo(inicio)

                runOnUiThread {
                    operacaoEmAndamento = false
                    exibirStatus("Erro ao sincronizar: ${erro.message ?: "falha desconhecida"}")
                    Toast.makeText(
                        this,
                        "Erro ao sincronizar. Confira a conexao do emulador.",
                        Toast.LENGTH_LONG
                    ).show()
                    atualizarEstadoAcoes()
                }
            }
        }.start()
    }

    private fun excluirSelecionados() {
        val codigos = selecionados.toList()
        if (codigos.isEmpty()) {
            Toast.makeText(this, "Selecione um cliente para excluir.", Toast.LENGTH_SHORT).show()
            return
        }

        operacaoEmAndamento = true
        exibirStatus("Excluindo registro(s)...")
        atualizarEstadoAcoes()

        Thread {
            val inicio = SystemClock.elapsedRealtime()
            try {
                val removidos = dbHelper.excluirPorCodigos(codigos)
                val clientesAtualizados = dbHelper.buscarTodos()

                esperarTempoMinimo(inicio)

                runOnUiThread {
                    clientes = clientesAtualizados
                    selecionados.clear()
                    renderizarClientes()
                    operacaoEmAndamento = false
                    exibirStatus("$removidos registro(s) excluido(s).")
                    atualizarEstadoAcoes()
                }
            } catch (erro: Exception) {
                esperarTempoMinimo(inicio)

                runOnUiThread {
                    operacaoEmAndamento = false
                    exibirStatus("Erro ao excluir: ${erro.message ?: "falha desconhecida"}")
                    atualizarEstadoAcoes()
                }
            }
        }.start()
    }

    private fun abrirDetalhes(cliente: Cliente) {
        val intent = Intent(this, ClienteDetalheActivity::class.java).apply {
            putExtra(EXTRA_CODIGO, cliente.codigo)
            putExtra(EXTRA_NOME, cliente.nome)
            putExtra(EXTRA_CPF, cliente.cpf)
        }
        startActivity(intent)
    }

    private fun atualizarEstadoAcoes() {
        btnSincronizar.isEnabled = !operacaoEmAndamento
        btnExcluir.isEnabled = !operacaoEmAndamento && selecionados.isNotEmpty()
    }

    private fun exibirStatus(mensagem: String) {
        statusText.text = mensagem
        statusText.visibility = View.VISIBLE
    }

    private fun esperarTempoMinimo(inicio: Long) {
        val tempoRestante = MIN_STATUS_MILLIS - (SystemClock.elapsedRealtime() - inicio)
        if (tempoRestante > 0) {
            SystemClock.sleep(tempoRestante)
        }
    }

    private fun criarMensagemVazia(): TextView {
        return TextView(this).apply {
            text = "Nenhum cliente salvo no banco local."
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#52606D"))
            setPadding(dp(18), dp(44), dp(18), dp(18))
        }
    }

    private fun criarBotaoHeader(texto: String): Button {
        return Button(this).apply {
            text = texto
            textSize = 12f
            minWidth = 0
            minimumWidth = 0
            minimumHeight = dp(40)
            setPadding(dp(10), 0, dp(10), 0)
        }
    }

    private fun criarParamsBotaoHeader(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            dp(44)
        ).apply {
            marginStart = dp(8)
        }
    }

    private fun criarParamsLinha(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(dp(16), dp(10), dp(16), 0)
        }
    }

    private fun criarFundoLinha(): GradientDrawable {
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

    companion object {
        const val EXTRA_CODIGO = "extra_codigo"
        const val EXTRA_NOME = "extra_nome"
        const val EXTRA_CPF = "extra_cpf"
        private const val MIN_STATUS_MILLIS = 2_000L
        private const val TAG = "MiniClientes"
    }
}
