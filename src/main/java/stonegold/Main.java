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
