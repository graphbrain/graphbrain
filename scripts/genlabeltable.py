f = open('edgetypes.csv')
lines = f.readlines()
f.close()

code = 'package com.graphbrain.webapp\n\n'

code += 'object EdgeLabelTable {\n'

code += ' val table = Map('

first = True
for l in lines:
  if first:
    first = False
  else:
    code += ', '

  tokens = l.split(',')
  key = tokens[0].strip()
  value = tokens[1].strip()
  code += '"' + key + '" -> "' + value + '"'

code += ')\n'

code += '}\n'

print(code)