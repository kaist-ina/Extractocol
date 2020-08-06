package extractocol.common.tools.QueryConvertor;

import javax.sql.rowset.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class QueryConvertor {

    private static String query;
    private static HashMap<String, ArrayList<String>> prtable = new HashMap<>();

    public static void main (String args[])
    {
        query = "(.*)|((.*)|((.*)?contest_id=((.*)|(.*))&w=-1&h=-1&m=-1|(.*)?contest_id=((.*)|(.*))&w=-1&h=-1&m=-1&s=((.*)|(.*)))|((.*)|((.*)|((.*)?contest_id=((.*)|(.*))&w=-1&h=-1&m=-1|(.*)?contest_id=((.*)|(.*))&w=-1&h=-1&m=-1&s=((.*)|(.*)))))?((.*)|((.*)|((.*)?contest_id=((.*)|(.*))&w=-1&h=-1&m=-1|(.*)?contest_id=((.*)|(.*))&w=-1&h=-1&m=-1&s=((.*)|(.*))))))";
        Pattern pattern = Pattern.compile(query);
    }
}
