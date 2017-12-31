package ru.crew.motley.piideo.chat.db;

/**
 * Created by vas on 12/26/17.
 */

public final class PiideoSchema {

    public static final class ChatTable {
        public static final String NAME = "chat";

        public static final class Cols {
            public static final String UUID = "id";
            public static final String PIIDEO_FILE = "PIIDEO_FILE";
        }
    }
}
