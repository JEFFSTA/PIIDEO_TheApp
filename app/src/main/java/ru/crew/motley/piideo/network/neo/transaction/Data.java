package ru.crew.motley.piideo.network.neo.transaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vas on 12/18/17.
 */

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

}
