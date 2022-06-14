package application.common;

import org.apache.commons.dbcp.BasicDataSource;

import io.sentry.ITransaction;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.SpanStatus;
import io.sentry.protocol.Message;
import io.sentry.protocol.User;

public class Logger {

	private static Logger instanceLog = null;

	private static final String OPERATING_SYSTEM = System.getProperty("os.name");
	private static final String JAVA_VERSION = System.getProperty("java.version");
	private static final String JAVA_VERSION_DATE = System.getProperty("java.version.date");
	private static final String DEVICE_USER_NAME = System.getProperty("user.name");

	private ITransaction globalTransaction = null;

	public static Logger getInstance() {
		if (instanceLog == null)
			instanceLog = new Logger();
		return instanceLog;
	}

	private Logger() {
	}

	public void setLogger(String appType) {
		Sentry.init(options -> {
			options.setDsn("Link"); //TODO: Replace "Link" with the correct DNS of your Sentry account
			options.setSampleRate(1.0);
			options.setTracesSampleRate(1.0);
			options.setEnvironment("Develop");
			options.setTag("Application_Type", appType);
			options.setTag("Operating_Sytem", OPERATING_SYSTEM);
			options.setTag("Java_Version", JAVA_VERSION);
			options.setTag("Java_Version_Date", JAVA_VERSION_DATE);
			options.setTag("Device_User_Name", DEVICE_USER_NAME);
		});
	}

	/**
	 * Specifica al logger Sentry quale utente è collegato all'app
	 * 
	 * @param email
	 */
	public void setUser(String email) {
		Sentry.configureScope(scope -> {
			User user = new User();
			user.setEmail(email);
			scope.setUser(user);
		});
	}

	public void captureException(Exception e) {
		Sentry.captureException(e);
		if (globalTransaction != null) {
			globalTransaction.setThrowable(e);
			globalTransaction.setStatus(SpanStatus.INTERNAL_ERROR);
			closeTransaction(globalTransaction);
		}
	}

	/**
	 * Chiude la transaction che cattura la performance
	 * 
	 * @param transaction - La transaction utilizzata per
	 *                    {@link #startTransaction(String, String)}
	 */
	public void closeTransaction(ITransaction transaction) {
		if (transaction != null) {
			transaction.finish();
		}
		if (globalTransaction != null) {
			globalTransaction.finish();
			globalTransaction = null;
		}

	}

	public void captureMessage(String errorMessage) {
		Sentry.captureMessage(errorMessage);
	}

	public void captureMessage(String errorMessage, SentryLevel level) {
		Sentry.captureMessage(errorMessage, level);
	}

	/**
	 * Inizia la transaction, per catturare la performance del metodo/operazione da
	 * questo istante.
	 * 
	 * @param methodName - nome del metodo per facilitare l'individualizzazione
	 *                   della provenienza
	 * @param taskName   - descrizione dell'operazione che viene eseguita
	 * @return ITransaction - la transaction da passare nel
	 *         {@link #closeTransaction(ITransaction)}
	 */
	public ITransaction startTransaction(String methodName, String taskName) {
		globalTransaction = Sentry.startTransaction(methodName, taskName);
		return globalTransaction;
	}

	public void captureException(Exception e, String errorMessage) {
		SentryEvent event = new SentryEvent();
		Message message = new Message();
		message.setMessage(errorMessage);
		event.setMessage(message);
		event.setThrowable(e);
		Sentry.captureEvent(event);
	}

	public void capturePoolConnectionInfo(BasicDataSource ds) {
		System.out.println("min idle: " + ds.getMinIdle());
		System.out.println("current num idle: " + ds.getNumIdle());
		System.out.println("max idle: " + ds.getMaxIdle());
		System.out.println("current active: " + ds.getNumActive());
		System.out.println("max active: " + ds.getMaxActive());
		System.out.println("map prepared statement: " + ds.getMaxOpenPreparedStatements());
	}
}
