package stonegold;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface BeanMapper<T> {
    T map(ResultSet rs) throws SQLException;
}
