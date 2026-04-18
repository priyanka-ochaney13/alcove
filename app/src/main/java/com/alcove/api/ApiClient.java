package com.alcove.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static Retrofit retrofit = null;

    private static final String BASE_URL = "https://daniele-unkenned-tate.ngrok-free.dev/api/v1/";
    //private static final String BASE_URL = "http://10.0.2.2:8000/api/v1/";

    public static void resetClient() {
        retrofit = null;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        // ✅ Bypass ngrok browser warning page
                        Request request = chain.request().newBuilder()
                                .addHeader("ngrok-skip-browser-warning", "true")
                                .build();
                        return chain.proceed(request);
                    })
                    .addInterceptor(new AuthInterceptor())
                    .addInterceptor(logging)
                    .build();

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                        @Override
                        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                            String dateStr = json.getAsString();
                            // Handle varying fractions of seconds safely
                            try {
                                if (dateStr.length() > 19 && dateStr.contains(".")) {
                                    dateStr = dateStr.substring(0, dateStr.indexOf(".")) + "Z";
                                } else if (!dateStr.endsWith("Z") && !dateStr.contains("+") && !dateStr.contains("-")) {
                                    dateStr = dateStr + "Z";
                                }
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                                return format.parse(dateStr);
                            } catch (ParseException e) {
                                throw new JsonParseException(e);
                            }
                        }
                    })
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static ApiService getService() {
        return getClient().create(ApiService.class);
    }
}