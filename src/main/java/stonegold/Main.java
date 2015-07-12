package stonegold;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");

        try (Connection conn = DriverManager.getConnection("jdbc:h2:~/stonegold", "sa", "")) {
            String dropTableSql = "drop table if exists person";
            try (Statement statement = proxy(conn.createStatement())) {
                statement.execute(dropTableSql);
            }

            String createTableSql = "create table  person(name varchar(10), id_no varchar(18), credit_card varchar(16))";
            try (PreparedStatement ps = proxy(conn.prepareStatement(createTableSql))) {
                ps.execute();
            }

            String insertSql = "insert into person(name, id_no, credit_card) values(?, ?, ?)";
            try (PreparedStatement insertPs = proxy(conn.prepareStatement(insertSql))) {
                insertPs.setString(1, "bingoo");
                insertPs.setString(2, "321421198312111234");
                insertPs.setString(3, "1111222233334444");
                insertPs.execute();
            }
        }
    }

    private static PreparedStatement proxy(final PreparedStatement statement) {
        return (PreparedStatement) Proxy.newProxyInstance(Main.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        System.out.println(method.getName());
                        return method.invoke(statement, args);
                    }
                });
    }

    private static Statement proxy(final Statement statement) {
        return (Statement) Proxy.newProxyInstance(Main.class.getClassLoader(),
                new Class[]{Statement.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        System.out.println(method.getName());
                        return method.invoke(statement, args);
                    }
                });
    }

}
