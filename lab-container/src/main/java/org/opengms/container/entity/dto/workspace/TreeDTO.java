package org.opengms.container.entity.dto.workspace;

import lombok.Data;

import java.util.List;

/**
 * @author 7bin
 * @date 2022/11/01
 */
@Data
public class TreeDTO {

    String label;

    String relativePath;

    List<TreeDTO> children;

}
