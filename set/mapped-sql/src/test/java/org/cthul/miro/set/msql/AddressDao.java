package org.cthul.miro.set.msql;

import org.cthul.miro.composer.template.TemplateLayer;
import org.cthul.miro.db.sql.SelectBuilder;
import org.cthul.miro.map.MappedStatement;
import org.cthul.miro.map.impl.QueryableEntitySet;

/**
 *
 */
public class AddressDao extends MappedSqlEntitySet<Address, AddressDao> {

    public AddressDao(QueryableEntitySet<Address> entitySet, TemplateLayer<? super MappedStatement<Address, ? extends SelectBuilder>> templateLayer) {
        super(entitySet, templateLayer);
    }

    protected AddressDao(MappedSqlEntitySet<Address, AddressDao> source) {
        super(source);
    }

    @Override
    protected void initialize() {
        super.initialize();
//        snippet(sql -> sql.select().sql("city"));
//        setUp(MappingKey.LOAD_FIELD, rs -> rs.add("city"));
    }
    
    public AddressDao byId(int id) {
        return snippet(sql -> sql.where().sql("id = ?", id));
    }
    
    public AddressDao cityLike(String pattern) {
        return snippet(sql -> sql.where().sql("city LIKE ?", pattern));
    }
}
