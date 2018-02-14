package ru.crew.motley.piideo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.crew.motley.piideo.network.neo.NeoApi;
import ru.crew.motley.piideo.network.neo.NeoApiSingleton;
import ru.crew.motley.piideo.network.neo.Parameters;
import ru.crew.motley.piideo.network.neo.Request;
import ru.crew.motley.piideo.network.neo.Statement;
import ru.crew.motley.piideo.network.neo.Statements;
import ru.crew.motley.piideo.network.neo.transaction.Row;

import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private static final String AWS_USERNAME = "neo4j";
    private static final String AWS_PASSWORD = "piideo_test";

    private static final String MY_PHONE = "9537395447";
    private static final String SUBJECT_NAME = "Thermodynamique";
    private static final String GROUP_NAME = "Engineering School";

    private Statements testStatements;
    private NeoApi testApi;

    @Before
    public void prepareStatements() {
        Statement subject = new Statement();
        subject.setStatement(Request.FIND_QUESTION_TARGET_0);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.Var.PHONE, MY_PHONE);
        parameters.getProps().put(Request.Var.NAME, SUBJECT_NAME);
        parameters.getProps().put(Request.Var.NAME_2, GROUP_NAME);
        subject.setParameters(parameters);
        testStatements = new Statements();
        testStatements.getValues().add(subject);
    }

    @Before
    public void prepareRetrofit() {
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

        Gson gson = new GsonBuilder().registerTypeAdapter(Row.class, new NeoApiSingleton.RowResponseTypeAdapter())
                .setPrettyPrinting()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
//                .baseUrl(NeoApi.AWS_INSTANCE_TEST)
                .baseUrl(NeoApi.AWS_INSTANCE_TEST)
                .addCallAdapterFactory(rxAdapter)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        testApi = retrofit.create(NeoApi.class);
    }

    String result = "";

    @Test
    public void testSearchRequest() {

        testApi.executeStatement(testStatements)
                .subscribe(
                        transactionResponse -> {
                            System.out.println(transactionResponse.toString());
                        },
                        System.out::println
                );
        result = "";
        Observable<String> observer = Observable.just("Hello"); // provides datea
        observer.subscribe(s -> result = s); // Callable as subscriber
        assertTrue(result.equals("Hello"));
        System.out.println("DONE");
    }

}