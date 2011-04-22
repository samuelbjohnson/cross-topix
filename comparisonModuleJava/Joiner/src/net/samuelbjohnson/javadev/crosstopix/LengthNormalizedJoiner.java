package net.samuelbjohnson.javadev.crosstopix;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;

public class LengthNormalizedJoiner extends Joiner {
	public LengthNormalizedJoiner(String imslpFile, String cpdlFile)
			throws RepositoryException, RDFParseException, IOException,
			NoSuchAlgorithmException {
		super(imslpFile, cpdlFile);
	}

	@Override
	protected BigDecimal computeDistance(String string1, String string2) {
		BigDecimal distance = new BigDecimal(StringUtils.getLevenshteinDistance(string1, string2));
		BigDecimal normalized = distance.divide(new BigDecimal(Math.max(string1.length(), string2.length())), new MathContext(9));
		return normalized;
	}
}
