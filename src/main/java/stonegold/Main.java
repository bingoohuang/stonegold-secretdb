package stonegold;

import java.sql.*;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");

        try (Connection conn = DriverManager.getConnection("jdbc:h2:~/stonegold", "sa", "")) {
            String dropTableSql = "drop table if exists person";
            try (Statement statement = conn.createStatement()) {
                statement.execute(dropTableSql);
            }

            String createTableSql = "create table  person(name varchar(10), id_no varchar(18), credit_card varchar(16))";
            try (PreparedStatement ps = conn.prepareStatement(createTableSql)) {
                ps.execute();
            }

            String insertSql = "insert into person(name, id_no, credit_card) values(?, ?, ?)";
            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setString(1, "bingoo");
                insertPs.setString(2, "321421198312111234");
                insertPs.setString(3, "1111222233334444");
                insertPs.execute();
            }
        }
    }
}
