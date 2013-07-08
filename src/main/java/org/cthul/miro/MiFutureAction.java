package org.cthul.miro;

public interface MiFutureAction<Argument, Result> {

    Result call(Argument arg) throws Exception;
}
