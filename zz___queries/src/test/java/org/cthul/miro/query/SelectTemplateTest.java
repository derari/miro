package org.cthul.miro.query;

import org.cthul.miro.composer.AbstractQueryComposer;
import org.cthul.miro.composer.StatementHolder;
import org.cthul.miro.composer.QueryPart;
import org.cthul.miro.composer.sql.SelectTemplate;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.db.sql.SelectQueryBuilder;
import org.cthul.miro.db.sql.syntax.AnsiSqlSyntax;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class SelectTemplateTest {

    SelectTemplate selectTemplate;
    
    @Before
    public void setUp() {
        
    }
    
    @Test
    public void test() {
//        SelectComposer cmp = new SelectComposer(selectTemplate.adapt(SelectBuilder::getStatement));
    }
    
    static class SelectComposer extends AbstractQueryComposer<SelectBuilder> {

        public SelectComposer(Template<? super SelectBuilder> template) {
            super(template);
        }

        @Override
        public String toString() {
            SelectBuilder builder = new SelectBuilder();
            buildStatement(builder);
            return builder.toString();
        }
    }
    
    static class SelectBuilder implements StatementHolder<SelectQueryBuilder> {
        SelectQueryBuilder builder = new AnsiSqlSyntax().newSelectQuery(null);

        @Override
        public SelectQueryBuilder getStatement() {
            return builder;
        }

        @Override
        public String toString() {
            return builder.toString();
        }
    }
    
}
