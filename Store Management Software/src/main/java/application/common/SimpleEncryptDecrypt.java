package application.common;

import org.jasypt.util.text.AES256TextEncryptor;

/**
 * Offre la possibilitò di cifrare e decifrare messaggi con una parola chiave
 * impostata. <br>
 * Usa algoritmo di crittografia AES256 per rendere i dati sensibili sicuri..
 */
public class SimpleEncryptDecrypt {

	private AES256TextEncryptor textEncryptor;
	private String encryptedText;
	private String decryptedText;

	/**
	 * Costruttore vuoto che crea un oggetto {@link SimpleEncryptDecrypt}
	 */
	public SimpleEncryptDecrypt() {
	}

	/**
	 * @param cipher      - la parola segreta
	 * @param text        - il testo da cifrare/decifrare
	 * @param encryptMode - true (encryptText), false (decryptText)
	 */
	public SimpleEncryptDecrypt(String cipher, String text, boolean encryptMode) {
		if (encryptMode)
			encryptText(cipher, text);
		else
			decryptText(cipher, text);
	}

	public String getDecryptedText() {
		return decryptedText;
	}

	public String getEncryptedText() {
		return encryptedText;
	}

	private void encryptText(String cipher, String textForEncryption) {
		textEncryptor = new AES256TextEncryptor();
		textEncryptor.setPassword(cipher);
		encryptedText = textEncryptor.encrypt(textForEncryption);
	}

	private void decryptText(String cipher, String encryptedText) {
		AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
		textEncryptor.setPassword(cipher);
		decryptedText = textEncryptor.decrypt(encryptedText);
	}
}
