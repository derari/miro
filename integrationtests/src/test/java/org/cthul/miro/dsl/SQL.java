package org.cthul.miro.dsl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.view.ViewR;

/**
 *
 */
public class SQL {
    
    public static Select select() {
        return new Select();
    }
    
    public static Select select(Object... attributes) {
        return new Select(Arrays.asList(attributes));
    }
    
    public static class Select {
        private final List<?> attributes;
        private MiConnection connection = null;

        public Select() {
            this(Collections.emptyList());
        }

        public Select(List<?> attributes) {
            this.attributes = attributes;
        }
        
        public Select via(MiConnection connection) {
            this.connection = connection;
            return this;
        }
        
        public <Query> Query from(ViewR<Query> view) {
            if (connection != null) {
                List<Object> att = new ArrayList<>(attributes.size()+1);
                att.add(connection);
                att.addAll(attributes);
                return view.select(att);
            }
            return view.select(attributes);
        }
    }
}
