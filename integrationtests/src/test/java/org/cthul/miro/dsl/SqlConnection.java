package org.cthul.miro.dsl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.view.ViewR;

/**
 *
 */
public interface SqlConnection {
    
    default Select select() {
        return select(Collections.emptyList());
    }
    
    default Select select(Object... attributes) {
        return select(Arrays.asList(attributes));
    }
    
    Select select(List<?> attributes);
    
    interface Select {
        <Query> Query from(ViewR<Query> view);
    }
    
    static SqlConnection wrap(MiConnection connection) {
        return new SqlConnectionWrapper(connection);
    }
}
