package net.cube.engine.dsl.impl;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * @author pluto
 * @date 2022/5/31
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class AbstractDslDefinition {

    private static Logger LOG = LoggerFactory.getLogger(AbstractDslDefinition.class);


}
