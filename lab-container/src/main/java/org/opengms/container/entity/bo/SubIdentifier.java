package org.opengms.container.entity.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字符串截取标识
 *
 * @author 7bin
 * @date 2022/10/20
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SubIdentifier {

    /** 截取的字符串又侧索引 */
    Integer left;

    /** 截取的字符串左侧索引 */
    Integer right;


    /** 截取的字符串 */
    String subBody;
}
