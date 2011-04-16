package net.samuelbjohnson.javadev.crosstopix;

import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.turtle.TurtleWriter;

public class PageTitleListStatementHandler extends TurtleWriter implements
		RDFHandler {
	PageTitleListStatementHandler(OutputStream w) {
		super(w);
	}
	
	@Override
	public void handleStatement(Statement st) {
		System.out.println("Subject: " + st.getSubject().stringValue());
		System.out.println("Predicate: " + st.getPredicate().stringValue());
		System.out.println("Object: " + st.getObject().toString());
	}
}
