package net.cube.engine.dsl.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author pluto
 * @date 2022/5/31
 */
public class DefaultLocalRegistry<T> extends AbstractDslRegistry<T> {
    public DefaultLocalRegistry(Map<String, Object> config) {
        super(config);
    }

    @Override
    public T get(String tenant, String key, String... index) {
        return null;
    }

    @Override
    public Collection<T> getAll() {
        return null;
    }

    @Override
    public List<T> getAll(String tenant, String... index) {
        return null;
    }

    @Override
    public boolean set(String tenant, String key, T t, boolean isImmutable, String... index) {
        return false;
    }

    @Override
    public Long remove(String tenant, String key, String... index) {
        return null;
    }
}
