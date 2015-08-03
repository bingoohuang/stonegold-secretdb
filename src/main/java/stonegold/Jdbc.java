package stonegold;

import com.google.common.base.Throwables;
import stonegold.proxy.ConnectionProxy;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class Jdbc {
    static String url, user, password, secretKey;

    static {
        try {
            //load a properties file from class path, inside static method
            InputStream is = Jdbc.class.getClassLoader().getResourceAsStream("jdbc.properties");

            if (is != null) {
                Properties jdbcProps = new Properties();
                jdbcProps.load(is);
                config(jdbcProps);
            }
        } catch (IOException ex) {
            throw Throwables.propagate(ex);
        }
    }

    public static void config(Properties props) {
        url = props.getProperty("url");
        user = props.getProperty("user");
        password = props.getProperty("password");
        secretKey = props.getProperty("secret.key");
    }

    public static String getSecretKey() {
        return secretKey;
    }

    public static Connection getConn() {
        try {
            return ConnectionProxy.proxy(DriverManager.getConnection(url, user, password));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static boolean run(String sql, Object... placeholders) {
        try (
                Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            bindPlaceholders(ps, placeholders);
            return ps.execute();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static <T> T run(BeanMapper<T> beanMapper, String sql, Object... placeholders) {
        try (
                Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            bindPlaceholders(ps, placeholders);
            try (ResultSet resultSet = ps.executeQuery()) {
                return resultSet.next() ? beanMapper.map(resultSet) : null;
            }
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    public static <T> T run(Class<T> beanClass, String sql, Object... placeholders) {
        DefaultBeanMapper<T> beanMapper = new DefaultBeanMapper<T>(beanClass);
        return run(beanMapper, sql, placeholders);
    }

    private static void bindPlaceholders(PreparedStatement ps, Object[] placeholders) throws SQLException {
        int i = 0;
        for (Object placeholder : placeholders) {
            if (placeholder instanceof String) {
                ps.setString(++i, (String) placeholder);
            } else {
                ps.setObject(++i, placeholder);
            }
        }
    }

}
