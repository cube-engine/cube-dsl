package net.cube.engine.dsl.impl;

import net.cube.engine.Registry;
import net.cube.engine.dsl.DslInitializer;

/**
 * @author pluto
 * @date 2022/5/31
 */
public abstract class AbstractDslInitializer<REGISTRY extends Registry<?>> implements DslInitializer<REGISTRY> {

    protected REGISTRY registry;

    @Override
    public void setRegistry(REGISTRY registry) {
        this.registry = registry;
    }
}
