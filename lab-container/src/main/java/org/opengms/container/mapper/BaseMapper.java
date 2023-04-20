package org.opengms.container.mapper;

import java.io.Serializable;
import java.util.List;

/**
 * @author 7bin
 * @date 2023/04/12
 */
public interface BaseMapper<T> {

    /**
     * 插入一条记录
     * @param entity 实体对象
     * @return {@link Integer}
     * @author 7bin
     **/
    Integer insert(T entity);

    /**
     * 根据实体(ID)删除
     * @param id 主键id
     * @return {@link Integer}
     * @author 7bin
     **/
    Integer deleteById(Long id);

    /**
     * 根据 ID 修改
     * @param entity 实体对象
     * @return {@link Integer}
     */
    Integer updateById(T entity);

    /**
     * 根据 ID 查询
     * @param id 主键id 
     * @return {@link T}
     * @author 7bin
     **/
    T selectById(Long id);

    /**
     * 获取列表
     * @return {@link List <T>}
     * @author 7bin
     **/
    List<T> selectList();

}
