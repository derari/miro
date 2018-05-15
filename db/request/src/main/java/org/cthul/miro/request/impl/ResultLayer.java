package org.cthul.miro.request.impl;

import java.util.*;
import java.util.function.Predicate;
import org.cthul.miro.request.Composer;
import org.cthul.miro.request.ComposerKey;
import org.cthul.miro.request.ComposerKey.Phase;
import org.cthul.miro.request.ComposerKey.PhaseListener;
import org.cthul.miro.request.ResultKey;
import org.cthul.miro.request.part.Copyable;
import org.cthul.miro.request.part.ListNode;
import org.cthul.miro.request.template.Template;
import org.cthul.miro.request.template.Templates;
import org.cthul.miro.util.Key;

public class ResultLayer extends AbstractTemplateLayer<Object> {
    
    private final Key<ListNode<String>> resultKey;
    private final Collection<String> defaultKeys = new ArrayList<>();
    private final Collection<String> optionalKeys = new ArrayList<>();

    public ResultLayer(Key<ListNode<String>> resultKey) {
        this.resultKey = resultKey;
    }

    public Collection<String> getDefaultKeys() {
        return defaultKeys;
    }

    public Collection<String> getOptionalKeys() {
        return optionalKeys;
    }

    @Override
    protected Template<? super Object> createPartTemplate(Parent<Object> parent, Object key) {
        switch (ComposerKey.key(key)) {
            case PHASE: 
                return parent.and(PhaseListener.handle((c, p) -> {
                    if (p == Phase.BUILD) ((Results) c.node(ResultKey.RESULT)).build();
                }));
        }
        switch (ResultKey.key(key)) {
            case RESULT: 
                return Templates.newNode(Results::new);
            case DEFAULT:
                return Templates.setUp(ResultKey.RESULT, r -> ((Results) r).addDefaults());
            case OPTIONAL:
                return Templates.setUp(ResultKey.RESULT, r -> ((Results) r).addOptionals());
        }
        if (key instanceof String) {
            String s = (String) key;
            switch (s) {
                case "*":
                    return Templates.require(ResultKey.DEFAULT);
                case "**":
                    return Templates.require(ResultKey.OPTIONAL);
            }
//            if (defaultKeys.contains(s) || optionalKeys.contains(s)) {
//            return Templates.setUp(ResultKey.RESULT, r -> r.add(s));
//            }
        }
        return null;
    }
    
    private class Results implements ListNode<String>, Copyable<Object> {
        
        private final ListNode<String> resultNode;
        private boolean empty = true;

        public Results(Composer c) {
            this.resultNode = c.node(resultKey);
        }

        public Results(Composer c, boolean empty) {
            this.resultNode = c.node(resultKey);
            this.empty = empty;
        }

        @Override
        public void add(String entry) {
            empty = false;
            resultNode.add(entry);
        }

        @Override
        public Object copyFor(CopyComposer<Object> ic) {
            return new Results(ic, empty);
        }

        void addDefaults() {
            addAll(defaultKeys);
        }

        void addOptionals() {
            addAll(defaultKeys);
            addAll(optionalKeys);
        }

        void build() {
            if (empty) {
                addDefaults();
            }
        }

        @Override
        public boolean allowReadOnly(Predicate<Object> isLatest) {
            return true;
        }
    }
}
