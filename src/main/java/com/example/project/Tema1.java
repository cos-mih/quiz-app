package com.example.project;

import javax.xml.crypto.Data;
import java.io.File;

/**
 * Starts running the project with the corresponding string of command line arguments
 */
public class Tema1 {

	/**
	 * Initialises a new "database" consisting of CSV type files that will save
	 * information in the system persistently; interprets the list of given arguments
	 * as the corresponding type of command.
	 * @param args command line arguments
	 */
	public static void main(final String[] args)
	{
		if(args == null)
		{
			System.out.print("Hello world!");
            return;
		}

        Database db = new Database();
        db.connect();

		Command command = new Command(args);
		command.interpreter(db);
	}
}
