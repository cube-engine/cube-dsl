package net.cube.engine.dsl.impl;

import net.cube.engine.CubeRuntimeException;
import net.cube.engine.dsl.DslCodec;
import net.cube.engine.dsl.DslExtensional;
import net.cube.engine.dsl.JaxbHelper;
import net.cube.engine.dsl.manager.DslExtensionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pluto
 * @date 2022/5/31
 */
public class DslXmlCodecImpl implements DslCodec<AbstractDslDefinition> {

    private static Logger LOG = LoggerFactory.getLogger(DslXmlCodecImpl.class);

    protected String contextPath;

    protected JAXBContext context;

    public DslXmlCodecImpl() {
        this(null);
    }

    public DslXmlCodecImpl(String contextPath) {
        this.contextPath = contextPath == null ? "" : contextPath;
        resetContextPath();
        try {
            context = JAXBContext.newInstance(this.contextPath, this.getClass().getClassLoader());
        } catch (JAXBException e) {
            throw new CubeRuntimeException(e);
        }
    }

    protected void resetContextPath() {
        Collection<DslExtensional> extensions = DslExtensionManager.getInstance().getExtensions();
        if (extensions == null || extensions.size() == 0) {
            return ;
        }
        extensions.forEach(extension -> this.contextPath = this.contextPath.concat(":").concat(extension.getName()));
    }

    public void setXmlAdapters(Object object) {
        Collection<DslExtensional> extensions = DslExtensionManager.getInstance().getExtensions();
        if (extensions == null || extensions.size() == 0) {
            return ;
        }
        extensions.forEach(extension -> {
            if (extension.getAdapters() == null || extension.getAdapters().size() == 0) {
                return ;
            }
            extension.getAdapters().forEach(adapter -> {
                if (object instanceof Marshaller) {
                    ((Marshaller)object).setAdapter(adapter);
                }
                if (object instanceof Unmarshaller) {
                    ((Unmarshaller)object).setAdapter(adapter);
                }
            });
        });
    }

    @Override
    public AbstractDslDefinition decode(InputStream is) throws Exception {
        if (is == null) {
            throw new IllegalArgumentException("The input stream can not be null.");
        }
        Document document = JaxbHelper.toDOMDocument(is);
        NamedNodeMap nodeMap = document.getDocumentElement().getAttributes();
        final Map<String, String> namespace = new HashMap<>(16);
        for (int i = 0; i < nodeMap.getLength(); i++) {
            Node node = nodeMap.item(i);
            String nsName = node.getNodeName();
            if (!nsName.startsWith("xmlns")) {
                continue;
            }
            String[] nsNameParts = nsName.split(":");
            String prefix = nsNameParts.length == 1 ? nsNameParts[0] : nsNameParts.length == 2 ? nsNameParts[1] : "";
            namespace.put(prefix, node.getNodeValue());
        }
        LOG.debug("[{}/decode] :: Current DSL context path is {}.", this.getClass().getSimpleName(), this.contextPath);
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        setXmlAdapters(unmarshaller);
        return (AbstractDslDefinition) unmarshaller.unmarshal(document);
    }

    @Override
    public void encode(AbstractDslDefinition definition, OutputStream os) throws Exception {
        if (definition == null) {
            throw new IllegalArgumentException(
                    "This config is null or not an instance of " + definition.getClass().getName()
            );
        }
        LOG.debug("[{}/encode] :: Current DSL context path is {}.", this.getClass().getSimpleName(), this.contextPath);
        final Marshaller marshaller = context.createMarshaller();
        setXmlAdapters(marshaller);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
        marshaller.marshal(definition, os);
    }
}
