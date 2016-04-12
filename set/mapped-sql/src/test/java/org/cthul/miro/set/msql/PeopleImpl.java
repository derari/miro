package org.cthul.miro.set.msql;

import org.cthul.miro.composer.template.TemplateLayer;
import org.cthul.miro.db.sql.SelectBuilder;
import org.cthul.miro.map.impl.QueryableEntitySet;
import org.cthul.miro.map.MappedStatement;

/**
 *
 */
public class PeopleImpl extends MappedSqlEntitySet<Person, PeopleImpl> implements People {

    public PeopleImpl(QueryableEntitySet<Person> entitySet, TemplateLayer<? super MappedStatement<Person, ? extends SelectBuilder>> templateLayer) {
        super(entitySet, templateLayer);
    }

    protected PeopleImpl(MappedSqlEntitySet<Person, PeopleImpl> source) {
        super(source);
    }
    
    @Override
    protected PeopleImpl copy() {
        return new PeopleImpl(this);
    }

    @Override
    public People withFirstName(String name) {
        return snippet(sql -> {
           sql.where().sql("first_name").eq(name);
        });
    }
}
