package application.server;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbcp.BasicDataSource;
import application.common.Logger;

public class DataSource {

	private static BasicDataSource ds = new BasicDataSource();

	static {
		ds.setUrl("jdbc:mysql://IP_ADDRESS/DB_NAME"); //TODO: replace IP_ADDRESS and DB_NAME with correct data of your database
		ds.setUsername("username"); //TODO: replace username with the one of your database
		ds.setPassword("psw"); //TODO: replace with correct password of your database
		ds.setMinIdle(5);
		ds.setMaxIdle(10);
		ds.setMaxOpenPreparedStatements(100);

		// se l'oggetto non è stato acceduto negli ultimi 50 secondi, "evict" (butta via
		// dal pool)
		ds.setTimeBetweenEvictionRunsMillis(50 * 1000);
		// frequenza con la quale controllare se l'oggeto e da buttare via "evict"
		ds.setMinEvictableIdleTimeMillis(10 * 1000);
	}

	private DataSource() {
	}

	public static Connection getConnection() {
		try {
			Connection con = ds.getConnection();
			con.setAutoCommit(true);
			return con;
		} catch (SQLException e) {
			Logger.getInstance().captureException(e, "Error with DB connection pool");
		}
		return null;
	}
}