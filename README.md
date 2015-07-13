# stonegold-secretdb
a demonstration for auto encryption/decryption of db table secret fields in internal training of touch-a-stone-and-turn-it-into-gold

1. initial code.

    ```java
    package stonegold;

    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.sql.SQLException;

    public class Main {
        public static void main(String[] args) throws ClassNotFoundException, SQLException {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection("jdbc:h2:~/stonegold", "sa", "");

            String createTableSql = "create table person(name varchar(10), id_no varchar(18), credit_card varchar(16))";
            PreparedStatement ps = conn.prepareStatement(createTableSql);
            ps.execute();
            ps.close();

            String insertSql = "insert into person(name, id_no, credit_card) values(?, ?, ?)";
            PreparedStatement insertPs = conn.prepareStatement(insertSql);
            insertPs.setString(1, "bingoo");
            insertPs.setString(2, "321421198312111234");
            insertPs.setString(3, "1111222233334444");
            insertPs.execute();
            insertPs.close();

            conn.close();
        }
    }
    ```

1. repeatable run.

    ```java
    String dropTableSql = "drop table if exists person";
    Statement statement = conn.createStatement();
    statement.execute(dropTableSql);
    statement.close();
    ```

1. use try-with-resources statement.

    ```java
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");

        try (Connection conn = DriverManager.getConnection("jdbc:h2:~/stonegold", "sa", "")) {
            String dropTableSql = "drop table if exists person";
            try (Statement statement = conn.createStatement()) {
                statement.execute(dropTableSql);
            }

            String createTableSql = "create table  person(name varchar(10), id_no varchar(18), credit_card varchar(16))";
            try (PreparedStatement ps = conn.prepareStatement(createTableSql)) {
                ps.execute();
            }

            String insertSql = "insert into person(name, id_no, credit_card) values(?, ?, ?)";
            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setString(1, "bingoo");
                insertPs.setString(2, "321421198312111234");
                insertPs.setString(3, "1111222233334444");
                insertPs.execute();
            }
        }
    }
    ```

1. verify the try-with-resources statement.

    ```java
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
    ```

1. try to proxy connection for sql parsing and auto encryption.

    ```java
    package stonegold;
    
    import com.google.common.collect.ImmutableSet;
    import com.google.common.primitives.Ints;
    
    import java.lang.reflect.InvocationHandler;
    import java.lang.reflect.Method;
    import java.lang.reflect.Proxy;
    import java.sql.*;
    
    public class Main {
        public static void main(String[] args) throws ClassNotFoundException, SQLException {
            Class.forName("org.h2.Driver");
    
            try (Connection conn = proxy(DriverManager.getConnection("jdbc:h2:~/stonegold", "sa", ""))) {
                String dropTableSql = "drop table if exists person";
                try (Statement statement = conn.createStatement()) {
                    statement.execute(dropTableSql);
                }
    
                String createTableSql = "create table  person(name varchar(10), id_no varchar(36), credit_card varchar(32))";
                try (PreparedStatement ps = conn.prepareStatement(createTableSql)) {
                    ps.execute();
                }
    
                String insertSql = "insert into person(name, id_no, credit_card) values(?, ?, ?)";
                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    insertPs.setString(1, "bingoo");
                    insertPs.setString(2, "321421198312111234");
                    insertPs.setString(3, "1111222233334444");
                    insertPs.execute();
                }
            }
        }
    
        private static Connection proxy(final Connection connection) {
            return (Connection) Proxy.newProxyInstance(Main.class.getClassLoader(),
                    new Class[]{Connection.class},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            Object invoke = method.invoke(connection, args);
    
                            String methodName = method.getName();
                            if (!methodName.equals("prepareStatement")) return invoke;
    
                            String sql = (String) args[0];
                            SqlSecretFieldsParser sqlSecretFieldsParser = new SqlSecretFieldsParser();
                            final SecretFields secretFields = sqlSecretFieldsParser.parse(sql);
                            if (secretFields.isEmpty()) return invoke;
    
                            final PreparedStatement ps = (PreparedStatement) invoke;
                            return Proxy.newProxyInstance(Main.class.getClassLoader(),
                                    new Class[]{PreparedStatement.class},
                                    new InvocationHandler() {
                                        @Override
                                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                            String name = method.getName();
                                            if (!name.equals("setString")) return method.invoke(ps, args);
    
                                            if (!secretFields.isSecretField(args[0])) return method.invoke(ps, args);
    
                                            args[1] = "secret:" + args[1];
    
                                            return method.invoke(ps, args);
                                        }
                                    });
                        }
                    });
        }
    
        public static class SqlSecretFieldsParser {
            public SecretFields parse(String sql) {
                return new SecretFields(ImmutableSet.copyOf(Ints.asList(2, 3)));
            }
        }
        
        public static class SecretFields {
            private final ImmutableSet<Integer> secretFields;
    
            public SecretFields(ImmutableSet<Integer> secretFields) {
                this.secretFields = secretFields;
            }
    
            public boolean isEmpty() {
                return secretFields.isEmpty();
            }
    
            public boolean isSecretField(Object arg) {
                return secretFields.contains(arg);
            }
        }
    }
    ```

