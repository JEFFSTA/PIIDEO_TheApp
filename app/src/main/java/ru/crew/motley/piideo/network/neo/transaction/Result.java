package ru.crew.motley.piideo.network.neo.transaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vas on 12/18/17.
 */

public class Result {

    @SerializedName("columns")
    private List<String> columns;
    @SerializedName("data")
    private List<Data> data;

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        if (data == null || data.isEmpty()) {
            return "Data is empty";
        }
        StringBuilder builder = new StringBuilder();
        for (Data data: data) {
            builder.append(data.toString());
            builder.append("\n");
        }
        return builder.toString();
    }
}
