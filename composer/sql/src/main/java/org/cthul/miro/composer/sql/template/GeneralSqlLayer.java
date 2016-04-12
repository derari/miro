package org.cthul.miro.composer.sql.template;

import org.cthul.miro.composer.ListNode;
import org.cthul.miro.composer.MapNode;
import org.cthul.miro.composer.template.Template;
import org.cthul.miro.db.sql.SqlFilterableClause;
import org.cthul.miro.composer.sql.SqlComposerKey;
import org.cthul.miro.composer.template.Templates;

/**
 * Maps the public {@link SqlComposerKey}s to internal {@link ViewComposer} methods.
 * @param <Builder>
 */
public class GeneralSqlLayer<Builder extends SqlFilterableClause> extends AbstractSqlLayer<Builder> {

    public GeneralSqlLayer(SqlTemplates owner) {
        super(owner);
    }

    @Override
    protected String getShortString() {
        return "SQL-misc";
    }
    
    @Override
    protected Template<? super Builder> createPartTemplate(Parent<Builder> parent, Object key) {
        switch (SqlComposerKey.key(key)) {
            case ATTRIBUTES:
                return ListNode.template(ic -> {
                    MapNode<String,?> cmp = ic.node(getOwner().getMainKey());
                    return cmp::get;
                });
            case SNIPPETS:
                return Templates.link(getOwner().getMainKey());
        }
        return null;
    }
//
//    protected class FindByKeys implements StatementPart<SqlFilterableClause>, ListNode<Object[]> {
//
//        private final List<Object[]> keys = new ArrayList<>();
//        
//        @Override
//        public void addTo(SqlFilterableClause builder) {
//            final int len = getOwner().getKeyAttributes().size();
//            SqlClause.Where<?> where = builder.where();
//            if (len == 1) {
//                where
//                    .ql(getOwner().getKeyAttributes().get(0).expression())
//                    .in().list(keys.stream().map(k -> k[0]));
//            } else {
//                SqlClause.Junction<?> j = where.either();
//                keys.forEach(k -> {
//                    SqlClause.Conjunction<?> c = j.or().all();
//                    for (int i = 0; i < len; i++) {
//                        c.ql(getOwner().getKeyAttributes().get(i).expression())
//                         .ql(" = ?")
//                         .pushArgument(k[i]);
//                    }
//                });
//            }
//        }
//
//        @Override
//        public void add(Object[] entry) {
//            this.keys.add(entry);
//        }
//    }
}
