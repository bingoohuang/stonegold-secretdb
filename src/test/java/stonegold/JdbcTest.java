package stonegold;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class JdbcTest {
    @Test
    public void simple() {
        Jdbc.execute("drop table if exists person");
        Jdbc.execute("create table  person(name varchar(10), id_no varchar(36), credit_card varchar(32))");
        Jdbc.execute("insert into person(name, id_no, credit_card) values(?, ?, ?)",
                "bingoo", "321421198312111234", "1111222233334444");

        Person person = Jdbc.execute(Person.class, "select name, id_no, credit_card from person limit 1");

        assertThat(person).isEqualTo(
                new Person("bingoo", "secret:321421198312111234", "secret:1111222233334444"));
    }
}
