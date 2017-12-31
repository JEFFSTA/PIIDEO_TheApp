package ru.crew.motley.piideo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.crew.motley.piideo.network.neo.NeoApi;
import ru.crew.motley.piideo.network.neo.Request;
import ru.crew.motley.piideo.network.neo.Statement;
import ru.crew.motley.piideo.network.neo.Statements;

public class NewMemberActivity extends AppCompatActivity {

    @BindView(R.id.create_person)
    Button createPerson;
    @BindView(R.id.response)
    TextView response;

    private String username = "neo4j";
    private String password = "aeknyy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_member);
        ButterKnife.bind(this);
        createPerson.setOnClickListener(v -> {
            Statements statements = new Statements();
            Statement create = new Statement();
            create.setStatement(Request.ME);
//            Map<String, String> parameters = new Parameters();
//            parameters.getProps().put(Request.PersonNode.PHONE, "123-123-123");
//            parameters.getProps().put(Request.PersonNode.NAME, "David");
//            create.setParameters(parameters);
            statements.getValues().add(create);
            NeoApi api = build();
            api.executeStatement(statements)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(next -> response.append(next.getResults().size() + "\n\n"),
                            error -> response.append(
                                    "Error!: " + error.getLocalizedMessage() + "\n\n"));
        });
    }

    public NeoApi build() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    okhttp3.Request originalRequest = chain.request();

                    okhttp3.Request.Builder builder = originalRequest.newBuilder()
                            .header("Authorization", Credentials.basic(username, password))
                            .header("Content-Type", "application/json;")
                            .header("Accept", "application/json;charset=UTF-8");

                    okhttp3.Request newRequest = builder.build();
                    return chain.proceed(newRequest);
                })
                .addInterceptor(interceptor)
                .build();

        RxJava2CallAdapterFactory rxAdapter = RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io());

        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(NeoApi.LOCAL_URL)
                .addCallAdapterFactory(rxAdapter)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(NeoApi.class);
    }
}
