package ru.crew.motley.piideo.network.neo;

/**
 * Created by vas on 12/6/17.
 */

public class Request {

    public static final class PersonNode {
        public static final String LABEL = ":Person";

        public static final String PHONE = "phoneNumber";
        public static final String NAME = "name";
        public static final String DLG_TIME = "dialogTime";
        public static final String CC = "countryCode";
        public static final String PH_PREFIX = "phonePrefix";
        public static final String CHAT_ID = "chatId";
    }

    public static final class SubjectNode {
        public static final String LABEL = ":Subject";

        public static final String NAME = "name";
    }

    public static final class SchoolGroupNode {
        public static final String LABEL = ":SchoolGroup";
        public static final String NAME = "name";
    }

    public static final class KnowsRelation {
        public static final String LABEL = ":KNOWS";
    }

    public static final class StudiesRelation {
        public static final String LABEL = ":STUDIES";
    }

    public static final class ContainsRelation {
        public static final String LABEL = ":CONTAINS";
    }

    public static final class Var {
        public static final String ID = "id";
        public static final String PHONE = "phone";
        public static final String NAME = "name";
        public static final String NAME_2 = "name_2";
        public static final String CHAT_ID = "chat_id";
        public static final String C_CODE = "country_code";
        public static final String PH_PREFIX = "phone_prefix";
        public static final String DLG_TIME = "dialog_time";
    }

    public static final String ME =
            "MERGE (p" + PersonNode.LABEL +
                    " { " + PersonNode.PHONE + ": {props}." + Var.PHONE + "}) " +
                    "ON CREATE SET p.registered = TRUE, p.chatId = {props}.chat_id " +
                    "ON MATCH SET p.registered = TRUE, p.chatId = {props}.chat_id " +
                    "RETURN p";

    public static final String ME_WITH_CC =
            "MERGE (p" + PersonNode.LABEL +
                    " { " + PersonNode.PHONE + ": {props}." + Var.PHONE + "}) " +
                    "ON CREATE SET p.registered = TRUE," +
                    "     p.chatId = {props}.chat_id, " +
                    "     p.countryCode = {props}.country_code " +
                    "ON MATCH SET p.registered = TRUE," +
                    "     p.chatId = {props}.chat_id, " +
                    "     p.countryCode = {props}.country_code " +
                    "RETURN p";

    public static final String ME_WITH_PREFIX =
            "MERGE (p" + PersonNode.LABEL +
                    " { " + PersonNode.PHONE + ": {props}." + Var.PHONE + "}) " +
                    "ON CREATE SET p.registered = TRUE," +
                    "     p.chatId = {props}.chat_id, " +
                    "     p.phonePrefix = {props}.phone_prefix " +
                    "ON MATCH SET p.registered = TRUE," +
                    "     p.chatId = {props}.chat_id, " +
                    "     p.phonePrefix = {props}.phone_prefix " +
                    "RETURN p";

    public static final String ME_WITH_CC_AND_PREFIX =
            "MERGE (p" + PersonNode.LABEL +
                    " { " + PersonNode.PHONE + ": {props}." + Var.PHONE + "}) " +
                    "ON CREATE SET p.registered = TRUE, " +
                    "     p.chatId = {props}.chat_id, " +
                    "     p.countryCode = {props}.country_code, " +
                    "     p.phonePrefix = {props}.phone_prefix " +
                    "ON MATCH SET p.registered = TRUE," +
                    "     p.chatId = {props}.chat_id, " +
                    "     p.countryCode = {props}.country_code, " +
                    "     p.phonePrefix = {props}.phone_prefix " +
                    "RETURN p";

    public static final String KNOWS =
            "MATCH (from" + PersonNode.LABEL + " {" + PersonNode.PHONE + ": {props}." + Var.PHONE + "From }), " +
                    "(to" + PersonNode.LABEL + " {" + PersonNode.PHONE + ": {props}." + Var.PHONE + "To }) " +
                    "MERGE (from)-[" + KnowsRelation.LABEL + "]->(to)";

    public static final String STUDIES =
            "MATCH (from" + PersonNode.LABEL + " {" + PersonNode.PHONE + ": {props}." + Var.PHONE + " }), " +
                    "(to" + SubjectNode.LABEL + " {" + SubjectNode.NAME + ": {props}." + Var.NAME + " }) " +
                    "WHERE id(to) = {props}." + Var.ID +
                    " MERGE (from)-[" + StudiesRelation.LABEL + "]->(to)";

    public static final String STUDIES_NOTHING =
            "MATCH (from" + PersonNode.LABEL + " {" + PersonNode.PHONE + ": {props}." + Var.PHONE + " })" +
                    "-[r" + StudiesRelation.LABEL + "]->" +
                    "(" + SubjectNode.LABEL + " ) " +
                    " DELETE r";

