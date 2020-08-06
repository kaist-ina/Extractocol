package QueryConvertor;

import extractocol.common.outputs.BackendOutput;
import extractocol.common.valueEntry.ValueEntryTable;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import pcreparser.PCRE;
import pcreparser.PCREParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryConverter {

    static public String query = "(.*)|((.*)|((.*)?contest_id=((.*)|(.*))&w=-1&h=170&m=-1|(.*)?contest_id=((.*)|(.*))&w=-1&h=170&m=-1&s=((.*)|(.*)))|((.*)|((.*)|((.*)?contest_id=((.*)|(.*))&w=-1&h=170&m=-1|(.*)?contest_id=((.*)|(.*))&w=-1&h=170&m=-1&s=((.*)|(.*)))))?((.*)|((.*)|((.*)?contest_id=((.*)|(.*))&w=-1&h=180&m=-1|(.*)?contest_id=((.*)|(.*))&w=-1&h=170&m=-1&s=((.*)|(.*))))))";
    static private boolean printresult = false;

    public static void main (String args[])
    {
        try {
            try (BufferedReader br = new BufferedReader(new FileReader("d:\\RegexSig_flipa.txt"))) {
                String line;
                try {
                    while ((line = br.readLine()) != null) {
                        // process the line.
                        System.out.println(line);
                        System.out.println(convert(BackendOutput.urlRefinement(line.substring(line.indexOf("(.*|.*/)"),line.length()))));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


//        System.out.println(convert(BackendOutput.urlRefinement(query)));
    }

    public static HashMap<String, String> convert(String query)
    {
        if (query.contains("?") || query.contains("&")) {
            PCRE pcre = new PCRE(query);
            StringBuilder sb = new StringBuilder();
            walk(pcre.getCommonTree(), sb);

            String result = sb.toString();
            ArrayList<String> mergedarr = new ArrayList<>();
            String merged = mergeresult(result);
            mergedarr.addAll(new ArrayList<String>(Arrays.asList(merged.split("\n"))));
            return refineparams(mergedarr);
        }
        return null;
    }
    
    public static HashMap<String, String> convert2(String query){
    	String[] sp = query.split("(\\?|\\&)");
    	if(sp == null) 
    		return null;
    	
    	ValueEntryTable vet = new ValueEntryTable();
    	for(String e: sp) {
    		if(!e.contains("="))
    			continue;
    		
    		String[] res = e.split("=");
    		String key = res[0];
    		String val = (res.length == 2)? res[1] : "";
    		vet.addConstantValue(key, val, false);
    	}
    	
    	HashMap<String, String> result = new HashMap<String, String>();
    	for(String key: vet.getValueEntryTable().keySet()) {
    		result.put(key, vet.GenRegex(key, false));
    	}
    	
    	return result;
    }

    private static String partofparams(String query) {
//        "";

        Pattern p = Pattern.compile("^((http[s]?|ftp):\\/)?\\/?([^:\\/\\s]+)((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?$");

        int onlyNum;
        Scanner iStream = new Scanner(System.in);

        Matcher m = p.matcher(query);

        if (m.find())
            System.out.println(m.group(7));
        System.exit(0);

        return "";


    }

    private static HashMap<String, String> refineparams(ArrayList<String> mergedstr)
    {
        HashMap<String, String> result = new HashMap<>();

        for (String a : mergedstr)
        {
            if (a.startsWith("ANY"))
                a = a.replaceFirst("ANY", "");

            if (a.contains("?")) {
                if (!a.startsWith("?"))
                    a = a.substring(a.indexOf("?"), a.length());

                a = a.replaceFirst("\\?", "");

            }

            String[] paramkv = a.split("&");

            for (String kvpair: paramkv)
            {
                if (!kvpair.equals("")) {
                    String kventry[] = kvpair.split("=");

                    if (kventry.length < 2)
                        continue;

                    String finalvalue = kventry[1].replaceAll("ANY", "(\\.*)|");
                    if (finalvalue.endsWith("|"))
                        finalvalue = finalvalue.substring(0, finalvalue.length()-1);

                    if (result.containsKey(kventry[0]))
                    {
                        String getval = result.get(kventry[0]);
                        if (!getval.equals(finalvalue))
                        {
                            HashSet<String> vset = new HashSet<>(Arrays.asList(getval.split("\\|")));
                            vset.addAll(Arrays.asList(finalvalue.split("\\|")));
                            String res = "";
                            for (String v : vset) {
                                if (v.equals("?"))
                                    continue;
                                res += v + "|";
                            }
                            result.put(kventry[0], res.substring(0, res.length()-1));
                        }
                    }
                    else
                        result.put(kventry[0], finalvalue);
                }
            }
        }

        if (printresult) {
            for (String k : result.keySet()) {
                System.out.println(k + " : " + result.get(k));
            }
        }
        return result;
    }

    private static String mergeresult(String result) {
        StringBuilder re = new StringBuilder();
        for (String a : result.split("###")) {
            if (!a.equals("") && !a.equals("ANY")) {
                if (a.startsWith("ANY") || a.startsWith("?"))
                    re.append("\n" + a);
                else
                    re.append("|" + a);
            }
        }
        return re.toString();
    }


    private static void walk(CommonTree tree, StringBuilder builder) {

        List<CommonTree> firstStack = new ArrayList();
        firstStack.add(tree);
        List<List<CommonTree>> childListStack = new ArrayList();
        childListStack.add(firstStack);
        boolean isequal = false;
        int prevANYlv = 0;
        Tree prevANY = null;

        int prevLiterlv = 0;
        Tree prevLiter = null;


        while(true) {
            while(!childListStack.isEmpty()) {
                List<CommonTree> childStack = (List)childListStack.get(childListStack.size() - 1);
                if (childStack.isEmpty()) {
                    childListStack.remove(childListStack.size() - 1);
                } else {
                    tree = (CommonTree)childStack.remove(0);
                    String indent = "";
                    int lv=0;

                    for(int i = 0; i < childListStack.size() - 1; ++i) {
                        lv += ((List)childListStack.get(i)).size();
                    }

                    String tokenName = PCREParser.tokenNames[tree.getType()];
                    String tokenText = tree.getText();


                    if (tokenName.equals("OR")) {
                        if (!isequal)
                            builder.append("###");
                    }
                    else if (tokenName.equals("LITERAL") || tokenName.equals("ANY")) {
                        if (tokenName.equals("LITERAL")) {
                            if (tree.getText().equals("=")) {
                                isequal = true;
                                prevLiter = tree.getParent().getParent();
                            }
                            else
                            {
                                isequal = false;
                            }
                            if (tree.getAncestors().size() > 3)
                                prevLiter = tree.getAncestors().get(tree.getAncestors().size() - 3);
                        }
                        else if (tokenName.equals("ANY")) {
                            if (!builder.toString().endsWith("=")) {
                                if (prevANYlv > tree.getAncestors().size()) {
                                    isequal = false;
                                    builder.append("###");
                                } else {
                                    if (builder.toString().endsWith("ANY") && !tree.getAncestors().get(tree.getAncestors().size() - 6).equals(prevANY)) {
                                        isequal = false;
                                        builder.append("###");
                                    }
                                }
                            }
                            prevANYlv = tree.getAncestors().size();
                            if (tree.getAncestors().size() > 6)
                                prevANY = tree.getAncestors().get(tree.getAncestors().size() - 6);
                        }

                        builder.append(tree.getText());
                    }
                    else {
                        if (tree.getParent() != null)
                            if (PCREParser.tokenNames[tree.getParent().getType()].equals("OR") && tokenName.equals("ALTERNATIVE")) {
                                if (!isequal)
                                    builder.append("###");
                            }
                    }

                    if (tree.getChildCount() > 0) {
                        childListStack.add(new ArrayList(tree.getChildren()));
                    }
                }
            }
            return;
        }
    }
}
