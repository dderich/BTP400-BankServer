package com.seneca.server;

import java.net.*;

import com.seneca.accounts.Chequing;
import com.seneca.accounts.GIC;
import com.seneca.business.Bank;

import java.io.*;

/**
 * @author Daniel Derich
 * @since 2020-04-12
 * @version 1.0
 */
public class BankServer {
	
	private static ServerSocket serverSocket;
	public static HandleClient client;
	private static Thread thread;
	
	
	public static void main(String[] args) {
		
		int count = 1;
		try {
			serverSocket = new ServerSocket(5678);
			System.out.println("Listening for a connection...");
			Bank myBank = new Bank();
			loadBank(myBank);
			System.out.println(myBank);
			while (true) {				
				client = new HandleClient(serverSocket.accept(), myBank, count);		
				thread = new Thread(client);
                thread.start();
                System.out.println("Thread started for client #" + count);
				count++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println ("*** the server is going to stop running ***");
	}
	
	/**
	 * @param bank the bank that the accounts are added to
	 */
	public static void loadBank(Bank bank) {
		bank.addAccount(new Chequing("John Doe", "1234C", 123.45, 0.25, 3));
		bank.addAccount(new Chequing("Mary Ryan", "5678C", 678.90, 0.12, 3));
		bank.addAccount(new GIC("John Doe", "9999G", 6000, 2, .0150));
		bank.addAccount(new GIC("Mary Ryan", "888G", 15000, 4, .0250));
		bank.addAccount(new GIC("Mary Ryan", "7778G", 12222, 4, .0250));

	}
}
