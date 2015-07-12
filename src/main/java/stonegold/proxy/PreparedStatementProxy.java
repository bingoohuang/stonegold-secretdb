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
