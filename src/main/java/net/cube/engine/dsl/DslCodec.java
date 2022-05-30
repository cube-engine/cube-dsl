package net.cube.engine.dsl;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author pluto
 * @date 2022/5/30
 */
public interface DslCodec<T> {

    /**
     *
     * @param is
     * @return
     * @throws Exception
     */
    T decode(InputStream is) throws Exception;

    /**
     *
     * @param t
     * @param os
     * @throws Exception
     */
    void encode(T t, OutputStream os) throws Exception;
}
