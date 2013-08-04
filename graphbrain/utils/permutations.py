def permutations(n):
	perms = 1
	for i in range(2, n + 1):
		perms *= i
	return perms
	
def assign_to_perm_pos(value, str_array, pos):
	j = 0
	for i in range(0, len(str_array)):
		if str_array[i] == None:
			if j == pos:
				str_array[i] = value
				return
			j += 1
	
def get_from_perm_pos(str_array, pos):
	j = 0;
	for i in range(0, len(str_array)):
		if str_array[i] != None:
			if (j == pos):
				res = str_array[i]
				str_array[i] = None
				return res
			j += 1
	# this should not happen
	return None
	
# http://stackoverflow.com/questions/1506078/fast-permutation-number-permutation-mapping-algorithms
def permutation_positions(n, per):
	res = [0 for i in xrange(n)]
	number = per

	# the remaining element always goes to the first free slot
	res[0] = 0
		
	base = 2
	for i in range(1, n):
		res[i] = number % base
		number = number / base
		base += 1
		
	return res
	
def str_array_permutation(arr_in, per):
	n = len(arr_in)
	out = [0 for i in xrange(n)]
	config = permutation_positions(n, per)
	
	for i in range(n - 1, -1, -1):	
		assign_to_perm_pos(arr_in[n - i - 1], out, config[i])
		
	return out
	
def str_array_unpermutate(arr_in, per):
	n = len(arr_in)
	out = [0 for i in xrange(n)]
	config = permutation_positions(n, per)
		
	for i in range(n - 1, -1, -1):
		out[n - i - 1] = get_from_perm_pos(arr_in, config[i])
		
	return out