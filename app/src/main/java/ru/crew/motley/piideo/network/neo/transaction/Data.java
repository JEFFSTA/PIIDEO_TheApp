package ru.crew.motley.piideo.network.neo.transaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {


    @SerializedName("row")
    private List<Row> row;
    @SerializedName("meta")
    private List<RowMeta> meta = null;

    public List<Row> getRow() {
        return row;
    }

    public void setRow(List<Row> row) {
        this.row = row;
    }

    public List<RowMeta> getMeta() {
        return meta;
    }

    public void setMeta(List<RowMeta> meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        if (row == null || row.isEmpty()) {
            return "Row is empty";
        }
        StringBuilder builder = new StringBuilder();
        for (Row row: row) {
            builder.append(row.toString());
            builder.append("\n");
        }
        return builder.toString();
    }
}
