package org.cthul.miro;

import org.cthul.miro.view.*;

public class DSL {

    public static Select select(String... attributes)  {
        return new Select(attributes);
    }
    
    public static Select0 select() {
        return SELECT0;
    }
    
    public static Insert insert(String... attributes)  {
        return new Insert(attributes);
    }
    
    public static Insert0 insert() {
        return INSERT0;
    }
    
    public static Update update(String... attributes)  {
        return new Update(attributes);
    }
    
    public static Update0 update() {
        return UPDATE0;
    }
    
    public static Delete0 delete() {
        return DELETE0;
    }
    
    private static final Select0 SELECT0 = new Select0();
    private static final Update0 UPDATE0 = new Update0();
    private static final Insert0 INSERT0 = new Insert0();
    private static final Delete0 DELETE0 = new Delete0();
    
    public static class Select {
        private final String[] attributes;

        public Select(String[] attributes) {
            this.attributes = attributes;
        }
        
        public <Q> Q from(ViewR<Q> view) {
            return view.select(attributes);
        }
    }
    
    public static class Select0 {
        public <Q> Q from(ViewR<Q> view) {
            return view.select();
        }
    }
    
    public static class Insert {
        private final String[] attributes;

        public Insert(String[] attributes) {
            this.attributes = attributes;
        }
        
        public <Q> Q into(ViewC<Q> view) {
            return view.insert(attributes);
        }
    }
    
    public static class Insert0 {
        public <Q> Q from(ViewC<Q> view) {
            return view.insert();
        }
    }
    
    public static class Update {
        private final String[] attributes;

        public Update(String[] attributes) {
            this.attributes = attributes;
        }
        
        public <Q> Q table(ViewU<Q> view) {
            return view.update(attributes);
        }
    }
    
    public static class Update0 {
        public <Q> Q table(ViewU<Q> view) {
            return view.update();
        }
    }
    
    public static class Delete0 {
        public <Q> Q table(ViewD<Q> view) {
            return view.delete();
        }
    }
}
