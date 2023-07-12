package org.opengms.admin.entity.bo;

import lombok.Data;

import java.util.List;

/**
 * @author 7bin
 * @date 2023/07/12
 */
@Data
public class CanalMessage {

    String id;
    String type;
    Boolean isDdl;
    String sql;
    String database;
    String table;
    List<Object> data;
    List<Object> old;

}
