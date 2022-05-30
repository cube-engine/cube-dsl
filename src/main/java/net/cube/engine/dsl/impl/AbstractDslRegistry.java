package net.cube.engine.dsl.impl;

import lombok.Getter;
import net.cube.engine.Configurable;
import net.cube.engine.CubeRuntimeException;
import net.cube.engine.ObjectHelper;
import net.cube.engine.dsl.DslInitializer;
import net.cube.engine.impl.AbstractRegistry;

import java.util.Map;

/**
 * @author pluto
 * @date 2022/5/31
 */
public abstract class AbstractDslRegistry<T> extends AbstractRegistry<T> {

    public static final String INITIALIZER_CONFIG_KEY = "initializer";

    protected DslInitializer<AbstractDslRegistry<T>> initializer;

    @Getter
    protected Class<T> clazz;

    public AbstractDslRegistry(Map<String, Object> config) {
        super(config);
    }

    @Override
    protected void init() throws CubeRuntimeException {
        String className = ObjectHelper.toObject(config.get(Configurable.FIXED_KEY_CLASS), String.class);
        if (className == null || "".equals(className)) {
            throw new CubeRuntimeException("\"class\" can not be empty. You must specify a class which represents a meta element for cube.");
        }
        try {
            clazz = (Class<T>) Class.forName(className, false, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new CubeRuntimeException("Registry initialized error.", e);
        }
        Object initializerConfig = config.get(INITIALIZER_CONFIG_KEY);
        if (initializerConfig == null) {
            return;
        }
        String initializerClassName = ObjectHelper.getProperty(initializerConfig, Configurable.FIXED_KEY_CLASS, String.class);
        if (initializerClassName == null || "".equals(initializerClassName)) {
            return;
        }
        try {
            Class<?> initializerClazz = Class.forName(initializerClassName, false, this.getClass().getClassLoader());
            initializer = (DslInitializer<AbstractDslRegistry<T>>) initializerClazz.newInstance();
            initializer.setRegistry(this);
            initializer.init(initializerConfig);
        } catch (Exception e) {
            throw new CubeRuntimeException("Registry iniialized error.", e);
        }
    }
}
