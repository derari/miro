package org.cthul.miro.composer.snippets;

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
