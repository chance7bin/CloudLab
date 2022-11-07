package org.opengms.admin.msc.entity.bo.mdl;

import lombok.Data;
import org.opengms.admin.entity.SerializableEntity;

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
