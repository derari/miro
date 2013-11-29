package org.cthul.miro.map;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.cthul.miro.MiConnection;
import org.cthul.miro.MiFuture;
import org.cthul.miro.MiFutureAction;
import org.cthul.miro.query.AbstractQuery;
import org.cthul.miro.query.adapter.JdbcQuery;
import org.cthul.miro.query.api.QueryType;
import org.cthul.miro.query.template.QueryTemplate;
import org.cthul.miro.result.CombinedEntityConfig;
import org.cthul.miro.result.EntityConfiguration;

public class AbstractMappedQuery<Entity> extends AbstractQuery {
    
    private final Mapping<Entity> mapping;

    public AbstractMappedQuery(QueryType<?> queryType, Mapping<Entity> mapping) {
        super(queryType);
        this.mapping = mapping;
    }

    public AbstractMappedQuery(QueryType<?> queryType, QueryTemplate template) {
        super(queryType, template);
        this.mapping = null;
    }

    public AbstractMappedQuery(QueryType<?> type, MappedTemplateProvider<Entity> templateProvider) {
        super(type, templateProvider);
        this.mapping = templateProvider.getMapping();
    }
    
    protected ResultSet executeJdbc(MiConnection cnn) throws SQLException {
        JdbcQuery<?> query = getAdapter(cnn.getJdbcAdapter());
        return cnn.execute(query);
    }
    
    protected MiFuture<ResultSet> submitJdbc(final MiConnection cnn) {
        MiFutureAction<AbstractMappedQuery<?>, ResultSet> exec = new MiFutureAction<AbstractMappedQuery<?>, ResultSet>() {
            @Override
            public ResultSet call(AbstractMappedQuery<?> arg) throws Exception {
                return arg.executeJdbc(cnn);
            }
        };
        return cnn.submit(exec, this);
    }
    
    protected EntityConfiguration<? super Entity> getConfiguration(MiConnection cnn) {
        List<EntityConfiguration<? super Entity>> configs = new ArrayList<>();
        for (Object o: getParts()) {
            if (o instanceof EntityConfiguration) {
                configs.add((EntityConfiguration<Entity>) o);
            }
        }
        return CombinedEntityConfig.combine(configs);
    }
}
