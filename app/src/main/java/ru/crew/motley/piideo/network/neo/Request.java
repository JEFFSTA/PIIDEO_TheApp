package ru.crew.motley.piideo.network.neo;

/**
 * Created by vas on 12/6/17.
 */

public class Request {

    public static final class PersonNode {
        public static final String LABEL = ":Person";

        public static final String PHONE = "phoneNumber";
        public static final String NAME = "name";
        public static final String ASKED = "askedAt";
    }

    public static final class SubjectNode {
        public static final String LABEL = ":Subject";

        public static final String NAME = "name";
    }

    public static final class KnowsRelation {
        public static final String LABEL = ":KNOWS";
    }

    public static final class StudiesRelation {
        public static final String LABEL = ":STUDIES";
    }

    public static final class Var {
        public static final String PHONE = "phone";
        public static final String NAME = "name";
        public static final String PHONE_FROM = PHONE + "From";
        public static final String PHONE_TO = PHONE + "To";
    }

    public static final String ME =
            "MERGE (p" + PersonNode.LABEL +
                    " { " + PersonNode.PHONE + ": {props}." + Var.PHONE + "}) " +
                    "ON CREATE SET p.registered = TRUE " +
                    "ON MATCH SET p.registered = TRUE " +
                    "RETURN p";


    public static final String KNOWS =
            "MATCH (from" + PersonNode.LABEL + " {" + PersonNode.PHONE + ": {props}." + Var.PHONE + "From }), " +
                    "(to" + PersonNode.LABEL + " {" + PersonNode.PHONE + ": {props}." + Var.PHONE + "To }) " +
                    "MERGE (from)-[" + KnowsRelation.LABEL + "]->(to)";

    public static final String STUDIES =
            "MATCH (from" + PersonNode.LABEL + " {" + PersonNode.PHONE + ": {props}." + Var.PHONE + " }), " +
                    "(to" + SubjectNode.LABEL + " {" + SubjectNode.NAME + ": {props}." + Var.NAME + "}) " +
                    "MERGE (from)-[" + StudiesRelation.LABEL + "]->(to)";

    public static final String FIND_PERSON =
            "MATCH (p" + PersonNode.LABEL +
                    " { " + PersonNode.PHONE + ": {props}." + Var.PHONE + " }) " +
                    "RETURN p";

//    public static final String FIND_CONTACTS =
//            "WITH [1026, 1207] AS phones " +
//                    "MATCH (p" + PersonNode.LABEL+ ") where p." + PersonNode.PHONE + " in phones" +
//                    " return p";

    public static final String NEW_CONTACT =
            "MERGE (p" + PersonNode.LABEL +
                    " { " + PersonNode.PHONE + ": {props}." + Var.PHONE + "})" +
                    " ON CREATE SET p.registered = FALSE";

    public static final String NEW_SUBJECT =
            "MERGE (s" + SubjectNode.LABEL +
                    " { " + SubjectNode.NAME + ": {props}." + Var.NAME + " })";

    public static final String FIND_SUBJECTS =
            "MATCH (s:" + SubjectNode.LABEL + ") RETURN s";

//    public static final String FIND_QUESTION_TARGET =
//            "MATCH (" + SubjectNode.LABEL + " {" + SubjectNode.NAME + " : { props }." + Var.NAME + " })" +
//                    "<-[" + StudiesRelation.LABEL + "]-(me" + PersonNode.LABEL + " {" + PersonNode.PHONE + " : { props }." + Var.PHONE + " })" +
//                    "<-[" + KnowsRelation.LABEL + "*1..]-" +
//                    "(friendOfFriend" + PersonNode.LABEL + ")" +
//                    "-[" + StudiesRelation.LABEL + "]->" +
//                    "(" + SubjectNode.LABEL + " {" + SubjectNode.NAME + " : { subjectName }) " +
//                    "RETURN friendOfFriend LIMIT 7";

    public static final String FIND_QUESTION_TARGET =
            "MATCH (me" + PersonNode.LABEL + " {" + PersonNode.PHONE + " : { props }." + Var.PHONE + " })" +
                    "<-[" + KnowsRelation.LABEL + "*1..]-" +
                    "(friendOfFriend" + PersonNode.LABEL + ")" +
                    "-[" + StudiesRelation.LABEL + "]->" +
                    "(" + SubjectNode.LABEL + " {" + SubjectNode.NAME + " : { props }." + Var.NAME + " }) " +
                    "RETURN friendOfFriend LIMIT 7";
}
