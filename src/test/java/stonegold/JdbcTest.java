package stonegold;

import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.google.common.truth.Truth.assertThat;

public class JdbcTest {
    @Test
    public void simple() throws SQLException {
        Jdbc.execute("drop table if exists person");
        Jdbc.execute("create table  person(name varchar(10), id_no varchar(36), credit_card varchar(32))");
        Jdbc.execute("insert into person(name, id_no, credit_card) values(?, ?, ?)",
                "bingoo", "321421198312111234", "1111222233334444");

        Person person = Jdbc.execute(new BeanMapper<Person>() {
            @Override
            public Person map(ResultSet rs) throws SQLException {
                return new Person(rs.getString(1), rs.getString(2), rs.getString(3));
            }
        }, "select name, id_no, credit_card from person");

        assertThat(person).isEqualTo(
                new Person("bingoo", "secret:321421198312111234", "secret:1111222233334444"));
    }
}
