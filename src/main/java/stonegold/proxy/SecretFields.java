package stonegold.proxy;

import com.google.common.collect.ImmutableSet;

import java.util.Collection;

public class SecretFields {
    private final ImmutableSet<Integer> secretInputIndex;
    private final ImmutableSet<Integer> secretOutputIndex;

    public SecretFields(Collection<Integer> inputIndex, Collection<Integer> outputIndex) {
        this.secretInputIndex = ImmutableSet.copyOf(inputIndex);
        this.secretOutputIndex = ImmutableSet.copyOf(outputIndex);
    }

    public boolean isInputEmpty() {
        return secretInputIndex.isEmpty();
    }

    public boolean isOutputEmpty() {
        return secretOutputIndex.isEmpty();
    }


    public boolean isSecretInputField(Object arg) {
        return secretInputIndex.contains(arg);
    }


    public boolean isSecretOutputField(Object arg) {
        return secretOutputIndex.contains(arg);
    }
}
