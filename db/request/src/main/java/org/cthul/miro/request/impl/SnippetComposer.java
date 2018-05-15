package org.cthul.miro.request.impl;

import org.cthul.miro.request.template.Snippets;

/**
 *
 */
public interface SnippetComposer<Builder> {
    
    Snippets<Builder> getSnippets();
}
