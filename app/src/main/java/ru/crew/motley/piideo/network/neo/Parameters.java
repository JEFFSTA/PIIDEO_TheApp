package ru.crew.motley.piideo.network.neo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vas on 12/5/17.
 */

public class Parameters {
    private Map<String, Object> props;

    public Map<String, Object> getProps() {
        if (props == null) {
            props = new HashMap<>();
        }
        return props;
    }

    public void setProps(Map<String, Object> props) {
        this.props = props;
    }
}
