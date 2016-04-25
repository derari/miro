package org.cthul.miro.sql.set;

import org.cthul.miro.request.template.TemplateLayer;
import org.cthul.miro.db.MiConnection;
import org.cthul.miro.sql.SelectQuery;
import org.cthul.miro.map.MappingKey;
import org.cthul.miro.map.layer.MappedQuery;

/**
 *
 */
public class PeopleImpl extends SqlEntitySet<Person, PeopleImpl> implements People {

    public PeopleImpl(MiConnection cnn, TemplateLayer<MappedQuery<Person, SelectQuery>> queryLayer) {
        super(cnn, queryLayer);
    }

    protected PeopleImpl(SqlEntitySet<Person, PeopleImpl> source) {
        super(source);
    }

    @Override
    protected void initialize() {
        super.initialize();
        setUp(MappingKey.LOAD, load -> load.addAll("firstName", "lastName"));
    }

    @Override
    public People withFirstName(String name) {
        return sql(sql -> {
           sql.where().sql("first_name").eq(name);
        });
    }
}
