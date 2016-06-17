package org.cthul.miro.sql.set;

import org.cthul.miro.request.template.TemplateLayer;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.map.MappingKey;
import org.cthul.miro.map.layer.MappedQuery;

/**
 *
 */
public class AddressDao extends SqlEntitySet<Address, AddressDao> {

    public AddressDao(MiConnection cnn, TemplateLayer<MappedQuery<Address, SelectQuery>> queryLayer) {
        super(cnn, queryLayer);
    }

    protected AddressDao(SqlEntitySet<Address, AddressDao> source) {
        super(source);
    }

    @Override
    protected void initialize() {
        super.initialize();
        setUp(MappingKey.FETCH, "city", "street");
    }
    
    public AddressDao byId(int id) {
        return sql(sql -> sql.where().sql("id = ?", id));
    }
    
    public AddressDao cityLike(String pattern) {
        return sql(sql -> sql.where().sql("city LIKE ?", pattern));
    }
}
