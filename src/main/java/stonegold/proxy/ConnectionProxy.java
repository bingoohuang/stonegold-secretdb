package stonegold.proxy;

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
        if (secretFields.isInputEmpty() && secretFields.isOutputEmpty()) return invoke;

        final PreparedStatement ps = (PreparedStatement) invoke;

        return new PreparedStatementProxy(ps, secretFields).createProxy();
    }

    public Connection createProxy() {
        return (Connection) Proxy.newProxyInstance(ConnectionProxy.class.getClassLoader(),
                new Class[]{Connection.class}, this);
    }

    public static Connection proxy(final Connection connection) {
        return new ConnectionProxy(connection).createProxy();
    }
}
