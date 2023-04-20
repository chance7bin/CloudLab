package org.opengms.container.entity.bo.mdl;

import lombok.Data;
import org.opengms.container.enums.DataMIME;
import org.opengms.container.enums.MDLStructure;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author 7bin
 * @date 2022/11/04
 */
@Data
@XmlRootElement(name = "InputParameter")
public class InputParameter extends Parameter {

}
