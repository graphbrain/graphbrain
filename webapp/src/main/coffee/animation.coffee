intervalID = false

stopAnim = false
animSpeedX = 0.007
animSpeedY = 0.005

animCycle = ->
  g.rotateX(-animSpeedX)
  g.rotateY(animSpeedY)
  g.updateView()
  g.updateDetailLevel()

  animSpeedX *= 0.98
  animSpeedY *= 0.98

  if animSpeedX < 0.0001
    stopAnim = true

  if stopAnim
    window.clearInterval(intervalID)


initAnimation = ->
  intervalID = window.setInterval(animCycle, 30)