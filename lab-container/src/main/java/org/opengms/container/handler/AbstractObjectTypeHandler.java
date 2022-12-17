package org.opengms.container.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.opengms.common.utils.GsonUtils;
import org.opengms.common.utils.StringUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * mybatis java对象 与json 类型转换处理器
 *
 * @author 7bin
 * @date 2022/11/13
 */
public abstract class AbstractObjectTypeHandler<T> extends BaseTypeHandler<T> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter,
                                    JdbcType jdbcType) throws SQLException {
        ps.setString(i, GsonUtils.toJsonStr(parameter));
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName)
        throws SQLException {
        String data = rs.getString(columnName);
        return StringUtils.isBlank(data) ? null : GsonUtils.fromJson(data, (Class<T>) getRawType());
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String data = rs.getString(columnIndex);
        return StringUtils.isBlank(data) ? null : GsonUtils.fromJson(data, (Class<T>) getRawType());
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex)
        throws SQLException {
        String data = cs.getString(columnIndex);
        return StringUtils.isBlank(data) ? null : GsonUtils.fromJson(data, (Class<T>) getRawType());
    }
}