1. refactor to clean the code.

    ```java
    package stonegold;
    
    import com.google.common.base.Throwables;
    
    import java.sql.Connection;
    import java.sql.SQLException;
    
    public class Main {
        public static void main(String[] args) {
            try (Connection conn = Jdbc.getConnection("jdbc:h2:~/stonegold", "sa", "")) {
                Jdbc.execute(conn, "drop table if exists person");
                Jdbc.execute(conn, "create table  person(name varchar(10), id_no varchar(36), credit_card varchar(32))");
                Jdbc.execute(conn, "insert into person(name, id_no, credit_card) values(?, ?, ?)",
                        "bingoo", "321421198312111234", "1111222233334444");
            } catch (SQLException e) {
                throw Throwables.propagate(e);
            }
        }
    }
    
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
    
    package stonegold.proxy;
    
    import stonegold.Main;
    
    import java.lang.reflect.InvocationHandler;
    import java.lang.reflect.Method;
    import java.lang.reflect.Proxy;
    import java.sql.Connection;
    import java.sql.PreparedStatement;
    
    public class ConnectionProxy implements InvocationHandler {
        private final Connection connection;
    
        public ConnectionProxy(Connection connection) {
            this.connection = connection;
        }
    
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object invoke = method.invoke(connection, args);
    
            String methodName = method.getName();
            if (!methodName.equals("prepareStatement")) return invoke;
    
            String sql = (String) args[0];
            SqlSecretFieldsParser sqlSecretFieldsParser = new SqlSecretFieldsParser();
            final SecretFields secretFields = sqlSecretFieldsParser.parse(sql);
            if (secretFields.isEmpty()) return invoke;
    
            final PreparedStatement ps = (PreparedStatement) invoke;
    
            return new PreparedStatementProxy(ps, secretFields).createProxy();
        }
    
        public Connection createProxy() {
            return (Connection) Proxy.newProxyInstance(Main.class.getClassLoader(),
                    new Class[]{Connection.class}, this);
        }
    
        public static Connection proxy(final Connection connection) {
            return new ConnectionProxy(connection).createProxy();
        }
    }
    
    package stonegold.proxy;
    
    import java.lang.reflect.InvocationHandler;
    import java.lang.reflect.Method;
    import java.lang.reflect.Proxy;
    import java.sql.PreparedStatement;
    
    public class PreparedStatementProxy implements InvocationHandler {
        private final PreparedStatement ps;
        private final SecretFields secretFields;
    
        public PreparedStatementProxy(PreparedStatement ps, SecretFields secretFields) {
            this.ps = ps;
            this.secretFields = secretFields;
        }
    
        public PreparedStatement createProxy() {
            return (PreparedStatement) Proxy.newProxyInstance(
                    PreparedStatementProxy.class.getClassLoader(),
                    new Class[]{PreparedStatement.class},
                    this);
        }
    
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if (!name.equals("setString")) return method.invoke(ps, args);
    
            if (!secretFields.isSecretField(args[0])) return method.invoke(ps, args);
    
            args[1] = "secret:" + args[1];
    
            return method.invoke(ps, args);
        }
    }
    
    package stonegold.proxy;
    
    import com.google.common.collect.ImmutableSet;
    import com.google.common.primitives.Ints;
    
    public class SqlSecretFieldsParser {
        public SecretFields parse(String sql) {
            return new SecretFields(ImmutableSet.copyOf(Ints.asList(2, 3)));
        }
    }
    
    package stonegold.proxy;
    
    import com.google.common.collect.ImmutableSet;
    
    public class SecretFields {
        private final ImmutableSet<Integer> secretFields;
    
        public SecretFields(ImmutableSet<Integer> secretFields) {
            this.secretFields = secretFields;
        }
    
        public boolean isEmpty() {
            return secretFields.isEmpty();
        }
    
        public boolean isSecretField(Object arg) {
            return secretFields.contains(arg);
        }
    }
    ```

