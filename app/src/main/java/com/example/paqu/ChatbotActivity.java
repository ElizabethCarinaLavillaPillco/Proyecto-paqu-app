package com.example.paqu;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paqu.ChatAdapter;
import com.example.paqu.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import com.example.paqu.utils.VoiceflowClient;
public class ChatbotActivity extends AppCompatActivity {

    private String userId;
    private RecyclerView recyclerChat;
    private EditText etMensaje;
    private ImageButton btnEnviar;

    private List<ChatMessage> messageList;
    private ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);
        userId = String.valueOf(System.currentTimeMillis());

        recyclerChat = findViewById(R.id.recyclerChat);
        etMensaje = findViewById(R.id.etMensaje);
        btnEnviar = findViewById(R.id.btnEnviar);

        messageList = new ArrayList<>();

        adapter = new ChatAdapter(messageList);
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(adapter);

        // Mensaje inicial de PAQU
        addBotMessage("Hola 👋 soy PAQU, tu asistente de quechua. ¿En qué puedo ayudarte?");

        btnEnviar.setOnClickListener(v -> {

            String mensaje = etMensaje.getText().toString().trim();

            if (!mensaje.isEmpty()) {

                addUserMessage(mensaje);
                etMensaje.setText("");

                sendToVoiceflow(mensaje);
            }
        });
    }

    private void sendToVoiceflow(String message) {

        VoiceflowClient.sendMessage(userId, message, new VoiceflowClient.VoiceflowCallback() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(() -> addBotMessage(response));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> addBotMessage("Error: " + error));
            }
        });
    }

    private void addUserMessage(String msg) {
        messageList.add(new ChatMessage(msg, ChatMessage.TYPE_USER));
        adapter.notifyItemInserted(messageList.size() - 1);
        scroll();
    }

    private void addBotMessage(String msg) {
        messageList.add(new ChatMessage(msg, ChatMessage.TYPE_BOT));
        adapter.notifyItemInserted(messageList.size() - 1);
        scroll();
    }

    private void scroll() {
        recyclerChat.scrollToPosition(messageList.size() - 1);
    }

    private void botResponse(String userMsg) {

        String response;

        if (userMsg.toLowerCase().contains("hola")) {
            response = "Sulpayki 👋 (Hola en quechua)";
        } else if (userMsg.toLowerCase().contains("diccionario")) {
            response = "Puedes ir a Herramientas → Diccionario 📚";
        } else {
            response = "Estoy aprendiendo 🤖 pronto seré más inteligente con IA.";
        }

        addBotMessage(response);
    }
}