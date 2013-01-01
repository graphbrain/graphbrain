intervalID = false

stopAnim = false
animSpeedX = 0.007
animSpeedY = 0.005

targetSNode = false


lookToSNode = (snode) ->
  targetSNode = snode
  if intervalID == false
    intervalID = window.setInterval(animCycle, 30)
  else
    stopAnim = true


animCycle = ->
  # initial rotation
  if targetSNode == false
    g.rotateX(-animSpeedX)
    g.rotateY(animSpeedY)
    g.updateView()

    animSpeedX *= 0.98
    animSpeedY *= 0.98

    if animSpeedX < 0.0001
      stopAnim = true

    if stopAnim
      window.clearInterval(intervalID)
      intervalID = false
  # bringing a snode to the front
  else
    speedFactor = 0.05
    precision = 0.01

    speedX = targetSNode.angleX * speedFactor
    speedY = targetSNode.angleY * speedFactor

    g.rotateX(speedY)
    g.rotateY(-speedX)
    g.updateView()

    if (Math.abs(targetSNode.angleX) < precision) and (Math.abs(targetSNode.angleY) < precision)
      window.clearInterval(intervalID)
      intervalID = false


initAnimation = ->
  intervalID = window.setInterval(animCycle, 30)