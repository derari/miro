package org.cthul.miro.composer.template;

/**
 *
 * @param <Builder>
 */
public interface QueryPartType<Builder> extends Template<Builder> {

    @Override
    void addTo(Object key, InternalQueryComposer<? extends Builder> query);
}
