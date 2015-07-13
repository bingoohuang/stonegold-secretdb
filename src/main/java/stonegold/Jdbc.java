package stonegold;

import com.google.common.base.Throwables;
import stonegold.proxy.ConnectionProxy;

import java.sql.*;

public class Jdbc {
    public static Connection getConnection(String url, String user, String password) {
        try {
            return ConnectionProxy.proxy(DriverManager.getConnection(url, user, password));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static boolean execute(Connection conn, String sql, Object... placeholders) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindPlaceholders(ps, placeholders);
            return ps.execute();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
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

    public static <T> T execute(Connection conn, BeanMapper<T> beanMapper, String sql, Object... placeholders) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindPlaceholders(ps, placeholders);
            try (ResultSet resultSet = ps.executeQuery()) {
                return resultSet.next() ? beanMapper.map(resultSet) : null;
            }
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        }
    }
}
