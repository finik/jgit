/*
 *  Copyright (C) 2008  Shawn Pearce <spearce@spearce.org>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License, version 2, as published by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 */
package org.spearce.jgit.transport;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import org.spearce.jgit.lib.Constants;
import org.spearce.jgit.util.NB;

/**
 * Transfers object data through a dumb transport.
 * <p>
 * Implementations are responsible for resolving path names relative to the
 * <code>objects/</code> subdirectory of a single remote Git repository or
 * nake object database and make the content available as a Java input stream
 * for reading during fetch. The actual object traversal logic to determine the
 * names of files to retrieve is handled through the generic, protocol
 * independent {@link WalkFetchConnection}.
 */
abstract class WalkRemoteObjectDatabase {
	static final String CHARENC = Constants.CHARACTER_ENCODING;

	static final String INFO_PACKS = "info/packs";

	static final String INFO_ALTERNATES = "info/alternates";

	static final String INFO_HTTP_ALTERNATES = "info/http-alternates";

	/**
	 * Obtain the list of available packs (if any).
	 * <p>
	 * Pack names should be the file name in the packs directory, that is
	 * <code>pack-035760ab452d6eebd123add421f253ce7682355a.pack</code>. Index
	 * names should not be included in the returned collection.
	 * 
	 * @return list of pack names; null or empty list if none are available.
	 * @throws IOException
	 *             The connection is unable to read the remote repository's list
	 *             of available pack files.
	 */
	abstract Collection<String> getPackNames() throws IOException;

	/**
	 * Obtain alternate connections to alternate object databases (if any).
	 * <p>
	 * Alternates are typically read from the file {@link #INFO_ALTERNATES} or
	 * {@link #INFO_HTTP_ALTERNATES}. The content of each line must be resolved
	 * by the implementation and a new database reference should be returned to
	 * represent the additional location.
	 * <p>
	 * Alternates may reuse the same network connection handle, however the
	 * fetch connection will {@link #close()} each created alternate.
	 * 
	 * @return list of additional object databases the caller could fetch from;
	 *         null or empty list if none are configured.
	 * @throws IOException
	 *             The connection is unable to read the remote repository's list
	 *             of configured alternates.
	 */
	abstract Collection<WalkRemoteObjectDatabase> getAlternates()
			throws IOException;

	/**
	 * Open a single file for reading.
	 * <p>
	 * Implementors should make every attempt possible to ensure
	 * {@link FileNotFoundException} is used when the remote object does not
	 * exist. However when fetching over HTTP some misconfigured servers may
	 * generate a 200 OK status message (rather than a 404 Not Found) with an
	 * HTML formatted message explaining the requested resource does not exist.
	 * Callers such as {@link WalkFetchConnection} are prepared to handle this
	 * by validating the content received, and assuming content that fails to
	 * match its hash is an incorrectly phrased FileNotFoundException.
	 * 
	 * @param path
	 *            location of the file to read, relative to this objects
	 *            directory (e.g.
	 *            <code>cb/95df6ab7ae9e57571511ef451cf33767c26dd2</code> or
	 *            <code>pack/pack-035760ab452d6eebd123add421f253ce7682355a.pack</code>).
	 * @return a stream to read from the file. Never null.
	 * @throws FileNotFoundException
	 *             the requested file does not exist at the given location.
	 * @throws IOException
	 *             The connection is unable to read the remote's file, and the
	 *             failure occurred prior to being able to determine if the file
	 *             exists, or after it was determined to exist but before the
	 *             stream could be created.
	 */
	abstract FileStream open(String path) throws FileNotFoundException,
			IOException;

	/**
	 * Create a new connection for a discovered alternate object database
	 * <p>
	 * This method is typically called by {@link #readAlternates(String)} when
	 * subclasses us the generic alternate parsing logic for their
	 * implementation of {@link #getAlternates()}.
	 * 
	 * @param location
	 *            the location of the new alternate, relative to the current
	 *            object database.
	 * @return a new database connection that can read from the specified
	 *         alternate.
	 * @throws IOException
	 *             The database connection cannot be established with the
	 *             alternate, such as if the alternate location does not
	 *             actually exist and the connection's constructor attempts to
	 *             verify that.
	 */
	abstract WalkRemoteObjectDatabase openAlternate(String location)
			throws IOException;

