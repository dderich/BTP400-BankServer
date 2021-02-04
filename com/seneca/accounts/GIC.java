/*
 * Assignment 1
 * @author Daniel Derich
 * @version 1.1
 * @since 2020-02-22
 * */

package com.seneca.accounts;
import java.io.*;
import java.math.BigDecimal;
import java.text.NumberFormat;

public class GIC extends Account implements Taxable, java.io.Serializable {
	private int m_period; // period of investment in years
	private BigDecimal m_rate; // annual interest rate
	private BigDecimal taxAmount = BigDecimal.valueOf(0.00); // total amount of taxes on balance at maturity
	private BigDecimal intIncome = BigDecimal.valueOf(0.00); // interest on starting balance using formula

	/**
	 * constructs empty GIC with default values
	 */
	public GIC() {
		this("", "", 0.00, 1, 0.0125);

	}

	/**
	 * This constructs a Account->GIC object
	 * 
	 * @param name      the full name of the customer
	 * @param acctnum   the customer's account number
	 * @param principal the starting balance of GIC
	 * @param period    the total # of years
	 * @param rate      the interest rate
	 */
	public GIC(String name, String acctnum, double principal, int period, double rate) {
		super(name, acctnum, principal);
		if (period <= 0) {
			m_period = 1;
		} else {
			m_period = period;
		}
		if (rate <= 0.00) {
			m_rate = BigDecimal.valueOf(0.0125);
		} else {
			m_rate = BigDecimal.valueOf(rate);
		}
	}

	/**
	 * This is an override of the equals() method
	 * 
	 * @param objG : Object - checked by if instanceof
	 * @return status : boolean returns true if all attributes are equal
	 */
	public boolean equals(Object objG) {
		boolean status = false;
		if (objG instanceof GIC) {
			GIC g2 = (GIC) objG;
			if (super.equals(g2) && this.m_period == g2.m_period && this.m_rate.equals(g2.m_rate)) {
				status = true;
			}

		}
		return status;
	}

	/**
	 * toString()
	 * 
	 * @return result.toString() : String - prints out all private attributes in
	 *         format
	 */
	public String toString() {
		NumberFormat nf = NumberFormat.getCurrencyInstance(); // formats intIncome & nbal
		NumberFormat rt = NumberFormat.getNumberInstance(); // formats Period of Investment
		rt.setMinimumFractionDigits(2);
		rt.setMaximumFractionDigits(2);
		StringBuffer result = new StringBuffer();
		BigDecimal nbal = BigDecimal.valueOf(getAccountBalance()) ; // required to run so that intIncome is calculated before printed to
												// console

		result.append(super.toString());
		result.append("\nAccount Type               : GIC\n").append("Annual Interest Rate       : ")
				.append(rt.format(m_rate.doubleValue() * 100.00));
		result.append("%\nPeriod of Investment       : ").append(m_period)
				.append(" year(s)\n" + "Interest Income at Maturity: ");
		result.append(nf.format(intIncome.doubleValue())).append("\nBalance at Maturity        : ");
		result.append(nf.format(nbal.doubleValue())).append("\n");
		return result.toString();
	}

	/**
	 * hashCode()
	 * 
	 * @return superclass hashcode
	 */
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * Interface method Calculates total taxes on balance at maturity
	 * 
	 */
	public void calculateTax() {
		taxAmount = BigDecimal.valueOf(this.getAccountBalance()).multiply(BigDecimal.valueOf(tax_rate));
	}

	/**
	 * Interface method
	 * 
	 * @return the total tax amount, private variable getters method
	 */
	public double getTaxAmount() {
		this.calculateTax();
		return taxAmount.doubleValue();
	}

	/**
	 * Interface method
	 * 
	 * @return str : String - a listing of the amount of interest, new balance, tax
	 *         accumulated.
	 */
	public String createTaxStatement() {
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		StringBuffer str = new StringBuffer();
		str.append("Tax rate       : ").append((int) (tax_rate * 100.00)).append("%\nAccount Number : ")
				.append(this.getAccountNumber());
		str.append("\nInterest Income: ").append(nf.format(this.getAccountBalance()))
				.append("\nAmount of tax  : ");
		str.append(nf.format(getTaxAmount())).append("\n");
		return str.toString();
	}

	@Override // empty as GIC does not allow deposits
	public void deposit(double amount) {
	}

	@Override // returns false always because GIC does not allow withdrawals
	public boolean withdraw(double amount) {
		return false;
	}

	/**
	 * getAccountBalance()
	 * 
	 * @return BigDecimal of Balance at Maturity Formula: Current/Starting Balance x
	 *         ( 1 + r ) ^ t r = annual interest rate t = number of years (i.e.
	 *         period of investment)
	 */
	@Override
	public double getAccountBalance() {
		BigDecimal start = BigDecimal.valueOf(super.getAccountBalance()) ;
		BigDecimal mature = BigDecimal.valueOf(0.00);
		BigDecimal formula = m_rate.add(BigDecimal.valueOf(1.00)).pow(m_period);
		mature = start.multiply(formula);
		this.intIncome = mature.subtract(start);
		return mature.doubleValue();
	}

	/**
	 * used for FinancialApp: public static void displayTax(Account[] accounts)
	 * 
	 * @return str.toString() - String that holds formatted information about a
	 *         Taxable GIC Account
	 */
	public String getTax() {
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		StringBuffer str = new StringBuffer();
		// str.append("Tax rate: ").append((int) (tax_rate * 100.00)).append("%\n");
		str.append("Account Number : ").append(this.getAccountNumber());
		str.append("\nInterest Income: ").append(nf.format(this.getAccountBalance()))
				.append("\nAmount of tax  : ");
		str.append(nf.format(getTaxAmount())).append("\n");
		return str.toString();
	}
}
