package org.cthul.miro.db.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * A piece of code that can be appended to a {@link QlBuilder}.
 */
public interface QlCode extends Consumer<QlBuilder<?>> {

    @Override
    default void accept(QlBuilder<?> t) {
        appendTo(t);
    }

    void appendTo(QlBuilder<?> qlBuilder);
    
    static Builder build() {
        return new BuilderImpl();
    }
    
    static Builder ql(String string) {
        return new PlainSyntax(string);
    }
    
    static Builder id(String... id) {
        return new Identifier(id);
    }
    
    static Builder str(String string) {
        return new StringLiteral(string);
    }
    
    static Builder lazy(Consumer<? super QlBuilder<?>> code) {
        return new Lazy(code);
    }
    
    class PlainSyntax implements Builder {
        private final String string;

        public PlainSyntax(String string) {
            this.string = string;
        }
        
        @Override
        public void appendTo(QlBuilder<?> qlBuilder) {
            qlBuilder.append(string);
        }

        @Override
        public String toString() {
            return string;
        }
    }
    
    class Identifier implements Builder {
        private final String[] id;

        public Identifier(String... id) {
            this.id = id;
        }

        @Override
        public void appendTo(QlBuilder<?> qlBuilder) {
            qlBuilder.id(id);
        }

        @Override
        public String toString() {
            return Arrays.toString(id);
        }
    }
    
    class StringLiteral implements Builder {
        private final String string;

        public StringLiteral(String string) {
            this.string = string;
        }

        @Override
        public void appendTo(QlBuilder<?> qlBuilder) {
            qlBuilder.stringLiteral(string);
        }

        @Override
        public String toString() {
            return '"' + string + '"';
        }
    }
    
    class Clause<C> implements Builder {

        private final ClauseType<C> type;
        private final Consumer<? super C> code;

        public Clause(ClauseType<C> type, Consumer<? super C> code) {
            this.type = type;
            this.code = code;
        }
        @Override
        public void appendTo(QlBuilder<?> qlBuilder) {
            code.accept(qlBuilder.begin(type));
        }

        @Override
        public String toString() {
            return type + "-Clause@" + Integer.toHexString(System.identityHashCode(this));
        }
    }
    
    class BuilderImpl implements Builder {
        
        private final List<QlCode> code = new ArrayList<>();

        @Override
        public Builder append(QlCode c) {
            code.add(c);
            return this;
        }
        
        @Override
        public void appendTo(QlBuilder<?> qlBuilder) {
            code.forEach(c -> c.appendTo(qlBuilder));
        }

        @Override
        public String toString() {
            return code.toString();
        }
    }
    
    class Lazy implements Builder {
        
        private final Consumer<? super QlBuilder<?>> code;

        public Lazy(Consumer<? super QlBuilder<?>> code) {
            this.code = code;
        }

        @Override
        public void appendTo(QlBuilder<?> qlBuilder) {
            code.accept(qlBuilder);
        }
    }
    
    interface Builder extends QlCode, QlBuilder<Builder> {
        
        @Override
        default Builder append(QlCode c) {
            return build().append(this).append(c);
        }
        
        @Override
        void appendTo(QlBuilder<?> qlBuilder);

        @Override
        default Builder append(CharSequence query) {
            return append(new PlainSyntax(query.toString()));
        }

        @Override
        default Builder identifier(String id) {
            return append(new Identifier(id));
        }

        @Override
        default Builder id(String... id) {
            return append(new Identifier(id));
        }

        @Override
        default Builder stringLiteral(String string) {
            return append(new StringLiteral(string));
        }

        @Override
        default Builder pushArgument(Object arg) {
            throw new UnsupportedOperationException();
        }

        @Override
        default <Clause> Builder clause(ClauseType<Clause> type, Consumer<? super Clause> code) {
            return append(new QlCode.Clause<>(type, code));
        }

        @Override
        default <Clause> Clause begin(ClauseType<Clause> type) {
            throw new UnsupportedOperationException();
        }
    }
}
