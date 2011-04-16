package net.samuelbjohnson.javadev.crosstopix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.dataset.DatasetRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.rio.turtle.TurtleParserFactory;
import org.openrdf.sail.memory.MemoryStore;

public class Joiner {
	
	private Repository imslpRep;
	private RepositoryConnection imslpCon;
	
	private Repository cpdlRep;
	private RepositoryConnection cpdlCon;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws RDFHandlerException 
	 * @throws RDFParseException 
	 */
	public static void main(String[] args) throws RDFParseException, RDFHandlerException, IOException, RepositoryException {
		/*TurtleParserFactory parserFactory = new TurtleParserFactory();
		RDFParser parser = parserFactory.getParser();
		System.out.println(parser.getRDFFormat());
		parser.setRDFHandler(new PageTitleListStatementHandler(System.out));
		BufferedReader reader = new BufferedReader(new FileReader(new File("data/sample.ttl")));
		parser.parse(reader, "");
		*/
		
		Joiner j = new Joiner("data/imslp.ttl", "data/cpdl.ttl");
	}
	
	public Joiner(String imslpFile, String cpdlFile) throws RepositoryException, RDFParseException, IOException {
		imslpRep = new SailRepository(new MemoryStore());
		imslpRep.initialize();
		imslpCon = imslpRep.getConnection();
		
		cpdlRep = new SailRepository(new MemoryStore());
		cpdlRep.initialize();
		cpdlCon = cpdlRep.getConnection();
		
		imslpCon.add(new File(imslpFile), "", RDFFormat.TURTLE, new Resource[0]);
		cpdlCon.add(new File(cpdlFile), "", RDFFormat.TURTLE, new Resource[0]);
		
		RepositoryResult<Statement> imslpResults= imslpCon.getStatements(null, null, null, false, new Resource[0]);
		
		
		while(imslpResults.hasNext()) {
			Statement imslpStatement = imslpResults.next();
			RepositoryResult<Statement> cpdlResults = cpdlCon.getStatements(null, null, null, false, new Resource[0]);
			while(cpdlResults.hasNext()) {
				Statement cpdlStatement = cpdlResults.next();
				join(imslpStatement, cpdlStatement);
			}
		}
	}
	
	public boolean join(Statement imslpStatement, Statement cpdlStatement) {
		
		
		return true;
	}

}
