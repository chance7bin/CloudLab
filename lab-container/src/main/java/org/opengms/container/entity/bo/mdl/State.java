package org.opengms.container.entity.bo.mdl;

import lombok.Data;
import org.opengms.container.entity.SerializableEntity;

import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/04
 */
@Data
public class State extends SerializableEntity {

    String id;

    String name;

    String description;

    List<Event> events;

}
