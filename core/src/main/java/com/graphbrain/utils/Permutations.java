package com.graphbrain.utils;


public class Permutations {
	
	public static int permutations(int n) {
		int perms = 1;
		for (int i = 2 ; i < n; i++) {
			perms *= i;
		}

	    return perms;
	}
	
	public static void assignToPermPos(String value, String[] strArray, int pos) {
		int j = 0;
		for (int i = 0; i < strArray.length; i++) {
			if (strArray[i] == null) {
				if (j == pos) {
					strArray[i] = value;
					return;
				}
				j += 1;
			}
		}
	}
	
	public static String getFromPermPos(String[] strArray, int pos) {
        int j = 0;
        for (int i = 0; i < strArray.length; i++) {
			if (strArray[i] != null) {
				if (j == pos) {
					String res = strArray[i];
					strArray[i] = null;
					return res;
				}
				j += 1;
			}
		}
		// this should not happen
		return null;
	}
	
	// http://stackoverflow.com/questions/1506078/fast-permutation-number-permutation-mapping-algorithms
	public static int[] permutationPositions(int n, int per) {
	    int[] res = new int[n];
		int number = per;

		// the remaining element always goes to the first free slot
		res[0] = 0;
		
		int base = 2;
		for (int i = 1; i < n; i++) {
	        res[i] = number % base;
		    number = number / base;
		    base += 1;
		}
		
		return res;
	}
	
	public static String[] strArrayPermutation(String[] in, int per) {
		int n = in.length;
		String[] out = new String[n];
		int[] config = permutationPositions(n, per);

        for (int i = n - 1; i>= 0; i--) {
			assignToPermPos(in[n - i - 1], out, config[i]);
		}
		
		return out;
	}
	
	public static String[] strArrayUnpermutate(String[] in, int per) {
		int n = in.length;
		String[] out = new String[n];
		int[] config = permutationPositions(n, per);

        for (int i = n - 1; i >= 0; i--) {
			out[n - i - 1] = getFromPermPos(in, config[i]);
		}
		
		return out;
	}
}