package stonegold;

import com.google.common.base.Throwables;
import stonegold.proxy.ConnectionProxy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class Jdbc {
    public static Connection getConnection(String url, String user, String password) {
        try {
            Class.forName("org.h2.Driver");
            return ConnectionProxy.proxy(DriverManager.getConnection(url, user, password));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static boolean execute(Connection conn, String sql, Object... placeholders) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 0;
            for (Object placeholder : placeholders) {
                if (placeholder instanceof String) {
                    ps.setString(++i, (String) placeholder);
                } else {
                    ps.setObject(++i, placeholder);
                }
            }
            return ps.execute();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