	/**
	 * Close any resources used by this connection.
	 * <p>
	 * If the remote repository is contacted by a network socket this method
	 * must close that network socket, disconnecting the two peers. If the
	 * remote repository is actually local (same system) this method must close
	 * any open file handles used to read the "remote" repository.
	 */
	abstract void close();

	/**
	 * Open a buffered reader around a file.
	 * <p>
	 * This is shorthand for calling {@link #open(String)} and then wrapping it
	 * in a reader suitable for line oriented files like the alternates list.
	 * 
	 * @return a stream to read from the file. Never null.
	 * @param path
	 *            location of the file to read, relative to this objects
	 *            directory (e.g. <code>info/packs</code>).
	 * @throws FileNotFoundException
	 *             the requested file does not exist at the given location.
	 * @throws IOException
	 *             The connection is unable to read the remote's file, and the
	 *             failure occurred prior to being able to determine if the file
	 *             exists, or after it was determined to exist but before the
	 *             stream could be created.
	 */
	BufferedReader openReader(final String path) throws IOException {
		return new BufferedReader(new InputStreamReader(open(path).in, CHARENC));
	}

	/**
	 * Read a standard Git alternates file to discover other object databases.
	 * <p>
	 * This method is suitable for reading the standard formats of the
	 * alternates file, such as found in <code>objects/info/alternates</code>
	 * or <code>objects/info/http-alternates</code> within a Git repository.
	 * <p>
	 * Alternates appear one per line, with paths expressed relative to this
	 * object database.
	 * 
	 * @param listPath
	 *            location of the alternate file to read, relative to this
	 *            object database (e.g. <code>info/alternates</code>).
	 * @return the list of discovered alternates. Empty list if the file exists,
	 *         but no entries were discovered.
	 * @throws FileNotFoundException
	 *             the requested file does not exist at the given location.
	 * @throws IOException
	 *             The connection is unable to read the remote's file, and the
	 *             failure occurred prior to being able to determine if the file
	 *             exists, or after it was determined to exist but before the
	 *             stream could be created.
	 */
	Collection<WalkRemoteObjectDatabase> readAlternates(final String listPath)
			throws IOException {
		final BufferedReader br = openReader(listPath);
		try {
			final Collection<WalkRemoteObjectDatabase> alts = new ArrayList<WalkRemoteObjectDatabase>();
			for (;;) {
				String line = br.readLine();
				if (line == null)
					break;
				if (!line.endsWith("/"))
					line += "/";
				alts.add(openAlternate(line));
			}
			return alts;
		} finally {
			br.close();
		}
	}

	static final class FileStream {
		final InputStream in;

		final long length;

		/**
		 * Create a new stream of unknown length.
		 * 
		 * @param i
		 *            stream containing the file data. This stream will be
		 *            closed by the caller when reading is complete.
		 */
		FileStream(final InputStream i) {
			in = i;
			length = -1;
		}

		/**
		 * Create a new stream of known length.
		 * 
		 * @param i
		 *            stream containing the file data. This stream will be
		 *            closed by the caller when reading is complete.
		 * @param n
		 *            total number of bytes available for reading through
		 *            <code>i</code>.
		 */
		FileStream(final InputStream i, final long n) {
			in = i;
			length = n;
		}

		byte[] toArray() throws IOException {
			try {
				if (length >= 0) {
					final byte[] r = new byte[(int) length];
					NB.readFully(in, r, 0, r.length);
					return r;
				}

				final ByteArrayOutputStream r = new ByteArrayOutputStream();
				final byte[] buf = new byte[2048];
				int n;
				while ((n = in.read(buf)) >= 0)
					r.write(buf, 0, n);
				return r.toByteArray();
			} finally {
				in.close();
			}
		}
	}
}