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
        try {
            ApiService apiService = ApiClient.getClient().create(ApiService.class);

            Response<UserPreferencesResponse> prefResponse = apiService.getUserPreferences().execute();
            if (!prefResponse.isSuccessful() || prefResponse.body() == null) return Result.success();
            if (!prefResponse.body().isReadingReminderEnabled()) return Result.success();

            Response<java.util.List<com.alcove.models.BookResponse>> readingResponse = apiService.getCurrentlyReading().execute();
            if (!readingResponse.isSuccessful() || readingResponse.body() == null) return Result.success();

            int count = readingResponse.body().size();
            if (count > 0) {
                NotificationUtil.showReadingReminder(getApplicationContext(), count);
            }
        } catch (Exception e) {
            return Result.failure();
        }
        return Result.success();
    }

}
