package ru.crew.motley.piideo.network.neo.transaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

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

    @Override
    public String toString() {
        if (getResults() == null || getResults().isEmpty()) {
            return "Results is empty";
        }
        StringBuilder builder = new StringBuilder();
        for (Result result : getResults()) {
            builder.append(result.toString());
            builder.append("\n");
        }
        return builder.toString();
    }
}
