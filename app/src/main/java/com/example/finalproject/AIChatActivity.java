package com.example.finalproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.Html;
import android.text.method.LinkMovementMethod;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class AIChatActivity extends AppCompatActivity {

    private EditText userInput;
    private LinearLayout chatContainer;
    private TextView typingBubble = null;

    private final String API_KEY = BuildConfig.OPENAI_API_KEY;

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
                addMessage("You: " + message, true);
                userInput.setText("");

                showTypingBubble();
                callAI(message);
            }
        });
    }

    private void addMessage(String text, boolean isUser) {
        TextView tv = new TextView(this);

        String formatted = isUser
                ? text.replace("\n", "<br>")
                : formatAIResponse(text);
        tv.setText(Html.fromHtml(formatted, Html.FROM_HTML_MODE_LEGACY));

        tv.setTextSize(16);
        tv.setPadding(20, 14, 20, 14);

        if (isUser) {
            tv.setBackgroundResource(R.drawable.user_bubble);
            tv.setTextColor(getResources().getColor(android.R.color.white));
            tv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        } else {
            tv.setBackgroundResource(R.drawable.ai_bubble);
            tv.setTextColor(getResources().getColor(android.R.color.white));
        }

        chatContainer.addView(tv);
        scrollToBottom();
    }

    private void callAI(String userMessage) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        try {
            JSONObject json = new JSONObject();
            json.put("model", "gpt-4.1-mini");

            JSONArray messages = new JSONArray();

            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content",
                    "You are a travel assistant. Format responses using markdown:\n" +
                            "- Use **bold** for headings\n" +
                            "- Use bullet points for lists\n" +
                            "- Keep spacing clean\n" +
                            "- Make it easy to read on mobile"
            );
            messages.put(systemMsg);

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.put(userMsg);

            json.put("messages", messages);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->{
                            removeTypingBubble();
                            addMessage("Error: " + e.getMessage(), false);
                });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    if (!response.isSuccessful()) {
                        runOnUiThread(() ->{
                                removeTypingBubble();
                                addMessage("HTTP Error: " + response.code(), false);
                        });
                        return;
                    }

                    if (response.body() == null) {
                        runOnUiThread(() -> {
                                removeTypingBubble();
                                addMessage("Empty response", false);
                    });
                        return;
                    }

                    String responseData = response.body().string();

                    Log.d("API_RESPONSE", responseData);

                    try {
                        JSONObject jsonObject = new JSONObject(responseData);

                        // Handle API error
                        if (jsonObject.has("error")) {
                            String errorMsg = jsonObject
                                    .getJSONObject("error")
                                    .getString("message");

                            runOnUiThread(() -> {
                                removeTypingBubble();
                                addMessage("API Error: " + errorMsg, false);
                            });
                            return;
                        }

                        JSONArray choices = jsonObject.optJSONArray("choices");

                        if (choices == null || choices.length() == 0) {
                            runOnUiThread(() -> {
                                removeTypingBubble();
                                addMessage("No response from AI", false);
                            });
                            return;
                        }

                        JSONObject messageObject = choices
                                .getJSONObject(0)
                                .optJSONObject("message");

                        if (messageObject == null) {
                            runOnUiThread(() ->{
                                    removeTypingBubble();
                                    addMessage("Invalid response format", false);
                        });
                            return;
                        }

                        String reply = messageObject.optString("content", "No reply");

                        runOnUiThread(() ->{
                                removeTypingBubble();
                                addMessage(reply, false);
                    });

                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            removeTypingBubble();
                            addMessage("Parsing Error: " + e.getMessage(), false);
                        });
                    }
                }
            });

        } catch (Exception e) {
            removeTypingBubble();
            addMessage("Error: " + e.getMessage(), false);
        }
    }

    private void showTypingBubble(){
        typingBubble = new TextView(this);

        typingBubble.setText("AI is typing...");
        typingBubble.setTextSize(16);
        typingBubble.setPadding(20, 14, 20, 14);
        typingBubble.setTextColor(getResources().getColor(android.R.color.white));
        typingBubble.setBackgroundResource(R.drawable.typing_bubble);

        chatContainer.addView(typingBubble);
        scrollToBottom();
    }

    private void removeTypingBubble() {
        if (typingBubble != null) {
            chatContainer.removeView(typingBubble);
            typingBubble = null;
        }
    }

    private void scrollToBottom() {
        chatContainer.post(() -> {
            if (chatContainer.getParent() instanceof android.widget.ScrollView) {
                android.widget.ScrollView scrollView = (android.widget.ScrollView) chatContainer.getParent();
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private String formatAIResponse(String text) {
        // Convert bold (**text** → <b>text</b>)
        text = text.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        // Convert italics
        text = text.replaceAll("\\*(.*?)\\*", "<i>$1</i>");
        // Convert bullet points (* item → • item)
        text = text.replaceAll("(?m)^\\* ", "• ");
        // Convert numbered lists (optional)
        text = text.replaceAll("(?m)^\\d+\\. ", "• ");
        // Convert line breaks
        text = text.replace("\n", "<br>");
        // Convert section spacing
        text = text.replaceAll("<br><br>", "<br><br><br>");
        return text;
    }
}