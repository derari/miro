package org.cthul.miro.set.msql;

import org.cthul.miro.composer.template.TemplateLayer;
import org.cthul.miro.db.sql.SelectBuilder;
import org.cthul.miro.map.MappedStatement;
import org.cthul.miro.map.MappingKey;
import org.cthul.miro.map.impl.QueryableEntitySet;

/**
 *
 */
public class PeopleDao2 extends MappedSqlEntitySet<Person, PeopleDao2> {

    public PeopleDao2(QueryableEntitySet<Person> entitySet, TemplateLayer<? super MappedStatement<Person, ? extends SelectBuilder>> templateLayer) {
        super(entitySet, templateLayer);
    }

    public PeopleDao2(MappedSqlEntitySet<Person, PeopleDao2> source) {
        super(source);
    }

    @Override
    protected void initialize() {
        super.initialize();
        snippet(sql -> sql.from().sql("People p"));
    }
    
    public PeopleDao2 selectFirstName() {
        return snippet(sql -> sql.select().sql("p.first_name AS `firstName`"))
               .setUp(MappingKey.LOAD_FIELD, lf -> lf.add("firstName"));
    }
    
    public PeopleDao2 withId(int id) {
        return snippet(sql -> sql.where().sql("p.id = ?", id));
    }

    public PeopleDao2 selectLastName() {
        return snippet(sql -> sql.select().sql("p.last_name AS `lastName`"))
               .setUp(MappingKey.LOAD_FIELD, lf -> lf.add("lastName"));
    }
    

}
