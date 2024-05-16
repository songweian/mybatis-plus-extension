package org.opengear.mybatisplus.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.mapper.Mapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public class OpenGearSqlInjector extends DefaultSqlInjector {
    private static final Field TABLE_NAME = ReflectionKit.getFieldMap(TableInfo.class).get("tableName");
    @Override
    public void inspectInject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass) {

        Class<?> modelClass = ReflectionKit.getSuperClassGenericType(mapperClass, Mapper.class, 0);
        if (modelClass != null) {
            String className = mapperClass.toString();
            Set<String> mapperRegistryCache = GlobalConfigUtils.getMapperRegistryCache(builderAssistant.getConfiguration());
            if (!mapperRegistryCache.contains(className)) {
                TableInfo tableInfo = TableInfoHelper.initTableInfo(builderAssistant, modelClass);
                updateTableName(tableInfo, builderAssistant.getConfiguration());
                List<AbstractMethod> methodList = this.getMethodList(mapperClass, tableInfo);
                // 兼容旧代码
                if (CollectionUtils.isEmpty(methodList)) {
                    methodList = this.getMethodList(builderAssistant.getConfiguration(), mapperClass, tableInfo);
                }
                if (CollectionUtils.isNotEmpty(methodList)) {
                    // 循环注入自定义方法
                    methodList.forEach(m -> m.inject(builderAssistant, mapperClass, modelClass, tableInfo));
                } else {
                    logger.debug(className + ", No effective injection method was found.");
                }
                mapperRegistryCache.add(className);
            }
        }
    }

    private void updateTableName(TableInfo tableInfo, Configuration configuration) {
        String tableName = tableInfo.getTableName();
        String targetTableName = PropertyParser.parse(tableName, configuration.getVariables());
        try {
            TABLE_NAME.set(tableInfo, targetTableName);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AbstractMethod> getMethodList(Configuration configuration, Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = super.getMethodList(configuration, mapperClass, tableInfo);
        methodList.add(new InsertBatchSomeColumn());
        return methodList;
    }

}
