package ru.crew.motley.piideo;

import org.junit.Before;
import org.junit.Test;

import ru.crew.motley.piideo.network.neo.Parameters;
import ru.crew.motley.piideo.network.neo.Request;
import ru.crew.motley.piideo.network.neo.Statement;
import ru.crew.motley.piideo.network.neo.Statements;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    private static final String MY_NAME = "May 1218";
    private static final String SUBJECT_NAME = "Math 0";

    private Statements searchFriends;

    @Before
    public void prepareSearchRequest() {
        searchFriends = new Statements();
        Statement statement = new Statement();
        statement.setStatement(Request.FIND_QUESTION_TARGET);
        Parameters parameters = new Parameters();
        parameters.getProps().put(Request.PersonNode.NAME, "May 1218");
        parameters.getProps().put()
        create.setParameters(parameters);
        statements.getValues().add(create);
    }
}