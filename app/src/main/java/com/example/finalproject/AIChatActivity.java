package com.example.finalproject;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AIChatActivity extends AppCompatActivity {

    private EditText userInput;
    private LinearLayout chatContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aichat_acitivity);

        userInput = findViewById(R.id.userInput);
        chatContainer = findViewById(R.id.chatContainer);
        ImageButton sendBtn = findViewById(R.id.sendBtn);

        sendBtn.setOnClickListener(v -> {
            String message = userInput.getText().toString().trim();

            if (!message.isEmpty()) {
                addMessage("You: " + message);
                userInput.setText("");

                // TEMP response (replace with API later)
                addMessage("AI: Finding travel suggestions...");
            }
        });
    }

    private void addMessage(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(16);
        tv.setTextColor(getResources().getColor(R.color.text_primary));
        tv.setPadding(16, 12, 16, 12);

        chatContainer.addView(tv);
    }
}