package stonegold.proxy;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;

import java.util.Collection;
import java.util.List;

public class SqlSecretFieldsParser {


    public SecretFields parse(String sql) {
        // parse /*!secret in(2,3)*/
        if (sql.startsWith("/*!secret")) {
            int inStartPos = sql.indexOf("in(");
            int inEndPos = sql.indexOf(')', inStartPos);
            String fieldsIndex = sql.substring(inStartPos + 3, inEndPos);
            List<String> split = Splitter.on(',').splitToList(fieldsIndex);
            Collection<Integer> transform = Collections2.transform(split, new Function<String, Integer>() {
                @Override
                public Integer apply(String input) {
                    return Integer.parseInt(input);
                }
            });
            return new SecretFields(ImmutableSet.copyOf(transform));
        }

        return new SecretFields(ImmutableSet.copyOf(Ints.asList(2, 3)));
    }
}
