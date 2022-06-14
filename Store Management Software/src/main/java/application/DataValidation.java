package application;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RegexValidator;

import javafx.scene.control.Spinner;

public class DataValidation {
	/**
	 * Metodo che verifica se il testo contenuto dentro una textField è una mail
	 * 
	 * @param email
	 * @return true - se il testo è una mail <br>
	 *         false - altrimenti
	 */
	public static boolean validateEmail(JFXTextField email) {
		RegexValidator validator = new RegexValidator();
		validator.setRegexPattern(Settings.REGEX_EMAIL_PATTERN);
		validator.setMessage("Email non valida");

		email.getValidators().add(validator);
		return email.validate();
	}

	/**
	 * Metodo che verifica se il testo contenuto dentro una textField (Password)
	 * rispetta le regole specificate nel file Settings
	 * 
	 * @param password
	 * @return true - se il testo rispetta la lunghezza desiderata <br>
	 *         false - altrimenti
	 */
	public static boolean isValid(JFXPasswordField password) {
		String pass = password.getText();
		return pass.length() >= Settings.PASSWORD_LENGHT;
	}

	/**
	 * Metodo che convalida il numero di telefono inserito nella textField
	 * 
	 * @param phone
	 * @returntrue - se il testo inserito è un numero di telefono valido <br>
	 *             false - altrimenti
	 */
	public static boolean isValid(JFXTextField phone) {
		RegexValidator validator = new RegexValidator();
		validator.setRegexPattern(Settings.REGEX_PHONE_PATTERN);
		validator.setMessage("Telefono non valido");

		phone.getValidators().add(validator);
		return phone.validate();
	}

	/**
	 * Metodo che convalida il numero di porta inserito nella textField
	 * 
	 * @param portNumber
	 * @return true - se è stata inserita una porta valida <br>
	 *         false - altrimenti
	 */
	public static boolean isValidNumber(JFXTextField portNumber) {
		RegexValidator validator = new RegexValidator();
		validator.setRegexPattern(Settings.REGEX_PORT_NUMBER_PATTERN);
		validator.setMessage("Numero di porta non valido");

		portNumber.getValidators().add(validator);
		return portNumber.validate();
	}

	/**
	 * @param numberField
	 * @param minDigitsAllowed - number of minimum digits allowed
	 * @param maxDigitsAllowed - number of maximum digits allowed
	 *                         <p>
	 *                         for example: <i> 404 has three digits. If you want
	 *                         your number to be between 0 and 9999,
	 *                         minNumsAllowed=1, maxNumsAllowed=4</i>
	 * @param errorMessage     - l'errore che verrà mostrato sotto la JFXTetField
	 * @return true - valido <br>
	 *         false - NON valido
	 */
	public static boolean isValidNumber(JFXTextField numberField, int minDigitsAllowed, int maxDigitsAllowed,
			String erroMessage) {
		RegexValidator validator = new RegexValidator();
		String min = Integer.toString(minDigitsAllowed);
		String max = Integer.toString(maxDigitsAllowed);
		validator.setRegexPattern("^[0-9]{" + min + "," + max + "}$");
		validator.setMessage(erroMessage);
		numberField.getValidators().add(validator);
		return numberField.validate();
	}

	/**
	 * Verifica se l'host inserito nella textField è valido
	 * 
	 * @param host
	 * @return true - se il testo inserito è un host <br>
	 *         false - altrimenti
	 */
	public static boolean isValidHost(JFXTextField host) {
		RegexValidator validator = new RegexValidator();
		validator.setRegexPattern(Settings.REGEX_HOST_PATTERN);
		validator.setMessage("Host non valido");

		host.getValidators().add(validator);
		return host.validate();
	}

	/**
	 * Checks if a spinner contains only digits
	 * 
	 * @param spinner
	 * @return true - se lo spinner contiene soltanto interi <br>
	 *         false - altrimenti
	 */
	public static boolean onlyDigits(Spinner<?> spinner) {
		String numberRegex = Settings.REGEX_NUMBER_DECIMALS_INCLUDED;
		Pattern p = Pattern.compile(numberRegex);
		String spinnerValue = spinner.getValue().toString();

		if (spinnerValue == null) {
			return false;
		}

		Matcher matcher = p.matcher(spinnerValue);
		return matcher.matches();
	}
}
