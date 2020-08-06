/*package extractocol.tester;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;	
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.tree.TreeNode;

import org.antlr.runtime.tree.CommonTree;

import QueryConvertor.QueryConverter;
import extractocol.Constants;
import extractocol.common.outputs.backendOutputHelper.ReqRespInfo;
import extractocol.common.regex.RegexHandler;
import extractocol.common.regex.basic.RegexNode;
import extractocol.common.retrofit.RetrofitBaseURLTracker;
import extractocol.common.retrofit.RetrofitParse;
import extractocol.common.retrofit.struct.Transaction;
import extractocol.common.retrofit.utils.FileAnalyzer;
import extractocol.common.retrofit.utils.InnerClassAnalyzer;
import extractocol.common.retrofit.utils.JavaFileAnalyzer;
import extractocol.common.retrofit.utils.MethodPrototype;
import extractocol.common.retrofit.utils.ResponseFileAnalyzer;
import extractocol.common.trackers.ImplicitCallEdgeTracker;
import extractocol.common.trackers.IntentMapTracker;
import extractocol.common.trackers.tools.ArgToVEL;
import extractocol.common.trackers.tools.HeapToString;
import extractocol.common.trackers.tools.HeapToVEL;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.frontend.helper.PropagateHelper;
import extractocol.frontend.output.basic.TaintResultEntry;
import javafx.util.Pair;
import pcreparser.PCRE;
import pcreparser.PCRELexer;
import pcreparser.PCREParser;
import sun.misc.Queue;

import org.antlr.misc.Graph;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.DOTTreeGenerator;
import org.antlr.stringtemplate.StringTemplate;

public class ForTest {

	static int columnForRow[] = new int[8];
	
	public static void main(String[] args) {
		String str = "abcd";
		//permutation(str);
		char[] cList = str.toCharArray();
		
		parentheses(3);
		
		System.out.println();

		
	}
	
	public static void parentheses(int n) {
		parentheses(n, "");
	}
	
	public static void parentheses (int n, String prefix) {
		int len = prefix.length();
		if(len == n * 2) {
			System.out.println(prefix);
			return;
		}
		
		// 1. count '(' and ')'
		int cnt1 = 0;
		int cnt2 = 0;
		for(int i = 0; i < len; i++)
			if(prefix.charAt(i) == '(')
				cnt1++;
			else
				cnt2++;

		// 0: '(', 1: ')'
		if(cnt1 < n)
			parentheses (n, prefix + "(");
		
		if(cnt2 < cnt1)
			parentheses (n, prefix + ")");
		
	}
	
	public static void permutation(String s) {
		permutation("", s);
	}
	
	public static void permutation(String prefix, String restStr) {
		int n = restStr.length();
		if(n == 0)
			System.out.println(prefix);
		
		for(int i = 0; i < n; i++) {
			permutation(prefix + restStr.charAt(i), restStr.substring(0, i)  0 to i-1  + restStr.substring(i+1, n) i+1 to n-1 );
		}
	}
	
	public static int fibonacci(int n) {
		if ( n < 0 )
			return -1;
	
		if (n == 0)
			return 0;
		else if (n == 1)
			return 1;
		else
			return fibonacci(n-2) + fibonacci(n-1);
	}
	
	public static int possiblePaths(int n) {
		return possiblePaths(n, 0, 0, 0);
	}
	
	public static int possiblePaths(int n, int x, int y, int cnt) {
		// check it arrives at the bottom right hand corner
		if(x == n-1 && y == n-1)
			return cnt + 1;
		
		// go down
		if(y < n - 1)
			cnt = possiblePaths(n, x, y + 1, cnt);
		
		// go right
		if(x < n - 1)
			cnt = possiblePaths(n, x + 1, y, cnt);
		
		return cnt;
	}
	
	public static int test() {
		try {
			int a= 10;
			System.out.println(a);
			return 0;
		}catch (Exception e){
			return -1;
		}finally {
			System.out.println("finally block");
			return 1;
		}
	}
	
	public static ArrayList<HtWt> getMaxSeq(ArrayList<HtWt> origList){
		if(origList == null || origList.size() == 0)
			return null;
		
		// 1. sort the orig list
		Collections.sort(origList);
		
		// 2. get max sequence
		ArrayList<HtWt> maxSeq = null;
		int fUnfitIdx = 0;
		
		while(true) {
			// allocate a new sequence
			ArrayList<HtWt> newSeq = new ArrayList<HtWt>();
			int prevFirstUnfitIdx = fUnfitIdx;
			
			// get the index of the first unfit entry
			fUnfitIdx = getUnfitIdxAndNewSeq(prevFirstUnfitIdx, origList, newSeq);
			
			// get the longest sequence
			maxSeq = getLongestOne(maxSeq, newSeq);
						
			// break condition
			if(fUnfitIdx == prevFirstUnfitIdx)
				break;
		}
		
		return maxSeq;
	}
	
	private static int getUnfitIdxAndNewSeq(int prevUnfitIdx, ArrayList<HtWt> origList, ArrayList<HtWt> newSeq) {
		int firstUnfitIdx = prevUnfitIdx;
		
		for(int i = prevUnfitIdx; i < origList.size(); i ++) {
			if(i == prevUnfitIdx) {
				newSeq.add(origList.get(i));
				continue;
			}
			
			if(isAbleToCome(newSeq.get(newSeq.size() - 1), origList.get(i))) {
				newSeq.add(origList.get(i));
				continue;
			}
			
			if(firstUnfitIdx == prevUnfitIdx)
				firstUnfitIdx = i;
		}
		
		return firstUnfitIdx;
	}
	
	private static boolean isAbleToCome(HtWt hw1, HtWt hw2) {
		return (hw1.Ht <= hw2.Ht) && (hw1.Wt <= hw2.Wt);
	}
	
	private static ArrayList<HtWt> getLongestOne(ArrayList<HtWt> seq1, ArrayList<HtWt> seq2){
		if (seq1 == null && seq2 == null)
			return null;
		
		if (seq1 != null && seq2 == null)
			return seq1;
		
		if (seq1 == null && seq2 != null)
			return seq2;
		
		return (seq1.size() > seq2.size())? seq1 : seq2;
	}
	
	public static int[] mergeSort2(int[] arr) {
		mergeSort(arr, 0, arr.length - 1, new int[arr.length]);
		return arr;
	}
	
	private static void mergeSort(int[] arr, int start, int end, int[] buffer) {
		if(start >= end)
			return;
		
		int mid = (start + end) / 2;
		mergeSort(arr, start, mid, buffer);
		mergeSort(arr, mid + 1, end, buffer);
		merge(arr, start, mid, end, buffer);
	}
	
	private static void merge(int[] arr, int start, int mid, int end, int[] buffer) {
		for(int i = start; i <= end; i++)
			buffer[i] = arr[i];
		
		int lIdx = start;
		int rIdx = mid + 1;
		int fIdx = start;
		
		while(lIdx <= mid && rIdx <= end) {
			if(buffer[lIdx] < buffer[rIdx])
				arr[fIdx++] = buffer[lIdx++];
			else
				arr[fIdx++] = buffer[rIdx++];
		}
		
		while(lIdx <= mid) {
			arr[fIdx++] = buffer[lIdx++];
		}
		
		while(rIdx <= end) {
			arr[fIdx++] = buffer[rIdx++];
		}
	}
	
	public static void mergeSort(int[] arr) {
		if (arr.length < 2)
			return;
		
		int mid = arr.length / 2;
		int[] left = getNewArr(arr, 0, mid - 1);
		int[] right = getNewArr(arr, mid, arr.length - 1);
		
		mergeSort(left);
		mergeSort(right);
		merge(left, right, arr);
	}
	
	private static int[] getNewArr(int[] arr, int start, int end) {
		int[] newArr = new int[end - start + 1];
		for(int i = start, k = 0; i <= end; i++, k++)
			newArr[k] = arr[i];
		
		return newArr;
	}
	
	private static void merge(int[] left, int[] right, int[] arr) {
		int lSize = left.length;
		int rSize = right.length;
		
		int lIdx = 0;
		int rIdx = 0;
		int oIdx = 0;
		
		while(lIdx < lSize && rIdx < rSize) {
			if(left[lIdx] < right[rIdx]) {
				arr[oIdx++] = left[lIdx++];
			}else
				arr[oIdx++] = right[rIdx++];
		}
		
		while(lIdx < lSize) {
			arr[oIdx++] = left[lIdx++];
		}
		
		while(rIdx < rSize) {
			arr[oIdx++] = right[rIdx++];
		}
	}
	
	public static void quickSort(int[] arr, int start, int end) {
		if(start >= end)
			return;
		
		int pIdx = partition(arr, start, end);
		quickSort(arr, start, pIdx - 1);
		quickSort(arr, pIdx + 1, end);
	}
	
	private static int partition(int[] arr, int start, int end) {
		int pivot = arr[end];
		int pIdx = start;
		
		for(int i = 0; i < end -1; i++) {
			if(arr[i] < pivot) {
				swap(arr, i, pIdx++);
			}
		}
		swap(arr, end, pIdx);
		return pIdx;
	}
	
	private static void swap(int[] arr, int i1, int i2) {
		int tmp = arr[i1];
		arr[i1] = arr[i2];
		arr[i2] = tmp;
	}
	
	
	
	public static myPos findElem(int[][] matrix, int tgt) {
		// 1. find row by using modified binary search
		int row = findRow(matrix, tgt);
		if(row == -1)
			return null;
		
		// 2. find column by using ordinary binary search
		int col = findCol(matrix, row, tgt);
		if(col == -1)
			return null;
		
		return new myPos(row, col);
	}
	
	private static int findCol(int[][] matrix, int row, int tgt) {
		int l = 0;
		int h = matrix[0].length - 1;
		
		while( l <= h ) {
			int m = (l + h) / 2;
			if(matrix[row][m] == tgt)
				return m;
			else if(matrix[row][m] < tgt)
				l = m + 1;
			else
				h = m - 1;
		}
		
		return -1;
	}
	
	private static int findRow(int[][] matrix, int tgt) {
		int l = 0;
		int h = matrix.length - 1;
		
		int lIdx = -1;
		
		while( l <= h ) {
			int m = (l + h)/2;
			
			if(matrix[m][0] == tgt)
				return m;
			else if (matrix[m][0] < tgt) {
				l = m + 1;
				lIdx = m;
			}else {
				h = m - 1;
			}
		}
		
		return lIdx;
	}
	
	public static int binarySearch(int[] arr, int tgt) {
		int l = 0;
		int h = arr.length - 1;
		
		while(l <= h) {
			int m = (l+ h) / 2;
			if(arr[m] == tgt)
				return m;
			else if (arr[m] > tgt)
				h = m - 1;
			else 
				l = m + 1;
		}
		
		return -1;
	}
	
	public static int[] sortConvert2(int[] input_arr, int A, int B, int C) {
		int[] convArr = input_arr;
		
		if( A == 0) {
			// 1. convert
			for(int i = 0; i < input_arr.length; i++) {
				convArr[i] = calcConv(input_arr[i], A, B, C);
			}
			
			// 2. Check B
			if(B >= 0) {
				return convArr;
			}else {
				return reverse(convArr);
			}
			
		} else if ( A > 0 ) {
			// 1. convert
			int min = Integer.MAX_VALUE;
			int minIdx = 0;
			
			for(int i = 0; i < input_arr.length; i++) {
				convArr[i] = calcConv(input_arr[i], A, B, C);
				
				if ( convArr[i] < min ) {
					min = convArr[i];
					minIdx = i;
				}
			}
			
			// 2. Check array and Handle (3 cases: (1) all greater than -B/2A, (2) all less than -B/2A, (3) other )
			// (1) just return the converted array
			if(minIdx == 0) {
				return convArr;
			}
			// (2) reverse the converted array
			else if (minIdx == input_arr.length - 1) {
				return reverse(convArr);
			}
			// (3) perform merge sort
			else {
				return mergeSort(convArr, minIdx);
			}
			
		} else { // ( A < 0)
			// 1. convert
			int max = Integer.MIN_VALUE;
			int maxIdx = 0;
			
			for(int i = 0; i < input_arr.length; i++) {
				convArr[i] = calcConv(input_arr[i], A, B, C);
				
				if ( convArr[i] > max ) {
					max = convArr[i];
					maxIdx = i;
				}
			}
			
			// 2. check array and handle (3 cases: (1) all greater than -B/2A, (2) all less than -B/2A, (3) other )
			// (1) reverse the converted array
			if(maxIdx == 0)
				return reverse(convArr);
			// (2) just return the converted array
			else if (maxIdx == input_arr.length - 1)
				return convArr;
			// (3) perform merge sort
			else
				return mergeSort2(convArr, maxIdx);
		}
	}
	
	private static int calcConv(int iVal, int A, int B, int C) {
		return A * iVal * iVal + B * iVal + C;
	}
	
	private static int[] reverse(int[] arr) {
		for(int i = 0; i < arr.length / 2; i++) {
			int tmp = arr[i];
			arr[i] = arr[arr.length - 1 - i];
			arr[arr.length - 1 - i] = tmp;
		}
		
		return arr;
	}
	
	private static int[] mergeSort(int[] arr, int division) {
		int idx = 0;
		int idx1 = division;
		int cnt1 = idx1 + 1;
		
		int idx2 = division + 1;
		int cnt2 = arr.length - cnt1;
		
		// 5 4 3 6 9
		//int[] newArr = new int[arr.length];
		while(cnt1 > 0 && cnt2 > 0) {
			if(arr[idx1] < arr[idx2]) {
				// 1. get value
				int tmp = arr[idx1];
						
				// 2. pull the left part
				pushOnce(arr, idx1 - 1, --cnt1, true reverse);
				
				// 3. set the value
				arr[idx++] = tmp;
			}else {
				// 1. get value
				int tmp = arr[idx2++];
						
				// 2. pull the left part
				pushOnce(arr, idx, cnt1, false reverse);
				
				// 3. set the value
				arr[idx++] = tmp;
				
				idx1++;
				cnt2--;
			}
		}
		
		if(cnt1 > 0) {
			for(int i = 0; i < (arr.length - idx)/2; i--) {
				int tmp = arr[idx+i];
				arr[idx+i] = arr[arr.length - 1 - i];
				arr[arr.length - 1 - i] = tmp;
			}
		}
		
		return arr;
	}
	
	private static void pushOnce(int[] arr, int start, int n, boolean reverse) {
		if(reverse) {
			for(int i = 0; i < n; i++)
				arr[start - i + 1] = arr[start - i];
		}else {
			for(int i = n; i > 0; i--)
				arr[start + i] = arr[start + i - 1];
		}
	}
	
	private static int[] mergeSort2(int[] arr, int division) {
		int idx = arr.length - 1;
		int idx1 = division;
		int idx2 = division + 1;
		
		int[] newArr = new int[arr.length];
		while(idx1 >= 0 && idx2 < arr.length) {
			int val;
			if(arr[idx1] > arr[idx2])
				val = arr[idx1--];
			else
				val = arr[idx2++];
			
			newArr[idx--] = val;
		}
		
		if(idx1 >= 0) {
			for(int i = idx1; i >= 0; i--)
				newArr[idx--] = arr[i];
		}else if(idx2 < arr.length) {
			for(int i = idx2; i < arr.length; i++)
				newArr[idx--] = arr[i];
		}
		
		return newArr;
	}
	
	public static int[] sortConvert(int[] input_arr, int A, int B, int C) {
		// conv[i] = A*x^2 + B*x + C
		
		// 1. convert
		int[] convertedArr = input_arr;
		int divisionValue = (A > 0)? Integer.MAX_VALUE : Integer.MIN_VALUE;
		int dIdx = 0;
		int status = 0;
		
		for(int i = 0; i < input_arr.length; i++) {
			int origValue = input_arr[i];
			convertedArr[i] = A * origValue * origValue + B * origValue + C;
			
			if(A > 0) {
				if (divisionValue > convertedArr[i]) {
					divisionValue = convertedArr[i];
					dIdx = i;
				}
			}else {
				if (divisionValue < convertedArr[i]) {
					divisionValue = convertedArr[i];
					dIdx = i;
				}
			}
			
			if ((-1) * B / (2 * A) <= origValue)
				status++;
			else
				status--;
		}
		
		// 2. sort
		if(Math.abs(status) == input_arr.length) {
			if(A > 0) {
				if(status > 0)
					return convertedArr;
				else {
					for(int i = 0; i < input_arr.length/2; i++) {
						int tmp = convertedArr[i]; 
						convertedArr[i] = convertedArr[convertedArr.length - 1 - i];
						convertedArr[convertedArr.length - 1 - i] = tmp;
					}
					return convertedArr;
				}
			}else {
				if(status < 0)
					return convertedArr;
				else {
					for(int i = 0; i < input_arr.length/2; i++) {
						int tmp = convertedArr[i]; 
						convertedArr[i] = convertedArr[convertedArr.length - 1 - i];
						convertedArr[convertedArr.length - 1 - i] = tmp;
					}
					return convertedArr;
				}
			}
			
		}else {
			// need to sort merge sort 
			int[] sortedArr = new int[input_arr.length];
			// 3 4 5 6 2 1
			// dIdx: 2
			// idx1: 2
			// idx2: 3
			int idx1 = dIdx;
			int idx2 = dIdx + 1;
			
			if(A > 0) {
				int idx = 0;
				
				while(idx1 > 0 && idx2 < input_arr.length) {
					int val;
					if(convertedArr[idx1] < convertedArr[idx2])
						val = convertedArr[idx1--];
					else
						val = convertedArr[idx2++];
					
					sortedArr[idx++] = val; 
				}
				
				if (idx1 >= 0) {
					for(int i = idx1; i >= 0; i--)
						sortedArr[idx++] = convertedArr[i];
				}else if (idx2 < input_arr.length - 1) {
					for(int i = idx2; i < input_arr.length; i++)
						sortedArr[idx++] = convertedArr[i];
				}
			}else {
				int idx = input_arr.length - 1;
				
				while(idx1 > 0 && idx2 < input_arr.length) {
					int val;
					if(convertedArr[idx1] > convertedArr[idx2])
						val = convertedArr[idx1--];
					else
						val = convertedArr[idx2++];
					
					sortedArr[idx--] = val; 
				}
				
				if (idx1 >= 0) {
					for(int i = idx1; i >= 0; i--)
						sortedArr[idx--] = convertedArr[i];
				}else if (idx2 < input_arr.length - 1) {
					for(int i = idx2; i < input_arr.length; i++)
						sortedArr[idx--] = convertedArr[i];
				}
			}
			
			return sortedArr;
		}
	}
	
	public static int find(int[] arr, int tgt) {
		int stride = arr.length;
		int idx = 0;
		
		while(true) {
			if(arr[idx] == tgt)
				return idx;
			
			stride /= 2;
			if(stride < 1)
				stride = 1;
			
			if(arr[idx] < tgt) {
				idx += stride;
				idx = idx % arr.length;
			}else {
				idx -= stride;
				if(idx < 0) {
					idx = idx + arr.length;
				}
			}
		}
	}
	
	public static String[] sortAnagram(String[] ss) {
		ArrayList<ArrayList<Integer>> arr = new ArrayList<ArrayList<Integer>>();
		
		// 1. find indices
		for(int i = 0; i < ss.length; i++) {
			boolean b = false;
			for(ArrayList<Integer> a : arr) {
				if(a.contains(i)) {
					b = true;
					break;
				}
			}
			if(b)
				continue;
			
			ArrayList<Integer> nArr = new ArrayList<Integer>();
			for(int j = i; j < ss.length; j++) {
				if(sortString(ss[i]).equals(sortString(ss[j]))) {
					nArr.add(j);
				}
			}
			
			arr.add(nArr);
		}
		
		// 2. generate new array
		String[] newArr = new String[ss.length];
		int i = 0;
		for(ArrayList<Integer> iArr : arr) {
			for(Integer idx: iArr) {
				newArr[i++] = ss[idx];
			}
		}
		
		return newArr;
	}
	
	private static String sortString(String s) {
		char[] arr = s.toCharArray();
		Arrays.sort(arr);
		return new String(arr);
	}
	
	public static void merge(int[] a, int[] b, int n, int m) {
		int k = m + n - 1;
		int i = n - 1;
		int j = m - 1;
		
		while(i >= 0 && j >= 0) {
			if(a[i] > b[j])
				a[k--] = a[i--];
			else
				a[k--] = b[j--];
		}
		
		while(j >= 0) {
			a[k--] = b[j--];
		}
	}
	
	private static void sortMerge(int[] a, int[] b) {
		for(int i = 0; i < b.length; i++) {
			// 1. get a value of b at an index i
			int tgt = b[i];
			
			// 2. get the position at which tgt is placed
			int p = 0;
			int aLen = a.length;
			boolean bo = false;
			for(int j = 0; j < a.length; j++) {
				if (a[j] > tgt && !bo) {
					p = j;
					bo = true;
				}
				
				if(a[j] == -1) {
					aLen = j;
					break;
				}
			}
			
			// 3. push values after the position p
			for(int j = aLen; j > p; j--) {
				a[j] = a[j-1];
			}
			
			a[p] = tgt;
		}
	}
	
	private static boolean check(int row) {
		for(int i = 0; i < row; i++) {
			int diff = Math.abs(columnForRow[i] - columnForRow[row]);
			if (diff == 0 || diff == row -i) return false;
		}
		return true;
	}
	
	public static void PlaceQueen(int row) {
		if (row == 8) {
			printBoard();
			return;
		}
		for(int i = 0; i < 8; i ++) {
			columnForRow[row] = i;
			if(check(row))
				PlaceQueen(row + 1);
		}
	}
	
	private static void printBoard() {
		for(int i = 0; i < 8; i++) {
			System.out.print("(" + (i+1) + ", " + (columnForRow[i]+1) + ") ");
		}
		System.out.println();
	}
	
	public static int makeChange(int n) {
		return makeChange(n, 25);
	}
	
	public static int makeChange(int n, int denom) {
		int next_denom = 0;
		switch (denom) {
		case 25:
			next_denom = 10;
			break;
		case 10:
			next_denom = 5;
			break;
		case 5:
			next_denom = 1;
			break;
		case 1:
			return 1;
		}
		
		int ways = 0;
		for(int i = 0; i * denom <= n; i++)
			ways += makeChange(n - i * denom, next_denom);
		return ways;
	}
	
	public static int getNways(int cents) {
		int[] denom = { 25, 10, 5, 1};
		return getNways_inner(denom, 0, cents);
	}
	
	private static int getNways_inner(int[] denom, int n, int cents) {
		if(denom[n] == 1)
			return 1;
		
		int ways = 0;
		for(int i = 0; i <= cents/denom[n]; i++) {
			ways += getNways_inner(denom, n+1, cents - i * denom[n]);
		}
		return ways;
	}
	
	public static void paint_fill(int[][] image, int x, int y, int nColor) {
		paint_fill_inner(image, x, y, image[x][y], nColor);
	}
	
	public static void paint_fill_inner(int[][] image, int x, int y, int oColor, int nColor) {
		if (x < 0 || y < 0 || x >= image.length || y >= image[0].length)
			return;
		
		if(image[x][y] == oColor) {
			image[x][y] = nColor;
			paint_fill_inner(image, x - 1, y, oColor, nColor);
			paint_fill_inner(image, x + 1, y, oColor, nColor);
			paint_fill_inner(image, x, y - 1, oColor, nColor);
			paint_fill_inner(image, x, y + 1, oColor, nColor);
		}
	}
	
	public static void paint_fill(int[][] image, final int xLen, final int yLen, int x, int y, int newColor, ArrayList<Pair<Integer, Integer>> processed) {
		int oldColor = image[x][y];
		
		// 1. Set to new color
		image[x][y] = newColor;
		
		// 2. Add four adjacent points to the list 'processed' when (1) an adjacent point is valid, (2) the point is not in the list, and (3) the color of adjacent point is same with that of the current one. 
		boolean b1 = false;
		boolean b2 = false;
		boolean b3 = false;
		boolean b4 = false;
		
		if ( x > 0 ) {
			if(!isInList(processed, x - 1, y)) {
				if (image[x-1][y] == oldColor) {
					processed.add(new Pair<Integer, Integer>(x - 1, y));
					b1 = true;
				}
			}
		}
		
		if ( y > 0 ) {
			if(!isInList(processed, x, y - 1)) {
				if (image[x][y-1] == oldColor) {
					processed.add(new Pair<Integer, Integer>(x, y - 1));
					b2 = true;
				}
			}
		}
		
		if ( x < xLen - 1 ) {
			if(!isInList(processed, x + 1, y)) {
				if (image[x+1][y] == oldColor) {
					processed.add(new Pair<Integer, Integer>(x + 1, y));
					b3 = true;
				}
			}
		}
		
		if ( y < yLen - 1 ) {
			if(!isInList(processed, x, y + 1)) {
				if (image[x][y+1] == oldColor) {
					processed.add(new Pair<Integer, Integer>(x, y + 1));
					b4 = true;
				}
			}
		}
						
		// 3. call recursively for the adjacent points
		if(b1) paint_fill(image, xLen, yLen, x - 1, y, newColor, processed);
		if(b2) paint_fill(image, xLen, yLen, x, y - 1, newColor, processed);
		if(b3) paint_fill(image, xLen, yLen, x + 1, y, newColor, processed);
		if(b4) paint_fill(image, xLen, yLen, x, y + 1, newColor, processed);
		
	}
	
	private static boolean isInList(ArrayList<Pair<Integer,Integer>> list, int x, int y) {
		for(Pair<Integer, Integer> p : list) {
			if (p.getKey() == x && p.getValue() == y)
				return true;
		}
		
		return false;
	}
	
	private static ArrayList<LinkedList<MyTreeNode>> getLinkedListsForSameDepth (MyTreeNode root) {
		// 1. get max depth
		int maxDepth = getMaxDepth(root);
		
		// 2. allocate an array of list with the max depth
		ArrayList<LinkedList<MyTreeNode>> list = new ArrayList<LinkedList<MyTreeNode>>();
		for(int i = 0; i < maxDepth; i++)
			list.add(new LinkedList<MyTreeNode>());
		
		// 3. traverse graph
		traverse(list, root, 0);
		
		return list;
	}
	
	private static void traverse(ArrayList<LinkedList<MyTreeNode>> list, MyTreeNode root, int depth) {
		// 1. add the node to the linked list
		list.get(depth).add(root);
		
		// 2. handle the left
		if(root.left != null)
			traverse(list, root.left, depth + 1);
		
		// 3. handle the right
		if(root.right != null)
			traverse(list, root.right, depth + 1);
	}
	
	private static int getMaxDepth(MyTreeNode root) {
		int leftMax = root.left == null? 0 : getMaxDepth(root.left);
		int rightMax = root.right == null? 0 : getMaxDepth(root.right);
		
		return 1 + Math.max(leftMax, rightMax);
	}
	
	private static int getMinDepth(MyTreeNode root) {
		int leftMin = root.left == null? 0 : getMinDepth(root.left);
		int rightMin = root.right == null? 0 : getMinDepth(root.right);
		
		return 1 + Math.min(leftMin, rightMin);
	}
	
	private static MyTreeNode proc(int[] arr, int s, int e) {
		if(s < 0 || e < 0 || s > e)
			return null;
		
		int mid = (s+e)/2;
		MyTreeNode tn = new MyTreeNode();
		tn.v = arr[mid];
		tn.left = proc(arr, s, mid -1);
		tn.right = proc(arr, mid+1, e);
		return tn;
	}
	
	private static boolean route(MyNode start, MyNode end) {
		List<MyNode> q = new ArrayList<MyNode>();
		q.add(start);
		
		while(!q.isEmpty()) {
			MyNode curr = q.remove(0);
			
			if(curr == null)
				continue;
			
			for(MyNode n : curr.getAdjacent()) {
				if(n.getState() == STATE.UnVisited) {
					if(n == end)
						return true;
					
					n.state = STATE.Visiting;
					q.add(n);
				}
			}
			curr.state = STATE.Visited;
		}
		
		return false;
	}
}

class HtWt implements Comparable<HtWt>{
	int Ht;
	int Wt;
	
	HtWt (int h, int w){
		this.Ht = h;
		this.Wt = w;
	}
	
	public int compareTo(HtWt other) {
		if(other.Ht == this.Ht)
			return this.Wt - other.Wt;
		else
			return this.Ht - other.Ht;
	}
	
	@Override
	public String toString() {
		return this.Ht + ", " + this.Wt;
	}
}

class myPos{
	int row;
	int col;
	
	public myPos(int r, int c){
		this.row = r;
		this.col = c;
	}
}

class MyTreeNode {
	int v;
	MyTreeNode left = null, right = null;
}

enum STATE { UnVisited, Visited, Visiting}

class MyNode {
	List<MyNode> adjacent = new ArrayList<MyNode>();
	STATE state = STATE.UnVisited;
	int id;
	
	MyNode(int _id) { this.id = _id; }
	public List<MyNode> getAdjacent() { return adjacent; }
	public void addAdjacent(MyNode n) { adjacent.add(n); }
	public STATE getState() { return state; } 
}

class MyGraph {
	List<MyNode> nodes = new ArrayList<MyNode>();
	
	public List<MyNode> getNodes() { return nodes; }
}

class MyQueue<T>{
	Stack<T> s1 = new Stack<T>();
	Stack<T> s2 = new Stack<T>();
	
	public int size() {
		return s1.size() + s2.size();
	}
	
	public void add(T e) {
		s1.push(e);
	}
	
	public T peek() {
		if(!s2.empty()) return s2.peek();
		while(!s1.empty()) s2.push(s1.pop());
		return s2.peek();
	}
	
	public T remove() {
		if(!s2.empty()) return s2.pop();
		while(!s1.empty()) s2.push(s1.pop());
		return s2.pop();
	}
}

class myQueue<T>{
	Stack<T> s1 = new Stack<T>();
	Stack<T> s2 = new Stack<T>();
	
	public void enqueue(T e) {
		s1.push(e);
	}
	
	public T dequeue() {
		while(s1.size() != 0)
			s2.push(s1.pop());
		
		T r = s2.pop();
		
		while(s2.size() != 0)
			s1.push(s2.pop());
		
		return r;
	}
}

class AnagramComparator implements Comparator<String> {
	private static String sortChars(String s) {
		char[] arr = s.toCharArray();
		Arrays.sort(arr);
		return new String(arr);
	}
	public int compare(String s1, String s2) {
		return sortChars(s1).compareTo(sortChars(s2));
	}
}

class Rod {
	Stack<Integer> st = new Stack<Integer>();
	int index;
	public Rod(int idx) {
		this.index = idx;
	}
	
	private int getIdx() { return this.index; }
	
	public void add(Integer i) {
		this.st.push(i);
	}
	
	private void moveTopTo(Rod dest) {
		Integer i = this.st.pop();
		dest.add(i);
		System.out.println("Move " + i + " from " + this.getIdx() + " to " + dest.getIdx());
	}
	
	public void print() {
		System.out.println("ID: " + this.index);
		if(st.size() == 0) {
			System.out.println("None");
			return;
		}
			
		for(Integer i: this.st) {
			System.out.println(i);
		}
	}
	
	public void moveDisks(int n, Rod dest, Rod buf) {
		if(n <= 0)
			return;
		
		this.moveDisks(n-1, buf, dest);
		this.moveTopTo(dest);
		buf.moveDisks(n-1, dest, this);
	}
}

class privateConst{
	int a;
	
	privateConst(){
		
	}
	
	private privateConst(int _a) {
		this.a = _a;
	}
	
	public static privateConst Factory() {
		return new privateConst(10);
	}
}

class childClass extends privateConst{
	int b;
}*/