    public static final String NEW_CONTACT =
            "MERGE (p" + PersonNode.LABEL +
                    " { " + PersonNode.PHONE + ": {props}." + Var.PHONE + "})" +
                    " ON CREATE SET p.registered = FALSE";

    public static final String NEW_CONTACT_WITH_PHONE_PREFIX =
            "MERGE (p" + PersonNode.LABEL +
                    " { " + PersonNode.PHONE + ": {props}." + Var.PHONE + "})" +
                    " ON CREATE SET p.registered = FALSE, p.phonePrefix = {props}." + Var.PH_PREFIX +
                    " ON MATCH SET p.phonePrefix = {props}." + Var.PH_PREFIX;

    public static final String NEW_CONTACT_WITH_COUNTRY_CODE =
            "MERGE (p" + PersonNode.LABEL +
                    " { " + PersonNode.PHONE + ": {props}." + Var.PHONE + "})" +
                    " ON CREATE SET p.registered = FALSE, p.countryCode = {props}." + Var.C_CODE +
                    " ON MATCH SET p.countryCode = {props}." + Var.C_CODE;

    public static final String DELETE_CONTACT =
            "MATCH (p" + PersonNode.LABEL +
                    " { " + PersonNode.PHONE + ": {props}." + Var.PHONE + "})" +
                    "-[rs" + KnowsRelation.LABEL + "]->(p0:Person) " +
                    " WHERE NOT p0." + PersonNode.PHONE + " IN ";

    public static final String NEW_SUBJECT =
            "MATCH (g" + SchoolGroupNode.LABEL + " {" + SchoolGroupNode.NAME + " : {props}." + Var.NAME_2 + "}) " +
                    "MERGE (s" + SubjectNode.LABEL +
                    " { " + SubjectNode.NAME + ": {props}." + Var.NAME + " })" +
                    "-[" + ContainsRelation.LABEL + "]" +
                    "->(g) " +
                    "RETURN s";

    public static final String FIND_QUESTION_TARGET =
            "MATCH (me" + PersonNode.LABEL + " {" + PersonNode.PHONE + " : { props }." + Var.PHONE + " })" +
                    "<-[" + KnowsRelation.LABEL + "*1..]-" +
                    "(friendOfFriend" + PersonNode.LABEL + ")" +
                    "-[" + StudiesRelation.LABEL + "]->" +
                    "(s" + SubjectNode.LABEL + " {" + SubjectNode.NAME + " : { props }." + Var.NAME + " })" +
                    "-[" + ContainsRelation.LABEL + "]->" +
                    "(" + SchoolGroupNode.LABEL + " { name : {props}." + Var.NAME_2 + "})" +
                    " WHERE friendOfFriend." + PersonNode.PHONE + " <> { props }." + Var.PHONE +
                    " AND NOT EXISTS(friendOfFriend." + PersonNode.DLG_TIME + ")" +
                    " RETURN DISTINCT friendOfFriend LIMIT 7";

    public static final String FIND_SUBJECTS_BY_SCHOOL =
            "MATCH (s" + SubjectNode.LABEL + ")" +
                    "-[r" + ContainsRelation.LABEL + "]" +
                    "->(g" + SchoolGroupNode.LABEL + " {" + SchoolGroupNode.NAME + ": {props}." + Var.NAME + "})" +
                    "RETURN s ORDER BY LOWER(s.name)";

    public static final String FIND_USED_SUBJECTS_BY_SCHOOL =
            "MATCH ()-->(s" + SubjectNode.LABEL + ")" +
                    "-[r" + ContainsRelation.LABEL + "]" +
                    "->(g" + SchoolGroupNode.LABEL + " {" + SchoolGroupNode.NAME + ": {props}." + Var.NAME + "})" +
                    "RETURN DISTINCT s ORDER BY LOWER(s.name)";

    public static final String FIND_ALL_SCHOOL_GROUPS =
            "MATCH (g" + SchoolGroupNode.LABEL + ") " +
                    "RETURN g";

    public static final String MAKE_ME_BUSY =
            "MERGE (me" + PersonNode.LABEL + " {" + PersonNode.PHONE + " : {props}." + Var.PHONE + " }) " +
                    " ON MATCH SET me.dialogTime = {props}." + Var.DLG_TIME;

    public static final String MAKE_ME_FREE =
            "MATCH (me" + PersonNode.LABEL + " {" + PersonNode.PHONE + " : {props}." + Var.PHONE + " }) " +
                    " REMOVE me.dialogTime";
}
