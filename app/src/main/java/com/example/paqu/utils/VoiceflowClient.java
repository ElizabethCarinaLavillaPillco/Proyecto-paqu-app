package com.example.paqu.utils;
import android.util.Log;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class VoiceflowClient {

    // 🔥 TU API KEY (VF.DM)
    private static final String API_KEY =
            "VF.DM.6a341f40c655089184db502a.1nImRIsTNxWFXOYg";

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

                    String responseBody = response.body() != null
                            ? response.body().string()
                            : "";

                    Log.d("VOICEFLOW", "====================");
                    Log.d("VOICEFLOW", "URL: " + url);
                    Log.d("VOICEFLOW", "REQUEST: " + body.toString());
                    Log.d("VOICEFLOW", "HTTP CODE: " + response.code());
                    Log.d("VOICEFLOW", "RESPONSE: " + responseBody);
                    Log.d("VOICEFLOW", "====================");

                    if (!response.isSuccessful()) {
                        callback.onError(
                                "Error Voiceflow: "
                                        + response.code()
                                        + "\n"
                                        + responseBody
                        );
                        return;
                    }

                    String reply = "";

                    try {

                        JSONArray trace = new JSONArray(responseBody);

                        for (int i = 0; i < trace.length(); i++) {

                            JSONObject item = trace.getJSONObject(i);
                            JSONObject payload = item.optJSONObject("payload");

                            if (payload == null) continue;

                            if (payload.has("message")) {
                                reply = payload.getString("message");
                            }

                            if (payload.has("text")) {
                                reply = payload.getString("text");
                            }
                        }

                        if (reply.isEmpty()) {
                            reply = "PAQU no respondió 😅";
                        }

                        callback.onResponse(reply);

                    } catch (Exception e) {

                        Log.e("VOICEFLOW", "PARSE ERROR", e);

                        callback.onError(
                                "Parse error: "
                                        + e.getMessage()
                                        + "\nRespuesta: "
                                        + responseBody
                        );
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Exception: " + e.getMessage());
        }
    }
}