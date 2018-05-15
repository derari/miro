package org.cthul.miro.sql.set;

import org.cthul.miro.request.template.Snippets;

/**
 *
 * @param <Builder>
 */
public interface SnippetComposer<Builder> {
    
    Snippets<Builder> getSnippets();
    
    interface Delegator<Builder> extends SnippetComposer<Builder> {
        
        SnippetComposer<Builder> getSnippetComposerDelegate();

        @Override
        default Snippets<Builder> getSnippets() {
            return getSnippetComposerDelegate().getSnippets();
        }
    }
}
