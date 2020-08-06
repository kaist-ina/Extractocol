package extractocol.common.valueEntry.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map.Entry;

import extractocol.common.tools.Pair;
import extractocol.common.valueEntry.ValueEntry;

@SuppressWarnings("serial")
public class Map extends ValueEntry implements Serializable {
	ArrayList<ArrayList<Pair>> mapList = new ArrayList<ArrayList<Pair>>();
	
	public Map(String key, String value){
		if(key != null && value != null){
			ArrayList<Pair> newMap = new ArrayList<Pair>();
			newMap.add(new Pair(key, value));
			this.mapList.add(newMap);
		}
	}
	
	/** Get list of maps **/
	public ArrayList<ArrayList<Pair>> getMapLists(){ return this.mapList; }
	
	/** Get type of this entry **/
	public SOURCE_TYPE getSourceType() { return SOURCE_TYPE.MAP;}

	/** Clear this instance
	 */
	public void Clear(){
		if(this.mapList != null){
			for(int i = 0; i < this.mapList.size(); i++){
				ArrayList<Pair> pList = this.mapList.get(i);
				for(int j = 0; j < pList.size(); j++){
					Pair p = pList.get(j);
					p = null;
				}
				pList.clear();
				pList = null;
			}
			this.mapList.clear();
			this.mapList = null;
		}
	}
	
	/** Clone this instance
	 */
	public Map Clone(){
		Map newObject = new Map(null, null);
		
		for(ArrayList<Pair> l : this.mapList){
			ArrayList<Pair> newList = new ArrayList<Pair>();
			
			for(Pair p:l)
				newList.add(p.Clone());
			
			newObject.getMapLists().add(newList);
		}
		
		return newObject;
	}
	
	/** Generate Regex **/
	public String GenRegex(){
		String result = "";
		for (ArrayList<Pair> pairList : this.mapList)
		{
			for(Pair p : pairList)
			{
				result += p.getKey() + "#:#" + p.getValue() + "###";
			}
			result += "|";
		}
		return result.substring(0, result.length()-1);
	}
	
	/**********************************************************************/
	/**                             Add & Set                            **/
	/**********************************************************************/
	/** Add pair(key,value) after removing the existing maps
	 * 
	 * @param key Key
	 * @param value Value
	 */
	public void setEntry(String key, String value){
		if(key != null && value != null){
			this.mapList = new ArrayList<ArrayList<Pair>>();
			ArrayList<Pair> newMap = new ArrayList<Pair>();
			newMap.add(new Pair(key, value));
			this.mapList.add(newMap);
		}
	}
	
	/** Add pair(key,value) (Except duplicated one)
	 * 
	 * @param key Key
	 * @param value Value
	 */
	public void addEntry(String key, String value){
		if(key != null && value != null)
			for(ArrayList<Pair> map : this.mapList)
				if(!doesContainKey(map, key))
					map.add(new Pair(key, value));
	}
	
	/** Add all maps of 've' after removing the existing maps
	 * 
	 * @param ve Map entry
	 */
	public void setEntry(ValueEntry ve){
		if(ve.getSourceType() != this.getSourceType())
			return;
		
		this.mapList = ((Map)ve).getMapLists();
	}
	
	/** Merge all maps of 've' with the existing map list (Except duplicated ones)
	 * 
	 * @param ve Map entry
	 */
	@SuppressWarnings("unchecked")
	public void addEntry(ValueEntry ve){
		if(ve.getSourceType() != this.getSourceType())
			return;
		
		// Old code
//		for(ArrayList<Pair> map : ((Map)ve).getMapLists()){
//			if(!doesContainMap(map, this.mapList))
//				this.mapList.add((ArrayList<Pair>)map.clone());
//		}
		
		int newSize = ((Map)ve).getMapLists().size();
		int existingSize = this.mapList.size(); 
		if( newSize > 1 ||  existingSize > 1){
			System.err.println("This map value entry has more than 2 of Maps!!");
			return;
		}
		
		if (newSize == 0 && existingSize == 0)
			return;
		else if (newSize == 0 && existingSize == 1)
			return;
		else if (newSize == 1 && existingSize == 0)
			this.mapList.add((ArrayList<Pair>)((Map)ve).getMapLists().get(0).clone());
		else if (newSize == 1 && existingSize == 1){
			if(isInclusionRelation(this.mapList.get(0), ((Map)ve).getMapLists().get(0)))
				return;
			else{
				this.Clear();
				this.mapList = new ArrayList<ArrayList<Pair>>();
				this.mapList.add((ArrayList<Pair>)((Map)ve).getMapLists().get(0).clone());
			}
		}
	}
	
	
	/**********************************************************************/
	/**                               Tools                              **/
	/**********************************************************************/
	private static boolean isInclusionRelation(ArrayList<Pair> big, ArrayList<Pair> small){
		for(Pair p1: small){
			boolean isPairIncluded = false;
			
			for(Pair p2: big){
				if(p1.getKey().equals(p2.getKey()) && p1.getValue().equals(p2.getValue())){
					isPairIncluded = true;
					break;
				}
			}
			
			if(!isPairIncluded)
				return false;
		}
		
		return true;
	}
	
	private static boolean OneOfTwoIncludeAnother(ArrayList<Pair> map1, ArrayList<Pair> map2){
		return isInclusionRelation(map1, map2) || isInclusionRelation(map2, map1);
	}
	
	private static boolean areSame(ArrayList<Pair> map1, ArrayList<Pair> map2){
		if(map1.size() != map2.size())
			return false;
		
		return isInclusionRelation(map1, map2);
	}
	
	private static boolean OneOfTwoIncludeAnotherWithMapList(ArrayList<Pair> map, ArrayList<ArrayList<Pair>> mapList){
		for(ArrayList<Pair> target : mapList){
			if(OneOfTwoIncludeAnother(target, map))
				return true;
		}
		
		return false;
	}
	
	private static boolean doesContainMap(ArrayList<Pair> map, ArrayList<ArrayList<Pair>> mapList){
		for(ArrayList<Pair> target : mapList){
			if(areSame(target, map))
				return true;
		}
		
		return false;
	}
	
	private static boolean doesContainKey(ArrayList<Pair> map, String key){
		for(Pair pair: map)
			if(pair.getKey().equals(key))
				return true;
		return false;
	}
}
