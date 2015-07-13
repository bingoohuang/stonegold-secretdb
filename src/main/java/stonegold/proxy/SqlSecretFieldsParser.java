package stonegold.proxy;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SqlSecretFieldsParser {
    public SecretFields parse(String sql) {
        // parse /*!secret in(2,3)*/
        if (sql.startsWith("/*!secret")) {
            Collection<Integer> inputIndex = parseInputOutIndex("in(", sql);
            Collection<Integer> outputIndex = parseInputOutIndex("out(", sql);
            return new SecretFields(inputIndex, outputIndex);
        }

        return new SecretFields(Collections.<Integer>emptyList(), Collections.<Integer>emptyList());
    }


    private Collection<Integer> parseInputOutIndex(String startString, String sql) {
        int startPos = sql.indexOf(startString);
        if (startPos < 0) return ImmutableSet.of();

        int endPos = sql.indexOf(')', startPos);
        String fieldsIndex = sql.substring(startPos + startString.length(), endPos);
        List<String> split = Splitter.on(',').trimResults().splitToList(fieldsIndex);
        return Collections2.transform(split, new Function<String, Integer>() {
            @Override
            public Integer apply(String input) {
                return Integer.parseInt(input);
            }
        });
    }
}
