package ru.crew.motley.piideo.network.neo;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;
import ru.crew.motley.piideo.network.neo.transaction.TransactionResponse;


public interface NeoApi {


    String LOCAL_URL = "http://172.16.206.102:7474";
    String GRAPHENE_TEST = "http://hobby-gkpaichbhigjgbkefmjegkal.dbs.graphenedb.com:24789/db/data/";
//    String AWS_INSTANCE_TEST = "http://ec2-54-93-246-79.eu-central-1.compute.amazonaws.com:7474/db/data/";
    String AWS_INSTANCE_TEST = "http://ec2-35-158-126-107.eu-central-1.compute.amazonaws.com:7474/db/data/";


    @POST("/db/data/transaction/commit")
    Single<TransactionResponse> executeStatement(@Body Statements statements);

}
