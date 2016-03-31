package ua.nure.vkmessanger.http;

import android.util.Log;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ua.nure.vkmessanger.model.Message;
import ua.nure.vkmessanger.model.UserDialog;

/**
 * Класс обертка, для Http-запросов.
 */
public class RESTVkSdkManager implements RESTInterface {

    private static final String REST_MANAGER_LOG_TAG = "REST_VK_SDK_MANAGER_LOG";

    private static final int USER_DIALOGS_DEFAULT_REQUEST_COUNT = 100;

    @Override
    public void loadUserDialogs(final ResponseCallback<UserDialog> responseCallback) {
        VKRequest currentRequest = VKApi.messages().getDialogs(
                VKParameters.from(VKApiConst.COUNT, USER_DIALOGS_DEFAULT_REQUEST_COUNT));
        currentRequest.attempts = 10;

        currentRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                List<UserDialog> dialogs = new ArrayList<>();
                try {
                    JSONArray responseMessagesArrayJSON = response.json.getJSONObject("response").getJSONArray("items");

                    for (int i = 0; i < responseMessagesArrayJSON.length(); i++) {
                        JSONObject messageJSON = responseMessagesArrayJSON.getJSONObject(i).getJSONObject("message");
                        dialogs.add(new UserDialog(
                                messageJSON.optInt("chat_id"),
                                messageJSON.getInt("user_id"),
                                messageJSON.getString("body")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(REST_MANAGER_LOG_TAG, String.format("Dialogs count == %d", dialogs.size()));

                //И передаю результат на callback в активити или фрагмент.
                responseCallback.onResponse(dialogs);
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                Log.d(REST_MANAGER_LOG_TAG, "attemptFailed " + request + " " + attemptNumber + " " + totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.d(REST_MANAGER_LOG_TAG, "onError: " + error);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
                Log.d(REST_MANAGER_LOG_TAG, "onProgress " + progressType + " " + bytesLoaded + " " + bytesTotal);
            }
        });
    }

    @Override
    public void loadSelectedDialog(int dialogId, ResponseCallback<Message> responseCallback) {

    }
}