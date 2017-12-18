package org.cthul.miro.db;

import java.util.function.Function;
import org.cthul.miro.request.RequestComposer;
import org.cthul.miro.request.StatementPart;
import org.cthul.miro.request.impl.AbstractTemplateLayer;
import org.cthul.miro.request.impl.SimpleRequestComposer;
import org.cthul.miro.request.impl.ValueKey;
import org.cthul.miro.request.part.Copyable;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.request.template.InternalComposer;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.request.template.TemplateLayer;
import org.cthul.miro.request.template.TemplateStack;
import org.cthul.miro.util.Key;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 */
public class ComposerTest {
    
    public static class PartCopyReturnOnly extends AbstractSetBagValue {
        public PartCopyReturnOnly() {
        }
        public PartCopyReturnOnly(String value) {
            super(value);
        }
        @Override
        public Object copyFor(CopyComposer<StringBag> cc) {
            return new PartCopyReturnOnly(value);
        }
    }
    
    @Test
    public void test_copy_return_only() {
        test_consistency(ic -> new PartCopyReturnOnly(), BAG_VALUE_KEY);
    }
    
    public static class PartIcNeeded extends AbstractSetBagValue {
        InternalComposer<?> ic;

        public PartIcNeeded(InternalComposer<?> ic) {
            this.ic = ic;
        }
        
        public PartIcNeeded(InternalComposer<?> ic, String value) {
            super(value);
            this.ic = ic;
        }
        
        @Override
        public Object copyFor(CopyComposer<StringBag> cc) {
            InternalComposer<StringBag> ic1 = ic.node(cc);
            return new PartIcNeeded(ic1, value);
        }
    }
    
    @Test
    public void test_ic_needed() {
        test_consistency(PartIcNeeded::new, BAG_VALUE_KEY);
    }
    
    public static class PartAddSecondKey extends AbstractSetBagValue {
        InternalComposer<?> ic;

        @SuppressWarnings("LeakingThisInConstructor")
        public PartAddSecondKey(InternalComposer<?> ic) {
            this.ic = ic;
            ic.addNode(STRING_LIST_KEY, this);
        }
        
        public PartAddSecondKey(InternalComposer<?> ic, String value) {
            super(value);
            this.ic = ic;
        }
        
        @Override
        public Object copyFor(Copyable.CopyComposer<StringBag> cc) {
            InternalComposer<StringBag> ic1 = ic.node(cc);
            PartAddSecondKey copy = new PartAddSecondKey(ic1, value);
            return copy;
        }
    }
    
    @Test
    public void test_add_separate_node() {
        test_consistency(PartAddSecondKey::new, STRING_LIST_KEY);
    }
    
    public static void test_consistency(Function<InternalComposer<?>, StatementPart<StringBag>> newNodePart, Key<ListNode<String>> key) {
        RequestComposer<DoubleStringBag> cmp = composer(newNodePart);
        cmp.require(BAG_VALUE_KEY);
        cmp.node(key).add("test");
        DoubleStringBag dblBag = new DoubleStringBag();
        cmp.build(dblBag);
        assertThat(dblBag.bag1.value, is("test"));
        assertThat(dblBag.bag2.value, is("test"));
        cmp = cmp.copy();
        
        dblBag = new DoubleStringBag();
        cmp.build(dblBag);
        assertThat(dblBag.bag1.value, is("test"));
        assertThat(dblBag.bag2.value, is("test"));
        
        cmp.node(key).add("123");
        dblBag = new DoubleStringBag();
        cmp.build(dblBag);
        assertThat(dblBag.bag1.value, is("test123"));
        assertThat(dblBag.bag2.value, is("test123"));
    }
    
    public static RequestComposer<DoubleStringBag> composer(Function<InternalComposer<?>, StatementPart<StringBag>> newNodePart) {
        return new SimpleRequestComposer<>(buildTemplate(newNodePart));
    }
    
    public static Template<DoubleStringBag> buildTemplate(Function<InternalComposer<?>, StatementPart<StringBag>> newNodePart) {
        TemplateLayer<StringBag> stringBagLayer = new StringBagLayer(newNodePart);
        return new TemplateStack<>()
                .and(stringBagLayer.<DoubleStringBag>adapt(dbl -> dbl.bag1))
                .and(stringBagLayer.adapt(dbl -> dbl.bag2));
    }
    
    public static class DoubleStringBag {
        public StringBag bag1 = new StringBag(), bag2 = new StringBag();
    }
    
    public static class StringBag {
        public String value = "";
    }

    private static final Key<ListNode<String>> BAG_VALUE_KEY = new ValueKey<>("bag value");
    private static final Key<ListNode<String>> STRING_LIST_KEY = new ValueKey<>("string list");
    
    public static class StringBagLayer extends AbstractTemplateLayer<StringBag> {
        
        private final Function<InternalComposer<?>, StatementPart<StringBag>> newNodePart;

        public StringBagLayer(Function<InternalComposer<?>, StatementPart<StringBag>> newNodePart) {
            this.newNodePart = newNodePart;
        }

        @Override
        protected Template<? super StringBag> createPartTemplate(Parent<StringBag> parent, Object key) {
            if (key == BAG_VALUE_KEY) {
                return parent.andNewNodePart(newNodePart);
            }
            return null;
        }
    }
    
    public static abstract class AbstractSetBagValue implements Copyable<StringBag>, StatementPart<StringBag>, ListNode<String> {

        protected String value = "";

        public AbstractSetBagValue() {
        }

        public AbstractSetBagValue(String value) {
            this.value = value;
        }

        @Override
        public void addTo(StringBag builder) {
            builder.value = builder.value + getValue();
        }

        public String getValue() {
            return value;
        }

        @Override
        public void add(String t) {
            this.value = value + t;
        }
    }
    
    public static class StringListNode implements ListNode<String>, Copyable<Object> {

        public final StringBuilder sb = new StringBuilder();
        
        @Override
        public void add(String entry) {
            sb.append(entry);
        }

        @Override
        public Object copyFor(CopyComposer<Object> cc) {
            StringListNode copy = new StringListNode();
            copy.sb.append(sb);
            return copy;
        }
    }
}
