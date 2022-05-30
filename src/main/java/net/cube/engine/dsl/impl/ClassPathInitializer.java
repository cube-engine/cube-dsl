package net.cube.engine.dsl.impl;

import net.cube.engine.CubeRuntimeException;
import net.cube.engine.FileHelper;
import net.cube.engine.Named;
import net.cube.engine.ObjectHelper;
import net.cube.engine.Tenantable;
import net.cube.engine.dsl.manager.DslCodecManager;
import net.cube.engine.manager.CodeSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author pluto
 * @date 2022/5/31
 */
public class ClassPathInitializer<T> extends AbstractDslInitializer<DefaultLocalRegistry<T>> {

    private static Logger LOG = LoggerFactory.getLogger(ClassPathInitializer.class);

    private static final String LOCAL_PATH = "localPath";

    @Override
    @SuppressWarnings("unchecked")
    public void init(Object object) {
        String localDslPath = ObjectHelper.getProperty(object, LOCAL_PATH, String.class);
        init:
        try {
            if (localDslPath == null || "".equals(localDslPath)) {
                break init;
            }
            Enumeration<URL> originInvolveUrls = this.getClass().getClassLoader().getResources(localDslPath);
            if (originInvolveUrls == null || !originInvolveUrls.hasMoreElements()) {
                break init;
            }
            File[] files = CodeSourceManager.getInstance().getCodeFiles();
            String scanPath = localDslPath.endsWith("/") ? localDslPath.substring(0, localDslPath.length() - 1) : localDslPath;
            while (originInvolveUrls.hasMoreElements()) {
                URL url = originInvolveUrls.nextElement();
                String path = url.getPath();
                for (File f : files) {
                    if (!path.contains(f.getName())) {
                        continue;
                    }
                    LOG.info("[{}] :: Scan file is [{}]. Current url is [{}].", this.getClass().getSimpleName(), f.toURI().toURL(), url);
                    if (FileHelper.PROTOCOL_JAR.equalsIgnoreCase(url.getProtocol()) || FileHelper.isJar(url)) {
                        JarFile jarFile;
                        try {
                            jarFile = new JarFile(f);
                        } catch (IOException e) {
                            throw new CubeRuntimeException(e);
                        }
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            if (!entry.getName().contains(localDslPath) || !entry.getName().endsWith(".xml")) {
                                LOG.info("[{}] :: Current file entry is {}.", this.getClass().getSimpleName(), entry.getName());
                                continue;
                            }
                            LOG.info("[{}] :: Current file entry is {}.", this.getClass().getSimpleName(), entry.getName());
                            try (InputStream is = jarFile.getInputStream(entry)) {
                                AbstractDslDefinition obj = (AbstractDslDefinition) DslCodecManager.getInstance().getCodec().decode(is);
                                LOG.info("[{}] :: Current class of obj is {}.", this.getClass().getSimpleName(), obj.getClass().getName());
                                if (isInstance(obj)) {
                                    processDefinition((T) obj);
                                }
                            } catch (Exception e) {
                                throw new CubeRuntimeException(e);
                            }
                        }
                    } else {
                        final List<File> processStack = new LinkedList<>();
                        processStack.add(0, f);
                        while (processStack.size() > 0) {
                            File currentFile = processStack.remove(0);
                            if (currentFile.isDirectory()) {
                                processStack.addAll(0, Arrays.asList(currentFile.listFiles()));
                                continue;
                            }
                            String currentFilePath = currentFile.getAbsolutePath();
                            currentFilePath = currentFilePath.endsWith("/") ? currentFilePath.substring(0, currentFilePath.length() - 1) : currentFilePath;
                            if (!currentFilePath.contains(localDslPath) || !currentFilePath.endsWith(".xml")) {
                                continue;
                            }
                            try (InputStream is = new FileInputStream(currentFile)) {
                                AbstractDslDefinition obj = (AbstractDslDefinition) DslCodecManager.getInstance().getCodec().decode(is);
                                LOG.info("[{}] :: Current class of object is {}.", this.getClass().getSimpleName(), obj.getClass().getName());
                                if (isInstance(obj)) {
                                    processDefinition((T) obj);
                                }
                            } catch (Exception e) {
                                throw new CubeRuntimeException(e);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new CubeRuntimeException(e);
        }
    }

    protected boolean isInstance(AbstractDslDefinition obj) {
        return registry.getClazz().isInstance(obj);
    }

    protected void processDefinition(T t) {
        if (t instanceof Tenantable && t instanceof Named) {
            registry.set(((Tenantable)t).getTenant(), ((Named)t).getName(), t, false);
        }
    }

}
