package application.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import application.Settings;
import application.common.FileBlob;
import application.common.Logger;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * Può essere invocato per avviare una finestra di dialogo, dalla quale
 * scegliere un singolo file. Permette di gestire il modo in cui i file vengono
 * scelti. <br>
 * Permette di specificare anche il {@link FilterType}, direttamente dal
 * costruttore <br>
 * Salva internamente il {@link FileChooser}, il {@link File} e un
 * {@link FileBlob} <br>
 * Offre metodi per la restituizione di: <br>
 * <li>{@link Image} - {@link #getImage()}
 * <li>{@link FileBlob} - {@link #getFileBlob()}
 * <li>URL del path - {@link #getURL()}
 * <li>{@link FileChooser} - {@link #getFileChooser()}
 * <li>{@link Image} - da un Path che include solo le cartelle del progetto
 * {@link #getImage(String)}
 */
public class FilePicker {

	private FileBlob fileBlob;
	private File file;
	private FileChooser fileChooser;

	/**
	 * Costruttore vuoto
	 */
	public FilePicker() {
	}

	/**
	 * Crea un FilePicker, apre la finestra che permette di scegliere il file,
	 * applica il filtro, salva il file nel FilePicker
	 * 
	 * @param fileType - tipo di filtro del file <br>
	 *                 Se non c'è alcun filtro, <b>null</b> / <b>NO_FILTER</b>
	 */
	public FilePicker(FilterType fileType) {
		chooseFile(fileType);
	}

	/**
	 * Prende un {@link Image} a partire da un path che fa riferimento solo alle
	 * cartelle del progetto. Questo metodo troverà automaticamente il path completo
	 * sul dispositivo su cui si richiede il contenuto
	 * 
	 * @param projectImagePath - il path deve includere SOLO le cartelle del
	 *                         progetto, nella quale risiede e il nome del file <br>
	 *                         <i> ad esempio
	 *                         "/application/images/photo_example.jpg" </i>
	 * @return l'immagine di tipo {@link Image}
	 */
	public Image getImage(String projectImagePath) {
		try {
			String path = getClass().getResource(projectImagePath).getPath();
			File file = new File(path);
			FileInputStream fis;
			fis = new FileInputStream(file);
			return new Image(fis);
		} catch (FileNotFoundException e) {
			Logger.getInstance().captureException(e,
					"Error while getting Image from default path: " + projectImagePath);
			return null;
		}
	}

	/**
	 * Prende l'indirizzo URL, dove si trova il file di tipo {@link File} scelto
	 * tramite il FileChooser
	 * 
	 * @return String del path del file
	 */
	private String getURL() {
		return file.toURI().toString();
	}

	/**
	 * @return il file scelto dal FilePicker, convertito in {@link FileBlob}
	 */
	public FileBlob getFileBlob() {
		return fileBlob;
	}

	/**
	 * Apre una finestra che permettere di scegliere un singolo file
	 * 
	 * @param filterType - il tipo di filtro che vuoi applicare al FilePicker <br>
	 */
	public void chooseFile(FilterType fileFilter) {
		fileChooser = new FileChooser();

		if (fileFilter != FilterType.NO_FILTER && fileFilter != null)
			fileChooser.getExtensionFilters().add(filter(fileFilter));

		fileInizializer();
	}
	

	/**
	 * Salva il file scelto in un FileBlob all'interno di FilePicker. <br>
	 * Da prelevare con un metodo come: {@link #getFileBlob()},{@link #getImage()} 
	 * o gli altri all'itnerno della classe.
	 */
	private void fileInizializer() {
		fileChooser.setTitle("Scegli un file");
		Stage stage = new Stage();
		file = fileChooser.showOpenDialog(stage);
		fileBlob = new FileBlob(file);
	}
	
	public void chooseAvatar() {
		fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(filter(FilterType.IMAGE_FILTER));		
		String folder = Paths.get("." + Settings.DEFAULT_PATH_AVATAR_FOLDER).toAbsolutePath().normalize().toString();
		Logger.getInstance().captureMessage("chooseFile() folderPath: "+folder);
		fileChooser.setInitialDirectory(new File(folder));
		fileInizializer();
	}

	/**
	 * Permette di definire il comportamento per ogni {@link FilterType} <br>
	 * 
	 * @param fileFilter - il tipo di filtro: {@link FilterType}
	 * @return filter - il filtro che potrà essere applicato ad un
	 *         {@link FileChooser}
	 */
	private ExtensionFilter filter(FilterType fileFilter) {
		FileChooser.ExtensionFilter filter = null;
		switch (fileFilter) {
		case IMAGE_FILTER:
			filter = new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.gif", "*.bmp");
			break;
		default:
			break;
		}
		return filter;
	}

	/**
	 * @return l'immafine scelta dalla finestra di dialogo creata
	 */
	public Image getImage() {
		return new Image(getURL());
	}

	/**
	 * @return il {@link FileChooser} creato internamento al momento della creazione
	 *         di un FilePicker
	 */
	public FileChooser getFileChooser() {
		return fileChooser;
	}

}