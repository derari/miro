/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cthul.miro.db.sql;

import org.cthul.miro.db.MiResultSet;
import org.cthul.miro.db.MiException;
import org.cthul.miro.futures.MiAction;
import org.cthul.miro.futures.MiFuture;

/**
 *
 */
public interface SqlStatement {
    
    MiResultSet execute() throws MiException;
    
    MiAction<MiResultSet> asAction();
    
    default MiFuture<MiResultSet> submit() {
        return asAction().submit();
    }
}
