package org.cthul.miro.sql.set;

import org.cthul.miro.db.MiConnection;
import static org.cthul.miro.sql.template.AttributeFilter.like;

/**
 *
 */
public class AddressDao extends SqlEntitySet<Address, AddressDao> {

    public AddressDao(MiConnection cnn, MappedSqlType<Address> type) {
        super(cnn, type);
    }

    protected AddressDao(SqlEntitySet<Address, AddressDao> source) {
        super(source);
    }

    @Override
    protected void initialize() {
        super.initialize();
        setUp(FETCH, "city", "street");
    }
    
    public AddressDao byId(int id) {
        return sql(sql -> sql.where("id = ?", id));
    }
    
    public AddressDao cityLike(String pattern) {
        return setUp(PROPERTY_FILTER, "city", like(pattern));
    }
}
