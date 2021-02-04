package com.btp400.client;

import java.net.*;
import java.io.*;
import java.util.*;

import com.seneca.accounts.Account;

public class BankClient {
	private static String bankName;

	public static void main(String[] args) {
		Socket clientSocket;

		try {
			clientSocket = new Socket(InetAddress.getByName("localhost"), 5678);
			System.out.println("Connected to " + clientSocket.getInetAddress().getHostName());

			// data streams
			DataOutputStream dosToServer = new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream disFromServer = new DataInputStream(clientSocket.getInputStream());

			System.out.println("I/O streams connected to the socket");
			Scanner keyboard = new Scanner(System.in);
			int choice = 0;
			bankName = disFromServer.readUTF();
			while (choice != 7) {
				Scanner in = new Scanner(System.in);
				try {
					displayMenu(bankName);
					choice = menuChoice();
					dosToServer.writeInt(choice);
					dosToServer.flush();
					switch (choice) {
					case 1:
						boolean valid_args = false;
						String acc_type = "";
						String valuesString = "";
						while (!valid_args) {
							System.out.print("Please enter the account type(CHQ/GIC)> ");
							acc_type = in.nextLine();

							System.out.println("Please enter account information in one line.\n");

							if (acc_type.equals("CHQ") || acc_type.equals("chq")) {
								System.out.println(
										"Format: Name;Account Number; Starting Balance; Service Charge; Max number of Transactions");
								System.out.println("ex. (John Doe; 1234; 567.89; 0.25; 3)");
								System.out.print(">");
								valuesString = in.nextLine();
								String[] chq_args = valuesString.split(";"); // type: String[]

								if (chq_args.length != 5) {
									System.out.println("Invalid input. Please follow the format shown on screen");
								} else {
									valid_args = true;
								}
							} else if (acc_type.equals("GIC") || acc_type.equals("gic")) {
								System.out.println(
										"Format: Name; Account Number; Starting Balance; Period of Investment in year(s); Interest Rate (15.5% would be 15.5)");
								System.out.println("Example: John M. Doe;A1234;1000.00; 1; 15.5");
								System.out.print(">");
								valuesString = in.nextLine();
								String[] gic_args = valuesString.split(";");

								if (gic_args.length != 5) {
									System.out.println("Invalid input. Please follow the format shown on screen.");
								} else {
									valid_args = true;
								}
							} else {
								System.out.println("Invalid account type");
							}
						} // end while

						dosToServer.writeUTF(acc_type);
						dosToServer.flush();
						dosToServer.writeUTF(valuesString);
						dosToServer.flush();
						System.out.println(disFromServer.readUTF()); // Account toString()
						break;
					case 2:
						System.out.print("Please enter the Account Number: ");
						String delAccNum = in.nextLine();
						StringBuffer confirm = new StringBuffer("Confirm delete of account with the number:")
								.append(delAccNum).append(" (Y/N)");
						System.out.println(confirm);
						String res = in.nextLine();

						boolean valid_res = false;
						while (!valid_res) {

							switch (res) {
							case "Y":
							case "y":
								valid_res = true;
								break;
							case "N":
							case "n":
								valid_res = true;
								System.out.println("Delete cancelled");
								break;
							default:
								System.out.println("Invalid response, please enter \"Y\" or \"N\" ");

							}
						}
						dosToServer.writeUTF(delAccNum);
						dosToServer.flush();
						dosToServer.writeUTF(res);
						dosToServer.flush();
						System.out.println(disFromServer.readUTF());
						break;

					case 3:
						System.out.print("Please enter your account number: ");
						String acc_num = in.nextLine();
						dosToServer.writeUTF(acc_num);
						dosToServer.flush();
						double withdrawAmount = 0.00;
						boolean state = disFromServer.readBoolean();
						if (state == true) {
							System.out.println("Please enter the amount you would like to deposit.");
							System.out.print("> $");
							withdrawAmount = in.nextDouble();
						} else {
							System.out.println("Invalid Account Number");
						}

						dosToServer.writeDouble(withdrawAmount);
						dosToServer.flush();

						String message = disFromServer.readUTF();
						System.out.println(message);
						break;
					case 4:
						System.out.print("Please enter your account number: ");
						String account_num = in.nextLine();
						dosToServer.writeUTF(account_num);
						dosToServer.flush();
						double withdraw = 0.00;
						boolean wState = disFromServer.readBoolean();
						if (wState) {
							System.out.println("Please enter the amount you would like to withdraw");
							String w = in.nextLine();
							withdraw = Double.parseDouble(w);
						} else {
							System.out.println("Error account not found.");
						}
						dosToServer.writeDouble(withdraw);
						dosToServer.flush();

						if (disFromServer.readBoolean()) {
							System.out.println("Withdraw successful!");
						} else {
							System.out.println("Withdraw failed!");
						}
						break;
					case 5:
						System.out.println("Please choose one of the following options: ");
						System.out.println("a) display all accounts with the same account name");
						System.out.println("b) display all accounts with the same final balance");
						System.out.println("c) display all accounts opened at the bank");
						System.out.print(">");
						String option = in.nextLine();

						dosToServer.writeUTF(option);
						dosToServer.flush();
						switch (option) {
						case "a":
						case "A":
							System.out.println("Please enter the name to search by: ");
							String nameSearch = in.nextLine();
							dosToServer.writeUTF(nameSearch);
							dosToServer.flush();
							break;
						case "b":
						case "B":
							System.out.println("Please enter the balance to search by: ");
							double balance = in.nextDouble();
							dosToServer.writeDouble(balance);
							dosToServer.flush();
							break;
						case "c":
						case "C":
							break;
						default:
							System.out.println("Invalid response, enter a valid response (a-d) or \"x\" to exit");
							break;
						}
						String listOfAccounts = disFromServer.readUTF();
						System.out.println(listOfAccounts);

						break;
					case 6:
						System.out.print("Which person would you like a tax statement for? ");
						String person = in.nextLine();
						dosToServer.writeUTF(person);
						dosToServer.flush();

						String list = disFromServer.readUTF();
						System.out.println(list);

						break;
					case 7:
						System.out.println("Thank you for using the " + bankName + " Online Console System!");
						break;
					default:
						System.out.println("invalid choice");
						choice = menuChoice();
					}

				} catch (StringIndexOutOfBoundsException se) {
					se.printStackTrace();
					System.out.println("INVALID INPUT!");

				} catch (EOFException eof) {

					System.out.println("The server has terminated connection!");
				} catch (IOException e) {
					System.out.println("I/O errors in data exchange");
					e.printStackTrace();
				}
			}
			System.out.println("Client: closing the connection...");
			// close data stream
			dosToServer.close();
			disFromServer.close();

			// close object stream
			// close socket
			clientSocket.close();
		} catch (IOException ioe) {
			System.out.println("I/O errors in socket connection");
			ioe.printStackTrace();
		}
		System.out.println("... the client is going to stop running...");
	} // end main

	/**
	 *
	 * @param bankName The bank's name to be displayed on the menu
	 */
	public static void displayMenu(String bankName) {
		StringBuffer title = new StringBuffer("\nWelcome to ").append(bankName).append(" Bank!");

		System.out.println(title);
		System.out.println("1. Open an account.");
		System.out.println("2. Close an account.");
		System.out.println("3. Deposit money.");
		System.out.println("4. Withdraw money.");
		System.out.println("5. Display accounts.");
		System.out.println("6. Display a tax statement.");
		System.out.println("7. Exit.");
	}

	/**
	 * Takes in an int from user's input
	 * 
	 * @return the user's menu choice
	 */
	public static int menuChoice() {
		Scanner s = new Scanner(System.in);
		System.out.print("Please enter your choice> ");
		String input = s.nextLine();
		return Integer.parseInt(input);
	}

} // end class