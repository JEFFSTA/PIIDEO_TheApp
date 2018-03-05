package ru.crew.motley.piideo.chat.db;

/**
 * Created by vas on 12/26/17.
 */

public final class PiideoSchema {

    public static final class ChatTable {
        public static final String NAME = "chat";

        public static final String PIIDEO_STATE_DONE = "done";

        public static final class Cols {
            public static final String UUID = "id";
            public static final String PIIDEO_FILE = "PIIDEO_FILE";
            public static final String PIIDEO_STATE = "piideo_state";
        }
    }

    public static final class MemberTable {
        public static final String NAME = "user";

        public static final class Cols {
            public static final String NEO_ID = "neo_id";
            public static final String PHONE = "phone";
            public static final String CHAT_ID = "chat_id";
            public static final String C_CODE = "country_code";
            public static final String PH_PREFIX = "phone_prefix";

        }
    }

    public static final class MemberQueue {
        public static final String NAME = "user_queue";

        public static final class Cols {
            public static final String NEO_ID = "neo_id";
            public static final String PHONE_NUM = "phone";
            public static final String PHONE_PR = "phone_prefix";
            public static final String CHAT_ID = "chat_id";
            public static final String C_CODE = "country_code";
        }
    }

    public static final class SubjectTable {
        public static final String NAME = "subject";

        public static final class Cols {
            public static final String NEO_ID = "neo_id";
            public static final String NAME = "name";
        }
    }

    public static final class SchoolTable {
        public static final String NAME = "school";

        public static final class Cols {
            public static final String NEO_ID = "neo_id";
            public static final String NAME = "name";
        }
    }

    public static final class MessageTable {
        public static final String NAME = "message";



        public static final class Cols {
            public static final String UUID = "id";
            public static final String SENDER_ID = "sender_id";
            public static final String RECEIVER_ID = "receiver_id";
            public static final String CONTENT = "content";
            public static final String MSG_TYPE = "msg_type";
        }
    }
}
