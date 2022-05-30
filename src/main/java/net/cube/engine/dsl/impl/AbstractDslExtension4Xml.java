package net.cube.engine.dsl.impl;

import com.sun.org.apache.bcel.internal.classfile.Code;
import net.cube.engine.CubeRuntimeException;
import net.cube.engine.dsl.DslExtensional;
import net.cube.engine.dsl.JaxbHelper;
import net.cube.engine.manager.CodeSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * @author pluto
 * @date 2022/5/31
 */
public abstract class AbstractDslExtension4Xml implements DslExtensional {

    private static Logger LOG = LoggerFactory.getLogger(AbstractDslExtension4Xml.class);

    @Override
    public List<XmlAdapter<?, ?>> getAdapters() {
        String packageName = this.getClass().getPackage().getName();
        ClassLoader classLoader = this.getClass().getClassLoader();
        String rootPackageRelativePath = packageName.replace(".", "/");
        Enumeration<URL> urls;
        File[] files = CodeSourceManager.getInstance().getCodeFiles();
        Vector<URL> codeFileUrls = new Vector<>();
        try {
            urls = classLoader.getResources(rootPackageRelativePath);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String path = url.getPath();
                for (File f : files) {
                    if (path.contains(f.getName())) {
                        codeFileUrls.add(f.toURI().toURL());
                    }
                }
            }
        } catch (IOException e) {
            throw new CubeRuntimeException(e);
        }
        return JaxbHelper.findXmlAdapters(packageName, classLoader, codeFileUrls.elements());
    }

    @Override
    public String getName() {
        String packageName = this.getClass().getPackage().getName();
        ClassLoader classLoader = this.getClass().getClassLoader();
        String rootPackageRelativePath = packageName.replace(".", "/");
        Enumeration<URL> urls;
        File[] files = CodeSourceManager.getInstance().getCodeFiles();
        Vector<URL> codeFileUrls = new Vector<>();
        try {
            urls = classLoader.getResources(rootPackageRelativePath);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String path = url.getPath();
                for (File f : files) {
                    if (path.contains(f.getName())) {
                        codeFileUrls.add(f.toURI().toURL());
                    }
                }
            }
        } catch (IOException e) {
            throw new CubeRuntimeException(e);
        }
        return JaxbHelper.assembleDefContextPath(packageName, classLoader, codeFileUrls.elements());
    }
}
