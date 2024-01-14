package com.hydra.server.galaxy

case class Position(x: Int, y: Int) {
  def directionTo(other: Position): Position = {
    if (other.equals(this)) {
      Position(0, 0)
    } else {
      val dx = other.x - x
      val dy = other.y - y
      val length = math.sqrt(dx * dx + dy * dy).toDouble

      // Calculate the direction vector
      var dirX = if (dx >= 0) 1 else -1
      var dirY = if (dy >= 0) 1 else -1

      // Normalize the direction vector
      if (length != 1) {
        dirX = Math.round((dx / length)).toInt
        dirY = Math.round((dy / length)).toInt
      }

      // Ensure the direction vector points in the correct direction
      if ((dirX > 0 && dx < 0) || (dirX < 0 && dx > 0)) dirX = -dirX
      if ((dirY > 0 && dy < 0) || (dirY < 0 && dy > 0)) dirY = -dirY

      Position(dirX, dirY)
    }
  }

  def add(direction: Position): Position = {
    Position(x + direction.x, y + direction.y)
  }
}
