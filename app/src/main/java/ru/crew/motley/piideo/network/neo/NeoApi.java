package ru.crew.motley.piideo.network.neo;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;
import ru.crew.motley.piideo.network.neo.transaction.TransactionResponse;

/**
 * Created by vas on 12/5/17.
 */

public interface NeoApi {

    String BASE_URL = "http://172.16.206.101:7474";

    @POST("/db/data/transaction/commit")
    Single<TransactionResponse> executeStatement(@Body Statements statements);

}
