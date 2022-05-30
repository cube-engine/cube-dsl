package net.cube.engine.dsl;

import net.cube.engine.Registry;

/**
 * @author pluto
 * @date 2022/5/30
 */
public interface DslInitializer<REGISTRY extends Registry<?>> {

    /**
     *
     * @param object
     */
    void init(Object object);

    /**
     *
     * @param registry
     */
    void setRegistry(REGISTRY registry);

}
