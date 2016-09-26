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
        return new Builder();
    }
    
    static Builder build(QlCode code) {
        return build().append(code);
    }
    
    static Fluent ql(String string) {
        return new PlainSyntax(string);
    }
    
    static Fluent id(String... id) {
        return new Identifier(id);
    }
    
    static Fluent str(String string) {
        return new StringLiteral(string);
    }
    
    static Fluent cnst(Object key) {
        return new Constant(key);
    }
    
    static Fluent lazy(Consumer<? super QlBuilder<?>> code) {
        return new Lazy(code);
    }
    
    class PlainSyntax implements Fluent {
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
    
    class PlainSyntaxBuilder implements Fluent {
        private final StringBuilder stringBuilder = new StringBuilder();
        private String s = null;
        
        public PlainSyntaxBuilder() {
        }
        
        public PlainSyntaxBuilder(String string) {
            stringBuilder.append(string);
        }

        @Override
        public Builder append(CharSequence query) {
            throw new UnsupportedOperationException();
        }
        
        public PlainSyntaxBuilder appendSyntax(CharSequence string) {
            s = null;
            stringBuilder.append(string);
            return this;
        }
        
        @Override
        public void appendTo(QlBuilder<?> qlBuilder) {
            qlBuilder.append(toString());
        }

        @Override
        public String toString() {
            if (s != null) return s;
            return s = stringBuilder.toString();
        }
    }
    
    class Identifier implements Fluent {
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
    
    class StringLiteral implements Fluent {
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
    
    class Constant implements Fluent {
        private final Object key;

        public Constant(Object key) {
            this.key = key;
        }

        @Override
        public void appendTo(QlBuilder<?> qlBuilder) {
            qlBuilder.constant(key);
        }

        @Override
        public String toString() {
            return "%" + key + "%";
        }
    }
    
    class Clause<C> implements Fluent {

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
    
    class Builder implements Fluent {
        
        private final List<QlCode> code = new ArrayList<>();
        private List<Object> args = null;
        private PlainSyntaxBuilder currentPlainSyntax = null;
        private boolean lastIsPlainSyntax = false;

        protected PlainSyntaxBuilder plainSyntaxBuilder() {
            if (currentPlainSyntax == null) {
                currentPlainSyntax = new PlainSyntaxBuilder();
                if (lastIsPlainSyntax) {
                    QlCode last = code.remove(code.size()-1);
                    currentPlainSyntax.appendSyntax(last.toString());
                }
                code.add(currentPlainSyntax);
                lastIsPlainSyntax = true;
            }
            return currentPlainSyntax;
        }
        
        protected void appendTo(Builder b) {
            currentPlainSyntax = null;
            lastIsPlainSyntax = false;
            code.forEach(b::append);
            if (args != null) b.args().addAll(args);
        }
        
        @Override
        public Builder append(QlCode c) {
            Class<?> clazz = c.getClass();
            if (clazz == Builder.class) {
                ((Builder) c).appendTo(this);
                return this;
            } else if (clazz == PlainSyntax.class || clazz == PlainSyntaxBuilder.class) {
                if (lastIsPlainSyntax) {
                    plainSyntaxBuilder().appendSyntax(c.toString());
                    return this;
                }
                lastIsPlainSyntax = true;
            } else {
                lastIsPlainSyntax = false;
                currentPlainSyntax = null;
            }
            code.add(c);
            return this;
        }

        @Override
        public Builder append(CharSequence query) {
            plainSyntaxBuilder().appendSyntax(query);
            return this;
        }

        protected List<Object> args() {
            if (args == null) args = new ArrayList<>();
            return args;
        }
        
        @Override
        public Builder pushArgument(Object arg) {
            args().add(arg);
            return this;
        }
        
        @Override
        public void appendTo(QlBuilder<?> qlBuilder) {
            code.forEach(c -> c.appendTo(qlBuilder));
            if (args != null) qlBuilder.pushArguments(args);
        }

        @Override
        public String toString() {
            return code.toString();
        }
    }
    
    class Lazy implements Fluent {
        
        private final Consumer<? super QlBuilder<?>> code;

        public Lazy(Consumer<? super QlBuilder<?>> code) {
            this.code = code;
        }

        @Override
        public void appendTo(QlBuilder<?> qlBuilder) {
            code.accept(qlBuilder);
        }
    }
    
    interface Fluent extends QlCode, QlBuilder<Builder> {
        
        @Override
        default Builder append(QlCode c) {
            return build(this).append(c);
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
        public default Builder constant(Object key) {
            return append(new Constant(key));
        }

        @Override
        default Builder pushArgument(Object arg) {
            return build(this).pushArgument(arg);
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
    
    class Var implements QlCode {
        
        private final static QlCode EMPTY = QlCode.ql("");
        private QlCode code;

        public Var() {
            this(EMPTY);
        }

        public Var(QlCode code) {
            this.code = code;
        }
        
        @Override
        public void appendTo(QlBuilder<?> qlBuilder) {
            code.appendTo(qlBuilder);
        }

        public void set(QlCode code) {
            this.code = code;
        }

        public QlCode get() {
            return code;
        }
        
        public QlCode.Var copy() {
            return new Var(code);
        }
    }
}
