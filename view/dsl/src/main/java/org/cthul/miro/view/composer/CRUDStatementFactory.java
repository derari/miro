package org.cthul.miro.view.composer;

import org.cthul.miro.composer.StatementFactory;
import java.util.List;
import org.cthul.miro.composer.template.Template;

/**
 *
 */
public class CRUDStatementFactory<CB, RB, UB, DB, C, R, U, D> {
    
    private StatementFactory<CB, C> insertFactory;
    private StatementFactory<RB, R> selectFactory;
    private StatementFactory<UB, U> updateFactory;
    private StatementFactory<DB, D> deleteFactory;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public CRUDStatementFactory(StatementFactory<CB, C> insertFactory, StatementFactory<RB, R> selectFactory, StatementFactory<UB, U> updateFactory, StatementFactory<DB, D> deleteFactory) {
        setInsert(insertFactory);
        setSelect(selectFactory);
        setUpdate(updateFactory);
        setDelete(deleteFactory);
    }

    public CRUDStatementFactory() {
        this(null, null, null, null);
    }
    
    public void setInsert(StatementFactory<CB, C> insertFactory) {
        this.insertFactory = insertFactory != null ? insertFactory : NO_FACTORY;
    }
    
    public void setSelect(StatementFactory<RB, R> selectFactory) {
        this.selectFactory = selectFactory != null ? selectFactory : NO_FACTORY;
    }
    
    public void setUpdate(StatementFactory<UB, U> updateFactory) {
        this.updateFactory = updateFactory != null ? updateFactory : NO_FACTORY;
    }
    
    public void setDelete(StatementFactory<DB, D> deleteFactory) {
        this.deleteFactory = deleteFactory != null ? deleteFactory : NO_FACTORY;
    }
    
    public <CB1, C1> CRUDStatementFactory<CB1, RB, UB, DB, C1, R, U, D> withInsert(StatementFactory<CB1, C1> insertFactory) {
        CRUDStatementFactory<CB1, RB, UB, DB, C1, R, U, D> self = (CRUDStatementFactory) this;
        self.setInsert(insertFactory);
        return self;
    }
    
    public <RB1, R1> CRUDStatementFactory<CB, RB1, UB, DB, C, R1, U, D> withSelect(StatementFactory<RB1, R1> selectFactory) {
        CRUDStatementFactory<CB, RB1, UB, DB, C, R1, U, D> self = (CRUDStatementFactory) this;
        self.setSelect(selectFactory);
        return self;
    }
    
    public <UB1, U1> CRUDStatementFactory<CB, RB, UB1, DB, C, R, U1, D> withUpdate(StatementFactory<UB1, U1> updateFactory) {
        CRUDStatementFactory<CB, RB, UB1, DB, C, R, U1, D> self = (CRUDStatementFactory) this;
        self.setUpdate(updateFactory);
        return self;
    }
    
    public <DB1, D1> CRUDStatementFactory<CB, RB, UB, DB1, C, R, U, D1> withDelete(StatementFactory<DB1, D1> deleteFactory) {
        CRUDStatementFactory<CB, RB, UB, DB1, C, R, U, D1> self = (CRUDStatementFactory) this;
        self.setDelete(deleteFactory);
        return self;
    }
    
    public C insert(Template<? super CB> template, List<?> attributes) {
        return insertFactory.create(template, attributes);
    }
    
    public R select(Template<? super RB> template, List<?> attributes) {
        return selectFactory.create(template, attributes);
    }
    
    public U update(Template<? super UB> template, List<?> attributes) {
        return updateFactory.create(template, attributes);
    }
    
    public D delete(Template<? super DB> template, List<?> attributes) {
        return deleteFactory.create(template, attributes);
    }
    
    private static final StatementFactory NO_FACTORY = (t,a) -> {throw new UnsupportedOperationException();};
}
