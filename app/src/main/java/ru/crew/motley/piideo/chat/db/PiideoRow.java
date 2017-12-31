package ru.crew.motley.piideo.chat.db;

/**
 * Created by vas on 12/26/17.
 */

public class PiideoRow {

    private int mId;
    private String piideoFileName;

    public PiideoRow() {
    }

    public PiideoRow(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getPiideoFileName() {
        return piideoFileName;
    }

    public void setPiideoFileName(String piideoFileName) {
        this.piideoFileName = piideoFileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PiideoRow piideoRow = (PiideoRow) o;

        return mId == piideoRow.mId;
    }

    @Override
    public int hashCode() {
        return mId;
    }
}
