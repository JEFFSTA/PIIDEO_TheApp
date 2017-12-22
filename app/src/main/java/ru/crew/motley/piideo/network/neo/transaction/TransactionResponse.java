package ru.crew.motley.piideo.network.neo.transaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vas on 12/18/17.
 */

public class TransactionResponse {

    @SerializedName("results")
    private List<Result> results;
    @SerializedName("errors")
    private List<Object> errors;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public List<Object> getErrors() {
        return errors;
    }

    public void setErrors(List<Object> errors) {
        this.errors = errors;
    }

}
