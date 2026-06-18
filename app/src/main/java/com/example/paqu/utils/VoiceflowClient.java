package com.example.paqu.utils;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class VoiceflowClient {

    // 🔥 TU API KEY (VF.DM)
    private static final String API_KEY =
            "VF.DM.6a3390842183b9ddf7d4a502.8ZkZCZntVyycKw8X";

    private static final OkHttpClient client = new OkHttpClient();

    public interface VoiceflowCallback {
        void onResponse(String message);
        void onError(String error);
    }

    public static void sendMessage(String userId, String message, VoiceflowCallback callback) {

        try {

            // ✅ ENDPOINT CORRECTO (Voiceflow DM)
            String url = "https://general-runtime.voiceflow.com/state/user/"
                    + userId
                    + "/interact";

            // ✅ BODY CORRECTO
            JSONObject action = new JSONObject();
            action.put("type", "text");
            action.put("payload", message);

            JSONObject body = new JSONObject();
            body.put("action", action);

            // ✅ REQUEST BODY CORRECTO
            RequestBody requestBody = RequestBody.create(
                    body.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            // ✅ REQUEST FINAL
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", API_KEY)
                    .addHeader("versionID", "main") // 🔥 IMPORTANTE: TU ENV REAL
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String responseBody = response.body().string();

                    if (!response.isSuccessful()) {
                        callback.onError("Error Voiceflow: " + response.code() + " " + responseBody);
                        return;
                    }
                    String reply = "";

                    try {
                        JSONObject json = new JSONObject(responseBody);

                        JSONArray trace = json.optJSONArray("trace");

                        if (trace != null) {

                            for (int i = 0; i < trace.length(); i++) {

                                JSONObject item = trace.getJSONObject(i);

                                JSONObject payload = item.optJSONObject("payload");

                                if (payload == null) continue;

                                // CASO 1: message
                                if (payload.has("message")) {
                                    reply = payload.getString("message");
                                }

                                // CASO 2: text (MUY IMPORTANTE)
                                else if (payload.has("text")) {
                                    reply = payload.getString("text");
                                }

                                // CASO 3: card / otros
                                else if (payload.has("content")) {
                                    reply = payload.getJSONObject("content").toString();
                                }
                            }
                        }

                        if (reply.isEmpty()) {
                            reply = "PAQU no respondió 😅";
                        }

                        callback.onResponse(reply);

                    } catch (Exception e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Exception: " + e.getMessage());
        }
    }
}