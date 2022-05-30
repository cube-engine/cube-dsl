package net.cube.engine.dsl;

import net.cube.engine.Plugin;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.List;

/**
 * @author pluto
 * @date 2022/5/30
 */
public interface DslExtensional extends Plugin {

    String PLUGIN_TYPE = "DslExtension";

    /**
     *
     * @return
     */
    List<XmlAdapter<?, ?>> getAdapters();

}
