package com.graphbrain.core

import scala.annotation.elidable
import scala.annotation.elidable._


trait SimpleLog {
    var simpleLogOn = true

    private def slog(msg: String, level: String, color: String=Console.WHITE) = {
        if (simpleLogOn) {
          val caller = (new Exception).getStackTrace()(3)
          val line = Console.RED + "[" + level + "] " + Console.GREEN + caller + " " + color + msg + Console.WHITE
          Console.out.println(line)
        } 
    }

    @elidable(ALL) def ldebug(msg: String, color: String=Console.WHITE) = slog(msg, "DEBUG", color)
    @elidable(INFO) def linfo(msg: String, color: String=Console.WHITE) = slog(msg, "INFO", color)
    @elidable(WARNING) def lwarn(msg: String, color: String=Console.WHITE) = slog(msg, "WARNING", color)
    @elidable(SEVERE) def lerror(msg: String, color: String=Console.WHITE) = slog(msg, "ERROR", color)
}