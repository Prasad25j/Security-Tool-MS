package com.example.config;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		basePackages = "com.example.secondaryDev.repository",
		entityManagerFactoryRef = "secondaryDevEntityManagerFactory",
		transactionManagerRef = "secondaryDevTransactionManager"
)
public class SecondaryDevDataSourceConfig {
	@Bean(name = "secondaryDevDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.secondary-dev")
	public HikariDataSource secondaryDataSource() {
		HikariDataSource ds = DataSourceBuilder.create().type(HikariDataSource.class).build();
		ds.setMaximumPoolSize(30);
		return ds;
	}
	
	@Bean(name = "secondaryDevEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory( @Qualifier("secondaryDevDataSource") DataSource secondaryDataSource) {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(secondaryDataSource);
		em.setPackagesToScan("com.example.entity");
		
		em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		return em;
	}
	
	@Bean(name = "secondaryDevTransactionManager")
	public PlatformTransactionManager secondaryTransactionManager( @Qualifier("secondaryDevEntityManagerFactory") LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory) {
		return new JpaTransactionManager(secondaryEntityManagerFactory.getObject());
	}
}