#typedef double vec3[3];

frand = ->
  Math.random() - 0.5


getCoulombEnergy = ->
  e = 0
  N = g.snodeArray.length
  for i in [0..(N - 1)]
    if i < N - 1
      for j in [(i + 1)..(N - 1)]
        e += 1 / v3diffLength(g.snodeArray[i].tpos, g.snodeArray[j].tpos)
  e


getForces = ->
  N = g.snodeArray.length
  r = newv3
  for i in [0..(N - 1)]
    g.snodeArray[i].f[0] = 0
    g.snodeArray[i].f[1] = 0
    g.snodeArray[i].f[2] = 0
  for i in [0..(N - 1)]
    posi = g.snodeArray[i].pos
    if i < N - 1
      for j in [(i + 1)..(N - 1)]
        posj = g.snodeArray[j].pos
        r[0] = posi[0] - posj[0]
        r[1] = posi[1] - posj[1]
        r[2] = posi[2] - posj[2]
        l = v3length(r)
        l = 1 / (l * l * l)
        ff = l * r[0]
        g.snodeArray[i].f[0] += ff
        g.snodeArray[j].f[0] -= ff
        ff = l * r[1]
        g.snodeArray[i].f[1] += ff
        g.snodeArray[j].f[1] -= ff
        ff = l * r[2]
        g.snodeArray[i].f[2] += ff
        g.snodeArray[j].f[2] -= ff


layout = ->
  N = g.snodeArray.length
  Nstep = 1000
  step = 0.01
  minimalStep = 1e-10
  
  for i in [0..(N - 1)]
    g.snodeArray[i].pos[0] = 2 * frand()
    g.snodeArray[i].pos[1] = 2 * frand()
    g.snodeArray[i].pos[2] = 2 * frand()
    l = v3length(g.snodeArray[i].pos)
    if l != 0.0
      g.snodeArray[i].pos[0] /= l
      g.snodeArray[i].pos[1] /= l
      g.snodeArray[i].pos[2] /= l
      g.snodeArray[i].tpos[0] = g.snodeArray[i].pos[0]
      g.snodeArray[i].tpos[1] = g.snodeArray[i].pos[1]
      g.snodeArray[i].tpos[2] = g.snodeArray[i].pos[2]
    else
      i -= 1

  e0 = getCoulombEnergy()
  for k in [0..(Nstep - 1)]
    getForces()
    for i in [0..(N - 1)]
      f = g.snodeArray[i].f
      pos = g.snodeArray[i].pos
      tpos = g.snodeArray[i].tpos
      d = v3dotv3(f, pos)
      f[0] -= pos[0] * d
      f[1] -= pos[1] * d
      f[2] -= pos[2] * d
      tpos[0] = pos[0] + f[0] * step
      tpos[1] = pos[1] + f[1] * step
      tpos[2] = pos[2] + f[2] * step
      l = v3length(tpos)
      tpos[0] /= l
      tpos[1] /= l
      tpos[2] /= l
    e = getCoulombEnergy()
    if e >= e0  # unsuccessfull step
      step /= 2
      if step < minimalStep
        return
    else        # successfull step
      for i in [0..(N - 1)]
        g.snodeArray[i].pos[0] = g.snodeArray[i].tpos[0]
        g.snodeArray[i].pos[1] = g.snodeArray[i].tpos[1]
        g.snodeArray[i].pos[2] = g.snodeArray[i].tpos[2]
      e0 = e
      step *= 2