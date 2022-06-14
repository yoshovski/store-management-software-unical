package application.common;

import javax.sql.rowset.serial.SerialBlob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.sql.Blob;
import java.sql.SQLException;
import javafx.scene.image.Image;

/**
 * Permette la gestione dei file Blob. Conversione di Blob in altri tipi di
 * dato. Memorizza i dati di tipo Blob sottoforma di byte[] per poter rendere la
 * classe Serializable.
 * <p>
 * Da Blob in Image: Creando un {@link #FileBlob(Blob)} e poi applicando
 * sull'oggetto, il metodo {@link #toImage()};
 * <p>
 * <p>
 * Da File in Blob: {@link #toBlob(String)}
 * <p>
 */
public class FileBlob implements Serializable {

	private static final long serialVersionUID = 7664798339892999162L;
	private byte[] byteStream;

	public FileBlob(byte[] byteStream) {
		this.byteStream = byteStream;
	}

	/**
	 * Crea un oggetto vuoto di tipo FileBlob()
	 */
	public FileBlob() {
	}

	public FileBlob(FileBlob fileBlob) {
		this.byteStream = fileBlob.byteStream;
	}

	/**
	 * Costruttore che prende un Blob come parametro, lo converte in byte[] e lo
	 * memorizza internamente
	 * 
	 * @param blob
	 */
	public FileBlob(Blob blob) {
		byteStream = toByte(blob);
	}

	/**
	 * Costruttore che prende un File come parametro, lo converte in byte[] e lo
	 * memorizza internamente
	 * 
	 * @param file
	 */
	public FileBlob(File file) {
		try {
			byteStream = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			Logger.getInstance().captureException(e, "Error while converting File to byte[]");
		}
	}

	/**
	 * Converte Blob in byte[]
	 * 
	 * @param blob
	 * @return byte[] contenente il Blob sottoforma di sequenza di byte
	 */
	public byte[] toByte(Blob blob) {
		try {
			int blobLength = (int) blob.length();
			byteStream = blob.getBytes(1, blobLength);
		} catch (SQLException e) {
			Logger.getInstance().captureException(e, "Error while converting Blob to byte[]");
		}

		return byteStream;
	}

	public Blob toBlob() {
		try {
			return new SerialBlob(byteStream);
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "Error while converting byte[] to Blob");
			return null;
		}
	}

	/**
	 * Converte FileBlob ovvero byte[] in Image
	 * 
	 * @return Image di javafx.scene.image.Image
	 */
	public Image toImage() {
		Blob blob;
		Image image = null;
		try {
			blob = new SerialBlob(byteStream);
			InputStream is = blob.getBinaryStream();
			image = new Image(is);
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "Error while converting byte[] to Image");
		}
		return image;
	}

	/**
	 * Converte un File in Blob
	 * 
	 * @param filePath (il percorso del file)
	 * @return Blob
	 */
	public Blob toBlob(String filePath) {
		Blob file = null;
		try {
			byteStream = toByte(filePath);
			file = new SerialBlob(byteStream);
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "Error, convertion to Blob failed");
		}
		return file;
	}

	/**
	 * Converte un File in byte[]
	 * 
	 * @param filePath (il percorso del file)
	 * @return byte[] contenente il File sottoforma di sequenza di byte
	 */
	public byte[] toByte(String filePath) {
		File file = new File(filePath);
		try {
			byteStream = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			Logger.getInstance().captureException(e, "Error, convertion from File to byte[] failed");
		}
		return byteStream;
	}

}
