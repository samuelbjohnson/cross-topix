/*
 * Copyright 2011 Samuel B. Johnson
 * This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

package net.samuelbjohnson.javadev.crosstopix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

public class Joiner {
	
	private Repository imslpRep;
	private RepositoryConnection imslpCon;
	
	private Repository cpdlRep;
	private RepositoryConnection cpdlCon;
	
	PrintWriter outputWriter;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws RDFHandlerException 
	 * @throws RDFParseException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] args) throws RDFParseException, RDFHandlerException, IOException, RepositoryException, NoSuchAlgorithmException {
		String file1, file2;
		
		if (args.length < 1) {
			System.out.println("Please name the files you'd like to join.");
			System.out.print("First File: ");
			file1 = getFileName();
			System.out.print("Second File: ");
			file2 = getFileName();
		} else if (args.length == 1) {
			System.out.println("Please name the second file you'd like to join.");
			System.out.print("Second File: ");
			file1 = args[0];
			file2 = getFileName();
		} else {
			file1 = args[0];
			file2 = args[1];
		}
		
		Joiner j = new Joiner(file1, file2);
		j.processJoin();
	}
	
	private static String getFileName() throws IOException {
		return (new BufferedReader(new InputStreamReader(System.in))).readLine();
	}
	
	public Joiner(String imslpFile, String cpdlFile) throws RepositoryException, RDFParseException, IOException, NoSuchAlgorithmException {
		File output = new File("output.ttl");
		outputWriter = new PrintWriter(new FileWriter(output));
		
		outputWriter.println("@prefix dcterms: <http://purl.org/dc/terms/> .");
		outputWriter.println("@prefix us:          <http://purl.org/twc/ontology/cross-topix.owl#> .");
		outputWriter.println("");
		
		imslpRep = new SailRepository(new MemoryStore());
		imslpRep.initialize();
		imslpCon = imslpRep.getConnection();
		
		cpdlRep = new SailRepository(new MemoryStore());
		cpdlRep.initialize();
		cpdlCon = cpdlRep.getConnection();
		
		imslpCon.add(new File(imslpFile), "", RDFFormat.TURTLE, new Resource[0]);
		cpdlCon.add(new File(cpdlFile), "", RDFFormat.TURTLE, new Resource[0]);
	}

	public void processJoin() throws RepositoryException, NoSuchAlgorithmException {
		RepositoryResult<Statement> imslpResults= imslpCon.getStatements(null, null, null, false, new Resource[0]);
		int counter = 1;
		while(imslpResults.hasNext()) {
			System.out.println("Working ismlp line: " + counter);
			counter ++;
			Statement imslpStatement = imslpResults.next();
			RepositoryResult<Statement> cpdlResults = cpdlCon.getStatements(null, null, null, false, new Resource[0]);
			while(cpdlResults.hasNext()) {
				Statement cpdlStatement = cpdlResults.next();
				join(imslpStatement, cpdlStatement);
			}
		}
		
		outputWriter.flush();
		outputWriter.close();
	}
	
	public boolean join(Statement imslpStatement, Statement cpdlStatement) throws NoSuchAlgorithmException {
		int distance = StringUtils.getLevenshteinDistance(imslpStatement.getObject().stringValue(), cpdlStatement.getObject().stringValue());
		
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		outputWriter.print("<http://todo.org/cross-topix/similarity/");
		md5.update((
				imslpStatement.getObject().stringValue() +
				cpdlStatement.getObject().stringValue() +
				Integer.toString(distance)
		).getBytes());
		
		outputWriter.print(md5.digest());
		outputWriter.println(">");
		
		outputWriter.print("\t" + "us:comparator_1 ");
		outputWriter.println(imslpStatement.getObject().stringValue() + ";");
		
		outputWriter.print("\t" + "us:comparator_1 ");
		outputWriter.println(cpdlStatement.getObject().stringValue() + ";");
		
		outputWriter.println("\t" + "us:similarity " + Integer.toString(distance));
		outputWriter.println(".");
		outputWriter.println("");
		
		return true;
	}

}
