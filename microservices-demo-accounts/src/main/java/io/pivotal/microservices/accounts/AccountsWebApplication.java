package io.pivotal.microservices.accounts;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

/**
 * The accounts web-application. This class has two uses:
 * <ol>
 * <li>Provide configuration and setup for {@link AccountsServer} ... or</li>
 * <li>Run as a stand-alone Spring Boot web-application for testing (in which
 * case there is <i>no</i> microservice registration</li>
 * </ol>
 * <p>
 * To execute as a microservice, run {@link AccountsServer} instead.
 *
 * @author Paul Chapman
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AccountsWebApplication {

    protected Logger logger = Logger.getLogger(AccountsWebApplication.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(AccountsWebApplication.class, args);
    }

    @Bean
    public DataSource dataSource() {
        logger.info("dataSource() invoked");

        DataSource dataSource = (new EmbeddedDatabaseBuilder())
                .addScript("classpath:testdb/schema.sql")
                .addScript("classpath:testdb/data.sql")
                .build();

        logger.info("dataSource = " + dataSource);

        // Sanity check
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> accounts = jdbcTemplate.queryForList("SELECT number FROM T_ACCOUNT");
        logger.info("System has " + accounts.size() + " accounts");

        // Populate with random balances
        Random rand = new Random();

        for (Map<String, Object> item : accounts) {
            String number = (String) item.get("number");
            BigDecimal balance = new BigDecimal(rand.nextInt(10000000) / 100.0).setScale(2, BigDecimal.ROUND_HALF_UP);
            jdbcTemplate.update("UPDATE T_ACCOUNT SET balance = ? WHERE number = ?", balance, number);
        }

        return dataSource;
    }
}
