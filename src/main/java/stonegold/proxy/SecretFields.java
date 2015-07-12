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
