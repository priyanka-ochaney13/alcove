package com.alcove.api;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.android.gms.tasks.Tasks;
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
                // Always get a fresh token to avoid expiration issues
                GetTokenResult tokenResult = Tasks.await(user.getIdToken(true));
                String token = tokenResult.getToken();
                if (token != null && !token.isEmpty()) {
                    request = request.newBuilder()
                            .addHeader("Authorization", "Bearer " + token)
                            .build();
                    System.out.println("AuthInterceptor: Token added successfully");
                } else {
                    System.out.println("AuthInterceptor: Token was null or empty");
                }
            } catch (Exception e) {
                System.out.println("AuthInterceptor: Token retrieval failed: " + e.getMessage());
                // Don't throw IOException for "too early" scenarios if we want to retry or proceed
                // proceeding without token will just result in 401 which the app handles
            }
        } else {
            System.out.println("AuthInterceptor: No current user");
        }

        return chain.proceed(request);
    }
}
