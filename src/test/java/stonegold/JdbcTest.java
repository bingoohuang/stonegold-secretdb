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

        Person person = Jdbc.execute(Person.class,
                "select name, id_no, credit_card from person where name = ?",
                "bingoo");

        assertThat(person).isEqualTo(
                new Person("bingoo", "321421198312111234", "1111222233334444"));
    }

    @Test
    public void inputEncryption() {
        Jdbc.execute("drop table if exists person");
        Jdbc.execute("create table  person(name varchar(10), id_no varchar(36), credit_card varchar(32))");
        Jdbc.execute("/*!secret in(2,3)*/insert into person(name, id_no, credit_card) values(?, ?, ?)",
                "bingoo", "321421198312111234", "1111222233334444");

        Person person = Jdbc.execute(Person.class,
                "select name, id_no, credit_card from person where name = ?",
                "bingoo");

        assertThat(person).isEqualTo(
                new Person("bingoo", "secret:321421198312111234", "secret:1111222233334444"));
    }

    @Test
    public void inputEncryptionAndOutDecryption() {
        Jdbc.execute("drop table if exists person");
        Jdbc.execute("create table  person(name varchar(10), id_no varchar(36), credit_card varchar(32))");
        Jdbc.execute("/*!secret in(2,3)*/insert into person(name, id_no, credit_card) values(?, ?, ?)",
                "bingoo", "321421198312111234", "1111222233334444");

        Person person1 = Jdbc.execute(Person.class,
                "/*!secret out(2, 3)*/select name, id_no, credit_card from person where name = ?",
                "bingoo");

        assertThat(person1).isEqualTo(
                new Person("bingoo", "321421198312111234", "1111222233334444"));

        Person person2 = Jdbc.execute(Person.class,
                "/*!secret in(1) out(2, 3)*/select name, id_no, credit_card from person where id_no = ?",
                "321421198312111234");

        assertThat(person2).isEqualTo(
                new Person("bingoo", "321421198312111234", "1111222233334444"));
    }
}
