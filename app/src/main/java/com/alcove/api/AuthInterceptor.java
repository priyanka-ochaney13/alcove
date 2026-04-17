package com.alcove.api;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * AuthInterceptor
 *
 * Attaches Firebase ID token as Authorization header to API requests.
 * Falls back gracefully if auth fails or token unavailable.
 */
public class AuthInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            try {
                GetTokenResult tokenResult = user.getIdToken(false).getResult();
                String token = tokenResult.getToken();
                if (token != null && !token.isEmpty()) {
                    request = request.newBuilder()
                            .addHeader("Authorization", "Bearer " + token)
                            .build();
                }
            } catch (Exception e) {
                // Token retrieval failed, proceed without auth header
                // This allows the app to work even if token refresh fails
            }
        }

        return chain.proceed(request);
    }
}
