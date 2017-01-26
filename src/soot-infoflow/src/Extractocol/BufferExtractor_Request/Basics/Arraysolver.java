package Extractocol.BufferExtractor_Request.Basics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import Extractocol.Constants;

public class Arraysolver {

	int type;
	private ArrayList<String> arraystring;
	private String string;
	private int start;
	private int end;

	static int stringtype = 1;
	static int arraytype = 2;

	public ArrayList<String> getArraystring() {
		return arraystring;
	}

	public void setArraystring(ArrayList<String> arraystring) {
		this.arraystring = arraystring;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void makearray(ArrayList<String> array) {
		this.type = arraytype;
		this.setArraystring(array);
	}

	public void makestring(String string) {
		this.type = stringtype;
		this.setString(string);
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public static String[] splitcustom(String input, String split) {

		Arraysolver rresult = notor(input);

		ArrayList<String> temparray = new ArrayList<String>();
		if (rresult.type == Arraysolver.arraytype) {
			temparray.addAll(rresult.getArraystring());
		} else {
			temparray.add(rresult.getString());
		}

		String[] somearray;
		String tempstring;
		int checkmax = 0;

		for (int i = 0; i < temparray.size(); i++) {
			tempstring = temparray.get(i).replaceAll(" ", "");
			somearray = tempstring.split(split);
			if (checkmax < somearray.length)
				checkmax = somearray.length;
		}

		String[] returnarray = new String[checkmax];
		Set<String> tempset = new HashSet<String>();
		ArrayList<Set<String>> arrayset = new ArrayList<Set<String>>();

		for (int i = 0; i < checkmax; i++) {
			Set<String> set = new HashSet<String>();
			arrayset.add(set);
		}

		for (int i = 0; i < returnarray.length; i++) {
			returnarray[i] = "(";
		}

		for (int i = 0; i < temparray.size(); i++) {
			somearray = temparray.get(i).split(split);

			for (int j = 0; j < somearray.length; j++) {
				arrayset.get(j).add(somearray[j]);
			}

		}

		for (int i = 0; i < returnarray.length; i++) {
			tempset = arrayset.get(i);
			for (Iterator iterator = tempset.iterator(); iterator.hasNext();) {
				String set = (String) iterator.next();
				returnarray[i] += set;

				if (iterator.hasNext())
					returnarray[i] += "|";
			}
		}

		for (int i = 0; i < returnarray.length; i++) {
			returnarray[i] += ")";
		}

		return returnarray;
	}

	public static Arraysolver notor(String input) {
		
		
		ArrayList<Integer> start = new ArrayList<Integer>();

		ArrayList<Arraysolver> arraysolverlist = new ArrayList<Arraysolver>();
		String normalstring = "";
		
		long end = System.currentTimeMillis();

		if(( end - Constants.starttime )/1000.0>10 && Constants.istimestart)
			return possibleString(arraysolverlist);
		
		// System.out.println("notor: " + input);

		// first iterator
		for (int i = 0; i < input.length(); i++) {
			char now = input.charAt(i);
			// meet (
			if (input.charAt(i) == '(') {
				Arraysolver ar = new Arraysolver();
				// Most outside "("
				// Ex) a((b)(c)d) -> 2nd index.
				if (normalstring.length() > 0 && start.size() == 0) {
					ar.makestring(normalstring.toString());
					// System.out.println("normalstring " + normalstring);

					arraysolverlist.add(ar);
				}
				normalstring = "";
				start.add(i);
			}
			// meet )
			else if (input.charAt(i) == ')') {
				if (start.size() > 1) {
					start.remove(start.size() - 1);
				}
				// Most outside ")"
				// Ex) a((b)(c)d) -> last index.
				else if (start.size() == 1) {
					int startindex = start.remove(start.size() - 1);
					String newstring = input.substring(startindex + 1, i);
					arraysolverlist.add(oror(newstring));

					// System.out.println("orcasestring " + newstring);
					normalstring = "";

				} else {
					System.out.println("Invalid REGEX regex= " + input);
					Arraysolver errorarray = new Arraysolver();
					return errorarray;
				}

			}
			// othercase
			else
				normalstring += input.charAt(i);

		}

		if (start.size() > 0) {
			System.out.println("Invalid REGEX regex= " + input);

			Arraysolver errorarray = new Arraysolver();
			return errorarray;
		}
		if (!input.contains("(")) {
			Arraysolver temp = new Arraysolver();
			temp.setString(normalstring);
			arraysolverlist.add(temp);
		} else if (!normalstring.isEmpty())
			arraysolverlist.add(notor(normalstring));

		return possibleString(arraysolverlist);
	}

	private static Arraysolver oror(String input) {
		ArrayList<Integer> start = new ArrayList<Integer>();

		ArrayList<Arraysolver> arraysolverorlist = new ArrayList<Arraysolver>();

		String normalstring = "";
		int orcase = 0;
		
		long end = System.currentTimeMillis();

		if(( end - Constants.starttime )/1000.0>10 && Constants.istimestart)
			return orpossiblestring(arraysolverorlist);

		// System.out.println("oror: " + input);

		int firststart = 0;
		int numofstart = 0;
		// first iterator
		for (int i = 0; i < input.length(); i++) {

			char now = input.charAt(i);
			// meet (
			if (input.charAt(i) == '(') {
				start.add(i);
			}
			// meet )
			else if (input.charAt(i) == ')') {
				start.remove(start.size() - 1);

			}
			// divided or
			else if (input.charAt(i) == '|' && start.size() == 0) {
				orcase++;

				normalstring = input.substring(firststart, i);

				Arraysolver ortemp = notor(normalstring);
				arraysolverorlist.add(ortemp);
				firststart = i + 1;
			}

		}

		normalstring = input.substring(firststart);
		Arraysolver ortemp = notor(normalstring);
		arraysolverorlist.add(ortemp);

		assert (orcase + 1 == arraysolverorlist.size());

		return orpossiblestring(arraysolverorlist);
	}

	private static Arraysolver orpossiblestring(
			ArrayList<Arraysolver> arraysolverorlist) {
		Arraysolver returnarraysolver = new Arraysolver();
		ArrayList<String> returnarray = new ArrayList<String>();

		for (int i = 0; i < arraysolverorlist.size(); i++) {
			ArrayList<Arraysolver> templist = new ArrayList<Arraysolver>();
			ArrayList<String> partorarray = new ArrayList<String>();
			templist.add(arraysolverorlist.get(i));
			if (possibleString(templist).type == Arraysolver.arraytype) {
				partorarray = possibleString(templist).getArraystring();
				returnarray.addAll(partorarray);
			}

			else {
				returnarray.add(possibleString(templist).getString());
			}

		}
		returnarraysolver.makearray(returnarray);
		return returnarraysolver;

	}

	private static Arraysolver possibleString(
			ArrayList<Arraysolver> arraysolverorlist) {

		Arraysolver temp;

		int numofstring = 1;

		// possible string set.
		ArrayList<Integer> possibleint = new ArrayList<Integer>();
		ArrayList<Integer> nowint = new ArrayList<Integer>();

		for (int i = 0; i < arraysolverorlist.size(); i++) {
			temp = arraysolverorlist.get(i);

			if (temp.type == Arraysolver.arraytype) {
				possibleint.add(temp.getArraystring().size());
				numofstring = numofstring * temp.getArraystring().size();
			} else {
				possibleint.add(1);
			}

		}

		Arraysolver returnarraysolver = new Arraysolver();
		ArrayList<String> returnarray = new ArrayList<String>();
		String tempstring = "";

		for (int i = 0; i < numofstring; i++) {
			long end = System.currentTimeMillis();

			if(( end - Constants.starttime )/1000.0>10 && Constants.istimestart)
				return returnarraysolver;
			
			nowint = whatisnext(i, possibleint, numofstring);
			for (int j = 0; j < possibleint.size(); j++) {
				temp = arraysolverorlist.get(j);
				if (temp.type == Arraysolver.arraytype)
					tempstring += temp.getArraystring().get(nowint.get(j));
				else
					tempstring += temp.getString();
			}
			returnarray.add(tempstring);
			tempstring = "";
		}

		if (returnarray.size() == 1)
			returnarraysolver.makestring(returnarray.get(0));
		else
			returnarraysolver.makearray(returnarray);

		return returnarraysolver;
	}

	private static ArrayList<Integer> whatisnext(int i,
			ArrayList<Integer> possibleint, int numofstring) {
		ArrayList<Integer> returnarray = new ArrayList<Integer>();
		int nownumofstring = numofstring;
		int now = i;

		for (int j = 0; j < possibleint.size(); j++) {
			int divider = nownumofstring / possibleint.get(j);
			if (possibleint.get(j) != 1)
				returnarray.add(now / divider);
			else
				returnarray.add(0);
			now = now - now / divider * divider;
			nownumofstring = nownumofstring / possibleint.get(j);
		}

		return returnarray;
	}

}
