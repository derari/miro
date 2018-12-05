package org.cthul.miro.sql.set;

import org.cthul.miro.db.MiConnection;
import org.cthul.miro.sql.map.MappedSelectRequest;
import static org.cthul.miro.sql.composer.Comparison.like;

/**
 *
 */
public class AddressDao extends SqlEntitySet<Address, AddressDao> {

    public AddressDao(MiConnection cnn, MappedSelectRequest<Address> composer) {
        super(cnn, composer);
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
