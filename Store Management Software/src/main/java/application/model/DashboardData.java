package application.model;

import java.io.Serializable;

public class DashboardData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2297173975306019873L;

	private int numOrdersToRequest = 5;
	/* Statistiche per Shop Manager */
	private double earnings;
	private int numProductsAll;
	private int numCategories;
	private int productsEsaurimento;
	private int productsEsauriti;
	private int usersOnline;
	private int allClients;
	private int numCompletedOrders;
	private int numOrdersInSospeso;
	private int numOrdersInLavorazione;

	/* Statistiche per Client */
	private double spent;
	private int numOrders;
	private int countPurchasing;
	private int numOrderLavorazione;
	private int numOrderCompletati;

	public DashboardData() {
	}

	/* Statistiche Shop Manager */
	public int getNumOrdersToRequest() {
		return numOrdersToRequest;
	}

	public void setNumOrdersToRequest(int numOrdersToRequest) {
		this.numOrdersToRequest = numOrdersToRequest;
	}

	public double getEarnings() {
		return earnings;
	}

	public void setEarnings(double earnings) {
		this.earnings = earnings;
	}

	public int getNumCategories() {
		return numCategories;
	}

	public void setNumCategories(int numCategories) {
		this.numCategories = numCategories;
	}

	public int getProductsEsaurimento() {
		return productsEsaurimento;
	}

	public void setProductsEsaurimento(int productsEsaurimento) {
		this.productsEsaurimento = productsEsaurimento;
	}

	public int getProductsEsauriti() {
		return productsEsauriti;
	}

	public void setProductsEsauriti(int productsEsauriti) {
		this.productsEsauriti = productsEsauriti;
	}

	public int getUsersOnline() {
		return usersOnline;
	}

	public void setUsersOnline(int usersOnline) {
		this.usersOnline = usersOnline;
	}

	public int getAllClients() {
		return allClients;
	}

	public void setAllClients(int allClients) {
		this.allClients = allClients;
	}

	public int getNumProductsAll() {
		return numProductsAll;
	}

	public void setNumProductsAll(int numProductsAll) {
		this.numProductsAll = numProductsAll;
	}

	public int getNumCompletedOrders() {
		return numCompletedOrders;
	}

	public void setNumCompletedOrders(int numCompletedOrders) {
		this.numCompletedOrders = numCompletedOrders;
	}

	public int getNumOrdersInSospeso() {
		return numOrdersInSospeso;
	}

	public void setNumOrdersInSospeso(int numOrdersInSospeso) {
		this.numOrdersInSospeso = numOrdersInSospeso;
	}

	public int getNumOrdersInLavorazione() {
		return numOrdersInLavorazione;
	}

	public void setNumOrdersInLavorazione(int numOrdersInLavorazione) {
		this.numOrdersInLavorazione = numOrdersInLavorazione;
	}
	/* Fine Statistiche Shop Manager */

	/* Statistiche Cliente */
	public double getSpent() {
		return spent;
	}

	public void setSpent(double spent) {
		this.spent = spent;
	}

	public int getNumOrders() {
		return numOrders;
	}

	public void setNumOrders(int numOrders) {
		this.numOrders = numOrders;
	}

	public int getCountPurchasing() {
		return countPurchasing;
	}

	public void setCountPurchasing(int countPurchasing) {
		this.countPurchasing = countPurchasing;
	}

	public int getNumOrderLavorazione() {
		return numOrderLavorazione;
	}

	public void setNumOrderLavorazione(int numOrderLavorazione) {
		this.numOrderLavorazione = numOrderLavorazione;
	}

	public int getNumOrderCompletati() {
		return numOrderCompletati;
	}

	public void setNumOrderCompletati(int numOrderCompletati) {
		this.numOrderCompletati = numOrderCompletati;
	}
	/* Fine Statistiche Cliente */

	@Override
	public String toString() {
		return "DashboardData [numOrdersToRequest=" + numOrdersToRequest + ", earnings=" + earnings
				+ ", numProductsAll=" + numProductsAll + ", numCategories=" + numCategories + ", productsEsaurimento="
				+ productsEsaurimento + ", productsEsauriti=" + productsEsauriti + ", usersOnline=" + usersOnline
				+ ", allClients=" + allClients + ", numCompletedOrders=" + numCompletedOrders + ", numOrdersInSospeso="
				+ numOrdersInSospeso + ", numOrdersInLavorazione=" + numOrdersInLavorazione + "]";
	}

}
