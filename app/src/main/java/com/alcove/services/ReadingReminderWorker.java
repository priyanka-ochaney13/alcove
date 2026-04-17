package com.alcove.services;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.alcove.api.ApiClient;
import com.alcove.api.ApiService;
import com.alcove.models.UserPreferencesResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadingReminderWorker extends Worker {

    public ReadingReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get user preferences to check if reminders are enabled
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getUserPreferences().enqueue(new Callback<UserPreferencesResponse>() {
            @Override
            public void onResponse(Call<UserPreferencesResponse> call, Response<UserPreferencesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserPreferencesResponse preferences = response.body();
                    if (preferences.isReadingReminderEnabled()) {
                        // Get currently reading count and show notification
                        getCurrentlyReadingCountAndNotify();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserPreferencesResponse> call, Throwable t) {
                // Silently fail - don't show notification if we can't get preferences
            }
        });

        return Result.success();
    }

    private void getCurrentlyReadingCountAndNotify() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getCurrentlyReading().enqueue(new Callback<java.util.List<com.alcove.models.BookResponse>>() {
            @Override
            public void onResponse(Call<java.util.List<com.alcove.models.BookResponse>> call, Response<java.util.List<com.alcove.models.BookResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int currentlyReadingCount = response.body().size();
                    if (currentlyReadingCount > 0) {
                        NotificationUtil.showReadingReminder(getApplicationContext(), currentlyReadingCount);
                    }
                }
            }

            @Override
            public void onFailure(Call<java.util.List<com.alcove.models.BookResponse>> call, Throwable t) {
                // Fallback to old hardcoded method if API fails
                int currentlyReadingCount = getCurrentlyReadingCount();
                if (currentlyReadingCount > 0) {
                    NotificationUtil.showReadingReminder(getApplicationContext(), currentlyReadingCount);
                }
            }
        });
    }

    private int getCurrentlyReadingCount() {
        // TODO: Replace with logic to check SQLite / Backend / Firebase for real data
        return 2;
    }
}
