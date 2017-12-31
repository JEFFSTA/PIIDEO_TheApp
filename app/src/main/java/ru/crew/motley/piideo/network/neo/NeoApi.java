package ru.crew.motley.piideo.network.neo;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;
import ru.crew.motley.piideo.network.neo.transaction.TransactionResponse;

/**
 * Created by vas on 12/5/17.
 */

public interface NeoApi {


    String LOCAL_URL = "http://172.16.206.101:7474";
    String GRAPHENE_TEST = "http://hobby-gkpaichbhigjgbkefmjegkal.dbs.graphenedb.com:24789/db/data/";
    String AWS_INSTANCE_TEST = "http://ec2-54-93-246-79.eu-central-1.compute.amazonaws.com:7474/db/data/";

    @POST("/db/data/transaction/commit")
    Single<TransactionResponse> executeStatement(@Body Statements statements);

}
