package soot.validation;

import soot.Body;
import soot.Unit;


/**
 * This kind of validation exception can be used if a unit is the cause of an validation error.
 */
public class UnitValidationException extends ValidationException {
	
	/**
	 * Creates a new ValidationException.
	 * @param concerned the unit which is concerned and could be highlighted in an IDE
	 * @param body the body which contains the concerned unit
	 * @param strMessage the message to display in an IDE supporting the concerned feature
	 * @param isWarning whether the exception can be considered as a warning message
	 */
	public UnitValidationException(Unit concerned, Body body, String strMessage, boolean isWarning) {
		super(concerned, strMessage, formatMsg(strMessage, concerned, body), isWarning);
	}

	/**
	 * Creates a new ValidationException, treated as an error.
	 * @param body the body which contains the concerned unit
	 * @param concerned the object which is concerned and could be highlighted in an IDE; for example an unit, a SootMethod, a SootClass or a local.
	 * @param strMessage the message to display in an IDE supporting the concerned feature
	 */
	public UnitValidationException(Unit concerned, Body body, String strMessage) {
		super(concerned, strMessage, formatMsg(strMessage, concerned, body), false);
	}


	private static String formatMsg(String s, Unit u, Body b) {
		StringBuilder sb = new StringBuilder();
		sb.append(s + "\n");
		sb.append("in unit: " + u + "\n");
		sb.append("in body: \n " + b + "\n");
		return sb.toString();
	}

	private static final long serialVersionUID = 1L;

}
