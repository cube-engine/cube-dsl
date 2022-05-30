package net.cube.engine.dsl;

import net.cube.engine.CubeRuntimeException;
import net.cube.engine.FileHelper;
import org.w3c.dom.Document;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author pluto
 * @date 2022/5/30
 */
public final class JaxbHelper {

    public static String assembleDefContextPath(String rootPackageName, ClassLoader classLoader, Enumeration<URL> enumeration) throws RuntimeException {
        if (rootPackageName == null || "".equals(rootPackageName)) {
            return "";
        }
        String rootPackageRelativePath = rootPackageName.replace(".", "/");
        if (enumeration == null || !enumeration.hasMoreElements()) {
            try {
                enumeration = classLoader.getResources(rootPackageName);
            } catch (IOException e) {
                throw new CubeRuntimeException(e);
            }
        }
        StringBuilder result = new StringBuilder(16);

        while (enumeration.hasMoreElements()) {
            URL url = enumeration.nextElement();
            if (FileHelper.PROTOCOL_JAR.equalsIgnoreCase(url.getProtocol()) || FileHelper.isJar(url)) {
                String filePath = url.getPath();
                int intraPathIndex = filePath.indexOf("!");
                int nestProtocolIndex = filePath.lastIndexOf(":");
                filePath = filePath.substring(nestProtocolIndex == -1 ? 0 : nestProtocolIndex,
                        intraPathIndex == -1 ? filePath.length() : intraPathIndex);
                JarFile jarFile;
                try {
                    jarFile = new JarFile(new File(filePath));
                } catch (IOException e) {
                    throw new CubeRuntimeException(e);
                }
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.getName().contains(rootPackageName) || !entry.getName().contains("package-info.class")) {
                        continue;
                    }
                    String currentPackageName = entry.getName().substring(0, entry.getName().lastIndexOf("/"));
                    currentPackageName = currentPackageName.replace("/", ".");
                    result.append(currentPackageName);
                    result.append(":");
                }
            } else {
                File file;
                try {
                    file = new File(url.toURI());
                } catch (URISyntaxException e) {
                    throw new CubeRuntimeException(e);
                }
                final List<File> processStack = new LinkedList<>();
                processStack.add(0, file);
                while (processStack.size() > 0) {
                    File currentFile = processStack.remove(0);
                    if (currentFile.isDirectory()) {
                        processStack.addAll(0, Arrays.asList(currentFile.listFiles()));
                        continue;
                    }
                    if (!"package-info.class".equals(currentFile.getName())) {
                        continue;
                    }
                    String relativeFilePath = currentFile.getParent().replace(file.getAbsolutePath(), "");
                    String currentPackageName = (rootPackageRelativePath + relativeFilePath).replace("/", ".");
                    result.append(currentPackageName);
                    result.append(":");
                }
            }
        }
        return result.length() == 0 ? result.toString() : result.substring(0, result.length() - 1);
    }

    public static List<XmlAdapter<?, ?>> findXmlAdapters(String packageName, ClassLoader classLoader, Enumeration<URL> enumeration) {
        if (packageName == null || "".equals(packageName)) {
            return Collections.EMPTY_LIST;
        }
        final String relativePackagePath = packageName.replace(".", "/");
        if (enumeration == null || !enumeration.hasMoreElements()) {
            try {
                enumeration = classLoader.getResources(relativePackagePath);
            } catch (IOException e) {
                throw new CubeRuntimeException(e);
            }
        }
        List<String> matchedClassNames = new LinkedList<>();
        String protocol;
        while (enumeration.hasMoreElements()) {
            URL url = enumeration.nextElement();
            protocol = url.getProtocol();
            switch (protocol) {
                case FileHelper.PROTOCOL_FILE : {
                    File file;
                    try {
                        file = new File(url.toURI());
                    } catch (URISyntaxException e) {
                        throw new CubeRuntimeException(e);
                    }
                    final List<File> processStack = new LinkedList<>();
                    processStack.add(0, file);
                    while (processStack.size() > 0) {
                        File currentFile = processStack.remove(0);
                        if (currentFile.isDirectory()) {
                            processStack.addAll(0, Arrays.asList(currentFile.listFiles()));
                            continue;
                        }
                        String extensionName = currentFile.getName().substring(currentFile.getName().lastIndexOf(".") + 1);
                        if (extensionName == null || !"class".equals(extensionName)) {
                            continue;
                        }
                        String relativeFilePath = currentFile.getAbsolutePath().replace(file.getAbsolutePath(), "");
                        relativeFilePath = relativePackagePath.substring(0, relativeFilePath.lastIndexOf("."));
                        String currentClassName = (relativePackagePath + relativeFilePath).replace("/", ".");
                        matchedClassNames.add(currentClassName);
                    }
                }
                break;
                case FileHelper.PROTOCOL_JAR : {
                    String filePath = url.getPath();
                    filePath = filePath.substring(0, filePath.indexOf("!"));
                    JarFile jarFile;
                    try {
                        jarFile = new JarFile(new File(URI.create(filePath)));
                    } catch (IOException e) {
                        throw new CubeRuntimeException(e);
                    }
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String extensionName = entry.getName().substring(entry.getName().lastIndexOf(".") + 1);
                        if (extensionName == null || !"class".equals(extensionName)) {
                            continue;
                        }
                        String currentClassName = entry.getName().substring(0, entry.getName().lastIndexOf("."));
                        currentClassName = currentClassName.replace("/", ".");
                        matchedClassNames.add(currentClassName);
                    }
                }
                break;
                default :
                    throw new IllegalArgumentException("No support by protocol " + protocol);
            }
        }
        List<XmlAdapter<?, ?>> adapters = new LinkedList<>();
        matchedClassNames.forEach(className -> {
            try {
                Class clazz = Class.forName(className, false, classLoader);
                Class superClazz = clazz.getSuperclass();
                if (XmlAdapter.class.equals(superClazz)) {
                    adapters.add((XmlAdapter)clazz.newInstance());
                }
            } catch (Exception e) {
                throw new CubeRuntimeException(e);
            }
        });
        return adapters;
    }

    public static Document toDOMDocument(InputStream is) throws Exception {
        DocumentBuilder builder = createDocumentBuilderFactory().newDocumentBuilder();
        return builder.parse(is);
    }

    public static DocumentBuilderFactory createDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        return factory;
    }

}
