package net.cube.engine.dsl.manager;

import net.cube.engine.Launcher;
import net.cube.engine.Plugin;
import net.cube.engine.annotation.Bootstrap;
import net.cube.engine.dsl.DslExtensional;
import net.cube.engine.manager.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author pluto
 * @date 2022/5/31
 */
@Bootstrap(priority = 2)
public class DslExtensionManager implements Launcher {

    private static Logger LOG = LoggerFactory.getLogger(DslExtensionManager.class);

    private Lock lock = new ReentrantLock();

    private Map<String, DslExtensional> registry = new HashMap<>(16);

    private volatile boolean started;

    private static volatile DslExtensionManager INSTANCE;

    private DslExtensionManager() {
    }

    public static DslExtensionManager getInstance() {
        if (INSTANCE == null) {
            synchronized (DslExtensionManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DslExtensionManager();
                }
            }
        }
        return INSTANCE;
    }

    public Collection<DslExtensional> getExtensions() {
        return registry.values();
    }

    @Override
    public void start() throws Exception {
        if (started) {
            LOG.warn("DslExtensionManager has been started. Do not need to be started once more.");
            return ;
        }
        start : if (lock.tryLock()) {
            try {
                if (started) {
                    break start;
                }
                Map<String, Plugin> plugins = PluginManager.getInstance().getPluginByType(DslExtensional.PLUGIN_TYPE);
                if (plugins == null || plugins.size() == 0) {
                    LOG.warn("Can not found any extensions for DSL.");
                    break start;
                }
                plugins.forEach((k, v) -> {
                    DslExtensional dslExtension = (DslExtensional) v;
                    registry.put(dslExtension.getName(), dslExtension);
                });
                started = true;
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void stop() throws Exception {
        if (!started) {
            LOG.warn("DslExtensionManager has been stopped. Do not need to stopped once more.");
            return;
        }
        stop : if (lock.tryLock()) {
            try {
                if (!started) {
                    break stop;
                }
                started = false;
                registry.clear();
            } finally {
                lock.unlock();
            }
        }
    }
}
