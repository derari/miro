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
        setUp(MappingKey.FETCH, "id", "firstName", "lastName");
    }

    @Override
    public People withFirstName(String name) {
        return setUp(MappingKey.PROPERTY_FILTER, "firstName", name);
//        return setUp(MappingKey.PROPERTY_FILTER, f -> f.forProperties("firstName").add(new Object[]{name}));
//        return sql(sql -> {
//           sql.where().ql("first_name").eq(name);
//        });
    }

    @Override
    public People withLastName(String name) {
        return setUp(MappingKey.PROPERTY_FILTER, "lastName", name);
//        return setUp(MappingKey.PROPERTY_FILTER, f -> f.forProperties("firstName").add(new Object[]{name}));
//        return sql(sql -> {
//           sql.where().ql("first_name").eq(name);
//        });
    }

    @Override
    public People withName(String first, String last) {
        return setUp(MappingKey.PROPERTY_FILTER, "firstName", first, "lastName", last);
    }
}
