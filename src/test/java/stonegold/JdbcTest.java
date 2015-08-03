package stonegold;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static com.google.common.truth.Truth.assertThat;

public class JdbcTest {
    @BeforeClass
    public static void beforeClass() {
        Properties properties = new Properties();
        properties.put("url", "jdbc:h2:~/stonegold");
        properties.put("user", "sa");
        properties.put("password", "");
        properties.put("secret.key", "SdxmXqwCb05NXpodets+4g==");

        Jdbc.config(properties);
    }

    @AfterClass
    public static void afterClass() {
        String userHome = System.getProperty("user.home");
        new File(userHome, "stonegold.mv.db").delete();
    }

    @Before
    public void before() {
        Jdbc.run("drop table if exists person");
        Jdbc.run("create table  person(name varchar(10), id_no varchar(100), credit_card varchar(100))");
    }


    @Test
    public void simple() {
        Jdbc.run("insert into person(name, id_no, credit_card) values(?, ?, ?)",
                "bingoo", "321421198312111234", "1111222233334444");

        Person person = Jdbc.run(Person.class,
                "select name, id_no, credit_card from person where name = ?",
                "bingoo");

        assertThat(person).isEqualTo(
                new Person("bingoo", "321421198312111234", "1111222233334444"));
    }

    @Test
    public void inputEncryption() {
        Jdbc.run("/*!secret in(2,3)*/insert into person(name, id_no, credit_card) values(?, ?, ?)",
                "bingoo", "321421198312111234", "1111222233334444");

        Person person = Jdbc.run(Person.class,
                "select name, id_no, credit_card from person where name = ?",
                "bingoo");

        assertThat(person).isNotNull();
    }

    @Test
    public void inputEncryptionAndOutDecryption() {
        Jdbc.run("/*!secret in(2,3)*/insert into person(name, id_no, credit_card) values(?, ?, ?)",
                "bingoo", "321421198312111234", "1111222233334444");

        Person person1 = Jdbc.run(Person.class,
                "/*!secret out(2, 3)*/select name, id_no, credit_card from person where name = ?",
                "bingoo");

        assertThat(person1).isEqualTo(
                new Person("bingoo", "321421198312111234", "1111222233334444"));
    }
}
