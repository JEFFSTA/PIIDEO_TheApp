package ru.crew.motley.piideo.network.neo;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by vas on 12/5/17.
 */

public class Statement {
    @SerializedName("statement")
    private String statement;
    @SerializedName("parameters")
    private Parameters parameters;

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }
}
