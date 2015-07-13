package stonegold;

import com.google.common.base.CaseFormat;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.apache.commons.beanutils.BeanUtils;
import org.objenesis.ObjenesisStd;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

public class DefaultBeanMapper<T> implements BeanMapper<T> {
    private final Class<T> beanClass;

    public DefaultBeanMapper(Class<T> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public T map(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Map<String, Object> map = Maps.newHashMap();

        for (int i = 1; i <= columnCount; ++i) {
            Object object = rs.getObject(i);

            String columnLabel = allUpperToLower(metaData.getColumnLabel(i));
            map.put(columnLabel, object);
            map.put(underscoreToCamel(columnLabel), object);


            String columnName = allUpperToLower(metaData.getColumnName(i));
            map.put(columnName, object);
            map.put(underscoreToCamel(columnName), object);
        }

        T instance = new ObjenesisStd().getInstantiatorOf(beanClass).newInstance();
        try {
            BeanUtils.populate(instance, map);
        } catch (Exception ex) {
            throw Throwables.propagate(ex);
        }
        return instance;
    }

    private String allUpperToLower(String str) {
        return (withoutLowercaseLetters(str)) ? str.toLowerCase() : str;
    }

    private boolean withoutLowercaseLetters(String str) {
        for (int i = 0, ii = str.length(); i < ii; ++i) {
            char ch = str.charAt(i);
            if (ch >= 'a' && ch <= 'z') return false;
        }
        return true;
    }

    private String underscoreToCamel(String str) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, str);
    }
}
