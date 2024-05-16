package org.opengear.mybatisplus.utils;

import org.apache.ibatis.session.Configuration;

public class MapperUtils {

    public static Class<?> getMapper(Configuration configuration, String id) {
        return configuration.getMappedStatement(id).getParameterMap().getType();
    }

}
