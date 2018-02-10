package ru.crew.motley.piideo.network.neo;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import io.reactivex.schedulers.Schedulers;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.crew.motley.piideo.network.neo.transaction.Row;

/**
 * Created by vas on 12/18/17.
 */

public class NeoApiSingleton {

    private static final String USERNAME = "neo4j";
    private static final String PASSWORD = "aeknyy";

    private static final String GRAPHENE_USERNAME = "test-user";
    private static final String GRAPHENE_PASSWORD = "b.IhmmnQZyAIH3.CWInrHqxppVio9oF";

    private static final String AWS_USERNAME = "neo4j";
    private static final String AWS_PASSWORD = "piideo_test";

    private static NeoApi sInstance;

    public static NeoApi getInstance() {
        if (sInstance == null) {
            sInstance = newInstance();
        }
        return sInstance;
    }

    private static NeoApi newInstance() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request originalRequest = chain.request();

                    okhttp3.Request.Builder builder = originalRequest.newBuilder()
                            .header("Authorization", Credentials.basic(AWS_USERNAME, AWS_PASSWORD))
                            .header("Content-Type", "application/json;")
                            .header("Accept", "application/json;charset=UTF-8");

                    okhttp3.Request newRequest = builder.build();
                    return chain.proceed(newRequest);
                })
                .addInterceptor(interceptor)
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(110, TimeUnit.SECONDS)
                .readTimeout(110, TimeUnit.SECONDS)
                .build();

        RxJava2CallAdapterFactory rxAdapter = RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io());

        Gson gson = new GsonBuilder().registerTypeAdapter(Row.class, new RowResponseTypeAdapter())
                .setPrettyPrinting()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
//                .baseUrl(NeoApi.AWS_INSTANCE_TEST)
                .baseUrl(NeoApi.AWS_INSTANCE_TEST)
                .addCallAdapterFactory(rxAdapter)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(NeoApi.class);
    }

    public static class RowResponseTypeAdapter implements JsonDeserializer<Row> {
        @Override
        public Row deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Row row = new Row();
            row.setValue(json.toString());
            return row;
        }

        /**
         * @param json FIND_PERSON response
         * @return "results" nested node from json
         */
        private JsonArray getResults(JsonElement json) {
            return json.getAsJsonObject().getAsJsonArray("results");
        }

        /**
         * @param json "results" nested node
         * @return first element from "data" nested node
         */
        private JsonObject getData(JsonArray json) {
            return json.get(0).getAsJsonObject()
                    .getAsJsonArray("data")
                    .get(0)
                    .getAsJsonObject();
        }

        /**
         * @param json "data" nested node
         * @return first element from "row" nested node
         */
        private JsonObject getRow(JsonObject json) {
            return json.get("row").getAsJsonArray().get(0).getAsJsonObject();
        }

        /**
         * @param json "data" nested node
         * @return first element from "meta" nested node
         */
        private JsonObject getMeta(JsonObject json) {
            return json.get("meta").getAsJsonArray().get(0).getAsJsonObject();
        }

    }

//    public class TrResponseAdapterFactory implements TypeAdapterFactory {
//
//        public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {
//
//            final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
//            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
//
//            return new TypeAdapter<T>() {
//
//                public void write(JsonWriter out, T value) throws IOException {
//                    delegate.write(out, value);
//                }
//
//                public T read(JsonReader in) throws IOException {
//
//                    JsonElement jsonElement = elementAdapter.read(in);
//                    if (jsonElement.isJsonObject()) {
//                        JsonObject json = jsonElement.getAsJsonObject();
//                        JsonArray jsonArray = getResults(json);
//                        JsonObject jsonData = getData(jsonArray);
//                        JsonObject jsonRow = getRow(jsonData);
//                        JsonObject jsonMeta = getMeta(jsonData);
//                        jsonElement = jsonRow;
//                    }
//
//                    return delegate.fromJsonTree(jsonElement);
//                }
//            }.nullSafe();
//        }
//
//        /**
//         * @param json FIND_PERSON response
//         * @return "results" nested node from json
//         */
//        private JsonArray getResults(JsonElement json) {
//            return json.getAsJsonObject().getAsJsonArray("results");
//        }
//
//        /**
//         * @param json "results" nested node
//         * @return first element from "data" nested node
//         */
//        private JsonObject getData(JsonArray json) {
//            return json.get(0).getAsJsonObject()
//                    .getAsJsonArray("data")
//                    .get(0)
//                    .getAsJsonObject();
//        }
//
//        /**
//         * @param json "data" nested node
//         * @return first element from "row" nested node
//         */
//        private JsonObject getRow(JsonObject json) {
//            return json.get("row").getAsJsonArray().get(0).getAsJsonObject();
//        }
//
//        /**
//         * @param json "data" nested node
//         * @return first element from "meta" nested node
//         */
//        private JsonObject getMeta(JsonObject json) {
//            return json.get("meta").getAsJsonArray().get(0).getAsJsonObject();
//        }
//    }

}
