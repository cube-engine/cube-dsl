package net.cube.engine.dsl.manager;

import lombok.Getter;
import net.cube.engine.ConfigHelper;
import net.cube.engine.Configurable;
import net.cube.engine.CubeRuntimeException;
import net.cube.engine.ObjectHelper;
import net.cube.engine.dsl.DslCodec;
import net.cube.engine.manager.AbstractManager;

/**
 * @author pluto
 * @date 2022/5/31
 */
public class DslCodecManager extends AbstractManager {

    @Getter
    private DslCodec<?> codec;

    private static volatile DslCodecManager INSTANCE;

    private DslCodecManager() {
    }

    public static DslCodecManager getInstance() {
        if (INSTANCE == null) {
            synchronized (DslCodecManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DslCodecManager();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    protected void runStartup() throws Exception {
        Object config = ConfigHelper.getInstance().getConfig(getConfigKey());
        String className = ObjectHelper.getProperty(config, Configurable.FIXED_KEY_CLASS, String.class);
        if (className == null || "".equals(className)) {
            throw new CubeRuntimeException("Can not found \""
                    + getConfigKey()
                    + "\" in properties, make sure has been set.");
        }
        Class<?> clazz = Class.forName(className, false, this.getClass().getClassLoader());
        codec = (DslCodec<?>) clazz.newInstance();
    }

    @Override
    protected void runShutdown() throws Exception {
        if (codec == null) {
            return ;
        }
        codec = null;
    }

    @Override
    public String getConfigKey() {
        return "DslCodec";
    }
}
