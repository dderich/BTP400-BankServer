package com.seneca.server;

import java.net.*;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Scanner;
import com.seneca.accounts.*;
import com.seneca.business.*;

import java.io.*;

public class HandleClient implements Runnable { // extends Thread
	public Socket connection;
	private static Bank myBank;
	private int clientNum;

	public HandleClient(Socket sock, Bank bank, int clientNo) {
		connection = sock;
		myBank = bank;
		clientNum = clientNo;
	}

	@Override
	public void run() {
		try {
			DataOutputStream dosToClient = new DataOutputStream(connection.getOutputStream());
			DataInputStream disFromClient = new DataInputStream(connection.getInputStream());

			System.out.println("Client #" + clientNum + ": I/O streams connected to the socket");

			try {
				dosToClient.writeUTF(myBank.getBankName()); // send BankName
				dosToClient.flush();
				while (true) {
					int choice = 0;
					while (choice != 7) {
						choice = disFromClient.readInt();

						switch (choice) {

						case 1: // Open an account
							String acc_type = disFromClient.readUTF();
							String acc_info = disFromClient.readUTF();
							String[] acc_args = acc_info.split(";");
							Account newAcc = openAcc(acc_type, acc_args);

							if (myBank.addAccount(newAcc)) {
								System.out.println("Client #" + clientNum + ": Account successfully Added!");
								dosToClient.writeUTF(newAcc.toString() + "\n\n: Account successfully Added!");
								dosToClient.flush();
							} else {
								System.out.println("Client #" + clientNum + ": Unable to add Account");
								dosToClient.writeUTF("Unable to add Account");
								dosToClient.flush();
							}
							break;

						case 2: // Close an account
							String acc_num = disFromClient.readUTF();
							String confirm = disFromClient.readUTF();
							String close = "";
							if (confirm.equals("Y") || confirm.equals("y")) {
								close = closeAcc(myBank, acc_num);
							} else {
								System.out.println("Client #" + clientNum + "Delete cancelled");
							}
							dosToClient.writeUTF(close);
							dosToClient.flush();
							break;
						case 3: // Deposit money
							String accountNumber = disFromClient.readUTF();
							Account depAcc = myBank.searchByAccountNumber(accountNumber);
							if (depAcc != null) {
								dosToClient.writeBoolean(true);
								System.out.print(accountNumber + ": ");
							} else {
								dosToClient.writeBoolean(false);
								System.out.println("\nClient #" + clientNum + "Invalid Account Number Entered!");
							}
							dosToClient.flush();
							double amount = disFromClient.readDouble();

							String message = "";
							if (depAcc != null) {
								depositMoney(depAcc, amount);
								message = "Deposit Successful!";
							}
							dosToClient.writeUTF(message);
							dosToClient.flush();

							break;
						case 4: // Withdraw money
							String aNum = disFromClient.readUTF();
							Account accountFound = myBank.searchByAccountNumber(aNum);
							if (accountFound != null) {
								dosToClient.writeBoolean(true);
							} else {
								System.out.println("Client #" + clientNum + "Error account not found.");
								dosToClient.writeBoolean(false);
							}
							dosToClient.flush();
							double howMuch = disFromClient.readDouble();
							boolean withResult = withdrawMoney(accountFound, howMuch);
							dosToClient.writeBoolean(withResult);
							dosToClient.flush();
							break;
						case 5: // Display accounts
							String option = disFromClient.readUTF();
							String accList = "";
							double balanceEntered = 0.00;
							String nameSearch = "";
							switch (option) {
							case "a":
							case "A":
								nameSearch = disFromClient.readUTF();
								System.out.println(Arrays.toString(myBank.searchByAccountName(nameSearch)));
								accList += Arrays.toString(myBank.searchByAccountName(nameSearch));
								break;
							case "b":
							case "B":
								balanceEntered = disFromClient.readDouble();
								System.out.println(Arrays.toString(myBank.searchByBalance(balanceEntered)));
								accList += Arrays.toString(myBank.searchByBalance(balanceEntered));
								break;
							case "c":
							case "C":
								System.out.println(Arrays.toString(myBank.getAllAccounts()));
								accList += Arrays.toString(myBank.getAllAccounts());
								break;
							default:
								System.out.println("Invalid response, enter a valid response (a-d) or \"x\" to exit");
								break;
							}
							dosToClient.writeUTF(accList);
							dosToClient.flush();
							break;
						case 6: // Display a tax statement
							String name = disFromClient.readUTF();
							String list = "";
							System.out.println("Client #" + clientNum);
							list = displayTax(myBank.getAllAccounts(), name);
							dosToClient.writeUTF(list);
							dosToClient.flush();
							break;
						case 7: // Exit
							System.out.println("Client #" + clientNum + " has exited!");
							break;
						default:
							System.out.println("Client #" + clientNum + "entered Invalid Choice!");
						}

					}
				} // end while

			} catch (EOFException eof) {
				System.out.println("*** CLIENT #" + clientNum + " has terminated connection ***");
			}

			/*
			 * close the connection to the client
			 */
			dosToClient.close();
			disFromClient.close();
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	} // end run

	/**
	 *
	 * @param account Account to be printed out
	 */
	public static void displayAccount(Account account) {
		System.out.println(account);
	}

	/**
	 * Creates an account based on what the user inputs. Different account choices:
	 * Chequing and GIC
	 * 
	 * @return An account built from the user's input.
	 */
	public static Account openAcc(String acc_type, String[] acc_args) {
		Account newAccount = null;
		if (acc_type.equals("CHQ") || acc_type.equals("chq")) {
			// this is vulnerable to type mismatch
			newAccount = new Chequing(acc_args[0], acc_args[1].trim(), Double.parseDouble(acc_args[2].trim()),
					Double.parseDouble(acc_args[3].trim()), Integer.parseInt(acc_args[4].trim()));
		} else if (acc_type.equals("GIC") || acc_type.equals("gic")) {
			newAccount = new GIC(acc_args[0], acc_args[1].trim(), Double.parseDouble(acc_args[2].trim()),
					Integer.parseInt(acc_args[3].trim()), (Double.parseDouble(acc_args[4].trim()) / 100.00));
		}

		if (newAccount != null) {
			displayAccount(newAccount);
		}

		return newAccount;

	}

	/**
	 * Asks user for account number and closes account if found
	 * 
	 * @param bank The bank from which the account will be deleted.
	 */
	public static String closeAcc(Bank bank, String acc_num) {
		String result = "";
		Account a = bank.removeAccount(acc_num);
		if (a != null) {
			result += "Account '" + acc_num + "' successfully deleted";
			System.out.println("Account '" + acc_num + "' successfully deleted");
		} else {
			System.out.println("Account " + acc_num + "' not found");
			result += "Account " + acc_num + "' not found";
		}
		return result;
	}

	public static void depositMoney(Account depAcc, double amount) {
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		depAcc.deposit(amount);
		System.out.println("Deposit of " + nf.format(amount) + " Successful");
	}

	/**
	 * Asks user for account number of the account to be withdrawn from
	 * 
	 * @param bank the bank that the account to be withdrawn from belongs to
	 */
	public static boolean withdrawMoney(Account depAcc, double withdrawAmount) {
		boolean result = false;
		if (depAcc.withdraw(withdrawAmount)) {
			System.out.println("Withdraw successful!");
			result = true;
		} else {
			System.out.println("Withdraw failed!");
			result = false;
		}
		return result;
	}

	/**
	 * Displays all the GIC accounts belonging to a particular person(identified by
	 * their name)
	 * 
	 * @param accounts Takes in an array of accounts. Any taxable accounts are
	 *                 printed out
	 */
	public static String displayTax(Account[] accounts, String n) {
		boolean start = false;
		int count = 1;

		String list = "";
		for (Account a : accounts) {

			if (a instanceof Taxable && a.getFullName().equals(n)) {
				if (!start) {
					System.out.println("Tax rate: " + ((int) (((Taxable) a).tax_rate * 100.00)) + "%");
					list += "Tax rate: " + ((int) (((Taxable) a).tax_rate * 100.00)) + "%\n";
					System.out.println("Name: " + a.getLastName() + ", " + a.getFirstName() + "\n");
					list += "Name: " + a.getLastName() + ", " + a.getFirstName() + "\n\n";
					start = true;
				}
				if (a instanceof GIC) {
					System.out.println("[" + count + "]");
					list += "[" + count++ + "]\n";
					System.out.println(((GIC) a).getTax());
					list += ((GIC) a).getTax() + "\n";
				}

			}
		}
		return list;
	}
} // end class