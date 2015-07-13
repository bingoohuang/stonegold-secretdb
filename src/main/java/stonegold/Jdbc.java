package stonegold;

import com.google.common.base.Throwables;
import stonegold.proxy.ConnectionProxy;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class Jdbc {
    static String url, user, password, secretKey;

    static {
        try {
            Properties jdbcProps = new Properties();
            //load a properties file from class path, inside static method
            jdbcProps.load(Jdbc.class.getClassLoader().getResourceAsStream("jdbc.properties"));
            url = jdbcProps.getProperty("url");
            user = jdbcProps.getProperty("user");
            password = jdbcProps.getProperty("password");
            secretKey = jdbcProps.getProperty("secret.key");
        } catch (IOException ex) {
            throw Throwables.propagate(ex);
        }
    }

    public static String getSecretKey() {
        return secretKey;
    }

    private static Connection getConnection() {
        try {
            return ConnectionProxy.proxy(DriverManager.getConnection(url, user, password));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static boolean execute(String sql, Object... placeholders) {
        try (
                Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            bindPlaceholders(ps, placeholders);
            return ps.execute();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static <T> T execute(BeanMapper<T> beanMapper, String sql, Object... placeholders) {
        try (
                Connection conn = getConnection();
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

    public static <T> T execute(Class<T> beanClass, String sql, Object... placeholders) {
        DefaultBeanMapper<T> beanMapper = new DefaultBeanMapper<T>(beanClass);
        return execute(beanMapper, sql, placeholders);
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
