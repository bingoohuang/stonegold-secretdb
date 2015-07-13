package stonegold.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;

public class ResultSetProxy implements InvocationHandler {
    private final ResultSet rs;
    private final SecretFields secretFields;

    public ResultSetProxy(ResultSet rs, SecretFields secretFields) {
        this.rs = rs;
        this.secretFields = secretFields;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Object result = method.invoke(rs, args);
        if (methodName.equals("getString") || methodName.equals("getObject")) {
            if (secretFields.isSecretOutputField(args[0])) {
                String strResult = (String) result;
                if (strResult.startsWith("secret:")) return strResult.substring(7);
            }
        }

        return result;
    }

    public Object createProxy() {
        return Proxy.newProxyInstance(ResultSetProxy.class.getClassLoader(),
                new Class[]{ResultSet.class},
                this);
    }
}
