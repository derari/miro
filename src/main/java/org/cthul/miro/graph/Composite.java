package org.cthul.miro.graph;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.cthul.miro.MiConnection;
import org.cthul.miro.dml.MappedDataQueryTemplateProvider;
import org.cthul.miro.map.Mapping;
import org.cthul.miro.result.EntityBuilderBase;
import org.cthul.miro.result.EntityConfiguration;
import org.cthul.miro.result.EntityInitializer;

public class Composite<Entity, Other> implements GraphConfigurationProvider<Entity> {
    
    private final String[] foreignKey;
    private final MappedDataQueryTemplateProvider<Other> provider;

    public Composite(String[] foreignKey, MappedDataQueryTemplateProvider<Other> provider) {
        this.foreignKey = foreignKey;
        this.provider = provider;
    }

    @Override
    public <E extends Entity> EntityConfiguration<? super E> getConfiguration(MiConnection cnn, Mapping<E> mapping, Graph graph, Object[] args) {
        return new Config<>(cnn, mapping, graph);
    }
    
    protected class Config<E extends Entity> implements EntityConfiguration<E> {
        private final MiConnection cnn;
        private final Mapping<E> mapping;
        private final Graph graph;
        private final Object[] selectedFields;

        public Config(MiConnection cnn, Mapping<E> mapping, Graph graph, Object[] fields) {
            this.cnn = cnn;
            this.mapping = mapping;
            this.graph = graph;
            this.selectedFields = fields;
        }

        @Override
        public EntityInitializer<E> newInitializer(ResultSet rs) throws SQLException {
            return new Init(cnn, mapping, graph, selectedFields, rs);
        }
    }
    
    protected class Init<E extends Entity> extends EntityBuilderBase implements EntityInitializer<E> {
        private final MiConnection cnn;
        private final Mapping<E> mapping;
        private final Graph graph;
        private final Object[] selectedFields;
        private final ResultSet rs;
        private final Object[] tmp;
        private final int[] fkIndices;
        private GraphSelectQuery<Other> graphSelect = null;

        public Init(MiConnection cnn, Mapping<E> mapping, Graph graph, Object[] selectedFields, ResultSet rs) throws SQLException {
            this.cnn = cnn;
            this.mapping = mapping;
            this.graph = graph;
            this.selectedFields = selectedFields;
            this.rs = rs;
            tmp = new Object[foreignKey.length];
            fkIndices = getFieldIndices(rs, foreignKey);
        }

        @Override
        public void apply(E entity) throws SQLException {
            if (graphSelect == null) {
                graphSelect = new GraphSelectQuery<>(provider, graph, selectedFields);
            }
            getFields(rs, fkIndices, tmp);
            
        }

        @Override
        public void complete() throws SQLException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void close() throws SQLException {
        }
    }
}
