package ru.crew.motley.piideo.network.neo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vas on 12/5/17.
 */

public class Statements {
    @SerializedName("statements")
    private List<Statement> values;

    public List<Statement> getValues() {
        if (values == null) {
            values = new ArrayList<>();
        }
        return values;
    }

    public void setValues(List<Statement> values) {
        this.values = values;
    }
}
