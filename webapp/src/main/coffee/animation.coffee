intervalID = false
anims = []


addAnim = (anim) ->
  if anim.name == 'lookat'
    anims = anims.filter (anim) -> (anim.name != 'initrotation') and (anim.name != 'lookat')

  if anims.length == 0
    intervalID = window.setInterval(animCycle, 30)

  anims.push(anim)


animCycle = ->
  for a in anims
    a.runCycle()

  anims = anims.filter (anim) -> anim.active

  if anims.length == 0
    window.clearInterval(intervalID)
    intervalID = false


stopAnims = ->
  anims = anims.filter (anim) -> not anim.stoppable

  if anims.length == 0
    window.clearInterval(intervalID)
    intervalID = false



class Animation
  constructor: ->
    @name = ''
    @active = true
    @stoppable = false

  runCycle: ->
    @active = @cycle()



class AnimInitRotation extends Animation
  constructor: ->
    @name = 'initrotation'
    @stoppable = true
    @animSpeedX = 0.007
    @animSpeedY = 0.005
  
  cycle: ->
    g.rotateX(-@animSpeedX)
    g.rotateY(@animSpeedY)
    g.updateView()

    @animSpeedX *= 0.98
    @animSpeedY *= 0.98

    if @animSpeedX < 0.0001
      false
    else
      true



class AnimLookAt extends Animation
  constructor: (targetSNode) ->
    super()
    @name = 'lookat'
    @stoppable = true
    @targetSNode = targetSNode
  
  cycle: ->
    speedFactor = 0.05
    precision = 0.01

    speedX = @targetSNode.angleX * speedFactor
    speedY = @targetSNode.angleY * speedFactor

    g.rotateX(speedY)
    g.rotateY(-speedX)
    g.updateView()

    if (Math.abs(@targetSNode.angleX) < precision) and (Math.abs(@targetSNode.angleY) < precision)
      false
    else
      true



class AnimNodeGlow extends Animation
  constructor: (node) ->
    super()
    @name = 'nodeglow'
    @node = node
    @x = 0
    @cycles = 0.0
    @delta = 0.05

    @r1 = 224.0
    @g1 = 224.0
    @b1 = 224.0

    @r2 = 189.0
    @g2 = 218.0
    @b2 = 249.0
  
  cycle: ->
    @x += @delta
    if (@x > 1)
      @x = 1
      @delta = -@delta
    if (@x < 0)
      @x = 0
      @delta = -@delta
      @cycles += 1

    r = Math.round(@r1 + ((@r2 - @r1) * @x))
    g = Math.round(@g1 + ((@g2 - @g1) * @x))
    b = Math.round(@b1 + ((@b2 - @b1) * @x))

    rgb = 'rgb(' + r + ',' + g + ',' + b + ')'
    $('#' + @node.divid).css({background: rgb})

    if @cycles > 3
      false
    else
      true