1. begin to use TDD.

    ```xml
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.12</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.google.truth</groupId>
        <artifactId>truth</artifactId>
        <version>0.27</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>1.4.187</version>
        <scope>test</scope>
    </dependency>
    ```

    ```java
    package stonegold;
    
    import org.junit.Test;
    
    import java.sql.Connection;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    
    import static com.google.common.truth.Truth.assertThat;
    
    public class JdbcTest {
        @Test
        public void simple() throws SQLException {
            try (Connection conn = Jdbc.getConnection("jdbc:h2:~/stonegold", "sa", "")) {
                Jdbc.execute(conn, "drop table if exists person");
                Jdbc.execute(conn, "create table  person(name varchar(10), id_no varchar(36), credit_card varchar(32))");
                Jdbc.execute(conn, "insert into person(name, id_no, credit_card) values(?, ?, ?)",
                        "bingoo", "321421198312111234", "1111222233334444");
    
                Person person = Jdbc.execute(conn, new BeanMapper<Person>() {
                    @Override
                    public Person map(ResultSet rs) throws SQLException {
                        return new Person(rs.getString(1), rs.getString(2), rs.getString(3));
                    }
                }, "select name, id_no, credit_card from person");
    
                assertThat(person).isEqualTo(
                        new Person("bingoo", "secret:321421198312111234", "secret:1111222233334444"));
            }
        }
    }

    package stonegold;
    
    import java.sql.ResultSet;
    import java.sql.SQLException;
    
    public interface BeanMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

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
    ```

1. remove explicit connection everywhere.

    ```java
    public class JdbcTest {
        @Test
        public void simple() throws SQLException {
            Jdbc.execute("drop table if exists person");
            Jdbc.execute("create table  person(name varchar(10), id_no varchar(36), credit_card varchar(32))");
            Jdbc.execute("insert into person(name, id_no, credit_card) values(?, ?, ?)",
                    "bingoo", "321421198312111234", "1111222233334444");
    
            Person person = Jdbc.execute(new BeanMapper<Person>() {
                @Override
                public Person map(ResultSet rs) throws SQLException {
                    return new Person(rs.getString(1), rs.getString(2), rs.getString(3));
                }
            }, "select name, id_no, credit_card from person");
    
            assertThat(person).isEqualTo(
                    new Person("bingoo", "secret:321421198312111234", "secret:1111222233334444"));
        }
    }
    
    ```

1. remove explicit ResultSet/SQLException everywhere.

    ```java
    public class JdbcTest {
        @Test
        public void simple() {
            Jdbc.execute("drop table if exists person");
            Jdbc.execute("create table  person(name varchar(10), id_no varchar(36), credit_card varchar(32))");
            Jdbc.execute("insert into person(name, id_no, credit_card) values(?, ?, ?)",
                    "bingoo", "321421198312111234", "1111222233334444");
    
            Person person = Jdbc.execute(Person.class, "select name, id_no, credit_card from person limit 1");
    
            assertThat(person).isEqualTo(
                    new Person("bingoo", "secret:321421198312111234", "secret:1111222233334444"));
        }
    }
    ```

1. parse sql hint for input/out secret fields auto encryption

    ```java
    @Test
    public void inputEncryptionAndOutDecryption() {
        Jdbc.execute("drop table if exists person");
        Jdbc.execute("create table  person(name varchar(10), id_no varchar(36), credit_card varchar(32))");
        Jdbc.execute("/*!secret in(2,3)*/insert into person(name, id_no, credit_card) values(?, ?, ?)",
                "bingoo", "321421198312111234", "1111222233334444");

        Person person1 = Jdbc.execute(Person.class,
                "/*!secret out(2, 3)*/select name, id_no, credit_card from person where name = ?",
                "bingoo");

        assertThat(person1).isEqualTo(
                new Person("bingoo", "321421198312111234", "1111222233334444"));

        Person person2 = Jdbc.execute(Person.class,
                "/*!secret in(1) out(2, 3)*/select name, id_no, credit_card from person where id_no = ?",
                "321421198312111234");

        assertThat(person2).isEqualTo(
                new Person("bingoo", "321421198312111234", "1111222233334444"));
    }
    ```