package cern.c2mon.client.ext.history.config;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class HistoryDataSourceConfig {

  @Bean
  @ConfigurationProperties(prefix = "c2mon.client.history.jdbc")
  public DataSource historyDataSource(Environment environment) {

    /**
     * HSQL only allows other JVMs to connect, if data is persisted on disk.<br/>
     * By default C2MON server is only storing data In-Memory.
     * Therefore please change accordingly the following c2mon server properties to the same url:
     * <li>c2mon.server.cachedbaccess.jdbc.url</li>
     * <li>c2mon.server.history.jdbc.url</li>
     */
    String url = "jdbc:hsqldb:hsql://localhost/c2mondb;sql.syntax_ora=true";
    String username = "sa";
    String password = "";

    BasicDataSource dataSource = (BasicDataSource) DataSourceBuilder.create().url(url).username(username).password(password).build();

    if (url.contains("hsql")) {
      dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
    }
    else if (url.contains("oracle")) {
      dataSource.setDriverClassName("oracle.jdbc.OracleDriver");

      // In oracle mode, reduce the connection timeout to 5 seconds
      dataSource.addConnectionProperty("oracle.net.CONNECT_TIMEOUT", "5000");
    }
    else if (url.contains("mysql")) {
      dataSource.setDriverClassName("com.mysql.jdbc.Driver");
    }

    return dataSource;
  }
}
