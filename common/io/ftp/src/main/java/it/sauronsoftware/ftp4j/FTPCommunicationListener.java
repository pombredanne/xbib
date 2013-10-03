/*
 * ftp4j - A pure Java FTP client library
 * 
 * Copyright (C) 2008-2010 Carlo Pelliccia (www.sauronsoftware.it)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version
 * 2.1, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License 2.1 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License version 2.1 along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package it.sauronsoftware.ftp4j;

/**
 * This interface describes how to build objects used to intercept any
 * communication between the client and the server. It is useful to catch what
 * happens behind. A FTPCommunicationListener can be added to any FTPClient
 * object by calling its addCommunicationListener() method.
 * 
 * @see FTPClient#addCommunicationListener(FTPCommunicationListener)
 */
public interface FTPCommunicationListener {

	/**
	 * Called every time a telnet triple has been sent over the network to
	 * the remote FTP server.
	 * 
	 * @param statement
	 *            The triple that has been sent.
	 */
	public void sent(String statement);

	/**
	 * Called every time a telnet triple is received by the client.
	 * 
	 * @param statement
	 *            The received triple.
	 */
	public void received(String statement);

}
