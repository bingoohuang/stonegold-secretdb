package stonegold.proxy;

import stonegold.Aes;
import stonegold.Jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
        if (name.equals("executeQuery")) {
            ResultSet rs = (ResultSet) method.invoke(ps, args);

            if (secretFields.isOutputEmpty()) return rs;

            return new ResultSetProxy(rs, secretFields).createProxy();
        }

        if (!name.equals("setString")) return method.invoke(ps, args);

        if (!secretFields.isSecretInputField(args[0])) return method.invoke(ps, args);

        args[1] = Aes.encrypt((String) args[1], Jdbc.getSecretKey());

        return method.invoke(ps, args);
    }
}
