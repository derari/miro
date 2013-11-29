package org.cthul.miro.graph;

import java.util.List;
import org.cthul.miro.map.z.SubmittableQuery;

/**
 *
 * @author Arian Treffer
 */
public interface SelectByKey<Entity> {
    
    SelectByKey<Entity> into(Graph graph, Entity... values);
    
    SelectByKey<Entity> byKeys(Graph graph, Object... keys);
    
    SubmittableQuery<Entity[]> asOrderedArray();
    
    SubmittableQuery<List<Entity>> asList();
    
}
