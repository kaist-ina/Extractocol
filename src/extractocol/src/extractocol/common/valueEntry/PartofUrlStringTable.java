package extractocol.common.valueEntry;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import extractocol.Constants;
import extractocol.common.outputs.backendOutputHelper.RequestInfoEntry;
import extractocol.common.trackers.tools.General;
import extractocol.common.valueEntry.node.PartofUrlString;
import extractocol.tester.HeapHandling;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Created by Jeongmin on 2017-05-19.
 * This class manages remaining url strings as a HashMap.
 * The key of this map is heap string. And the value of this map is a arraylist<String> of remaining string.
 */
public class PartofUrlStringTable {
    private static HashMap<PartUrlHeader, ArrayList<PartofUrlString>> RemainTable = new HashMap<>();
    private static HashMap<String, ArrayList<String>> HeapConstantTable = new HashMap<>();

    public static boolean LazySemantic(RequestInfoEntry rie)
    {
        for (PartUrlHeader _puh : RemainTable.keySet())
        {
            Matcher m = HeapHandling.heapPattern.matcher(rie.getSignature());
            String heap = "";
            while(m.find()) {
                heap = m.group();

                if (_puh.isEqual(new PartUrlHeader(heap, rie.getEPorSPMethod()))) {
                    applySemantic(rie, _puh);
                    return true;
                }
            }
        }
        return false;
    }

    public static void getSameStrings(String first, ArrayList<String> result)
    {
        if (HeapConstantTable.containsKey(first))
        {
            result.addAll(HeapConstantTable.get(first));

            for (String entity : result)
            {
                if (entity.contains("<") && entity.contains(">"))
                    getSameStrings(entity, result);
            }
        }
        else
            return;
    }

    public static void addHeapConstantList(String heap, String includingheap)
    {
        ArrayList<String> subheaps = new ArrayList<>();
        Matcher m = HeapHandling.heapPattern.matcher(includingheap);
        while (m.find()) {
            // 1. Extract heap
            heap = m.group();
            subheaps.add(heap);
        }
        HeapConstantTable.put(heap, subheaps);
        return;
    }

    public static void addRemainString(String _HeapStr, String _EP, PartofUrlString _value)
    {
        PartUrlHeader puh = new PartUrlHeader(_HeapStr, _EP);
        ArrayList<PartofUrlString> dest;
        if(RemainTable.containsKey(puh))
           dest = RemainTable.get(puh);
        else
            dest = new ArrayList<>();
        dest.add(_value);
        RemainTable.put(puh, dest);
    }

    private static ArrayList<PartofUrlString> getPartofUrlStringList(PartUrlHeader _puh)
    {
        for (PartUrlHeader thispuh : RemainTable.keySet())
        {
            if (thispuh.isEqual(_puh))
                return RemainTable.get(thispuh);
        }
        return null;
    }

    public static void applySemantic(RequestInfoEntry rie, PartUrlHeader puh)
    {
        ArrayList<PartofUrlString> target = getPartofUrlStringList(puh);
        String targetEp = rie.getEPorSPMethod();

        if (target != null)
        {
            for (PartofUrlString pus : target)
            {
                if (pus.getSemantic().equals("<java.lang.String: java.lang.String format(java.lang.String,java.lang.Object[])>"))
                {
                    //Remove Lazy Semantic Tag in a normal signature.
                    String plainHeap = rie.getSignature().replace("Remain:"+targetEp , "");
                    rie.setSignature(rie.getSignature().replace("Remain:"+targetEp , ""));

                    Set<String> arg0results = retreiveConstants(puh.getHeapStr());
                    if (arg0results.size() == 0) {
                        plainHeap = plainHeap.replace(puh.getHeapStr(), General.getValueEntryList(puh.getHeapStr()).GenRegex() /*replace existing one with 'General' by BK*/);
                        //rie.setRegexSignature(String.format(plainHeap, pus.getValues()));
                    }
                    //TODO The results of these above blocks must include first heap's result.
                    else if (arg0results.size() == 1)
                    {
                        plainHeap = plainHeap.replace(puh.getHeapStr(), arg0results.toArray()[0].toString());
                        //rie.setRegexSignature(String.format(plainHeap, pus.getValues()));
                    }
                    else if (arg0results.size() > 1) {
                        //TODO Jeongmin has to implement this section.
                        //When arg0 var has multiple candidates.
                    }
                }
            }
        }
    }

    private static Set<String> retreiveConstants(String heapStr) {

        ArrayList<String> baseOrStrings = new ArrayList<>();
        getSameStrings(heapStr, baseOrStrings);

        Set<String> result = new HashSet<>();

        for (String heapentity : baseOrStrings)
            result.add(General.getValueEntryList(heapentity).GenRegex()/*replace existing one with 'General' by BK*/);

        return result;
    }

    public static void serialize()
    {
        String path = Constants.getPartofUrlStringPath();
        Kryo kryo = new Kryo();
        Output output;
        try
        {
            output = new Output(new FileOutputStream(path));
            kryo.writeObject(output, RemainTable);
            output.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

//        for (ArrayList<PartofUrlString> arr : RemainTable.values())
//            for (PartofUrlString val : arr) {
//                System.out.print("[Part of String]: ");
//                for (String parturl : val.getValues())
//                    System.out.print(parturl + " ");
//                System.out.println(" in " + val.getSemantic());
//            }
    }

    public static void deserialize()
    {
        String path = Constants.getPartofUrlStringPath();
        Kryo kryo = new Kryo();
        Input input = null;
        try {
            input = new Input(new FileInputStream(path));
            RemainTable = kryo.readObject(input, HashMap.class);
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
