package com.socialauto.gemma

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private var isWaiting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle(R.string.app_name)

        recyclerView = findViewById(R.id.recyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        adapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = adapter

        sendButton.setOnClickListener { sendMessage() }
        messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }

        // Welcome message
        addAssistantMessage("Hi! I'm Gemma 4. Make sure Ollama is running on your PC, then set the server URL in Settings.")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sendMessage() {
        val text = messageInput.text.toString().trim()
        if (text.isEmpty() || isWaiting) return

        addUserMessage(text)
        messageInput.text.clear()
        isWaiting = true
        sendButton.isEnabled = false

        val loadingMsg = ChatMessage("assistant", "", isLoading = true)
        messages.add(loadingMsg)
        adapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val baseUrl = prefs.getString("ollama_url", "http://10.0.2.2:11434") ?: "http://10.0.2.2:11434"
        val model = prefs.getString("ollama_model", "gemma4") ?: "gemma4"
        val client = OllamaClient(baseUrl, model)

        client.chatStream(messages.filter { !it.isLoading }, object : OllamaClient.StreamListener {
            override fun onToken(token: String) {
                runOnUiThread {
                    val idx = messages.indexOfLast { it.role == "assistant" && it.isLoading }
                    if (idx >= 0) {
                        val old = messages[idx]
                        messages[idx] = old.copy(content = old.content + token, isLoading = false)
                        adapter.notifyItemChanged(idx)
                        scrollToBottom()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    isWaiting = false
                    sendButton.isEnabled = true
                    removeLoadingMessage()
                    Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                }
            }

            override fun onComplete() {
                runOnUiThread {
                    isWaiting = false
                    sendButton.isEnabled = true
                    val idx = messages.indexOfLast { it.role == "assistant" && it.isLoading }
                    if (idx >= 0) {
                        val old = messages[idx]
                        messages[idx] = old.copy(isLoading = false)
                        adapter.notifyItemChanged(idx)
                    }
                }
            }
        })
    }

    private fun addUserMessage(text: String) {
        messages.add(ChatMessage("user", text))
        adapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()
    }

    private fun addAssistantMessage(text: String) {
        messages.add(ChatMessage("assistant", text))
        adapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()
    }

    private fun removeLoadingMessage() {
        val idx = messages.indexOfLast { it.isLoading }
        if (idx >= 0) {
            messages.removeAt(idx)
            adapter.notifyItemRemoved(idx)
        }
    }

    private fun scrollToBottom() {
        recyclerView.post {
            if (messages.isNotEmpty()) {
                recyclerView.smoothScrollToPosition(messages.size - 1)
            }
        }
    }
}
