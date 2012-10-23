package com.graphbrain.utils

import scala.annotation.elidable
import scala.annotation.elidable._


trait SimpleLog {
    private def slog(msg: String, level: String) = {
        val caller = (new Exception).getStackTrace()(3)
        val line = "[" + level + "] " + caller + " " + msg
        println(line) 
    }

    @elidable(ALL) def ldebug(msg: String) = slog(msg, "DEBUG")
    @elidable(INFO) def linfo(msg: String) = slog(msg, "INFO")
    @elidable(WARNING) def lwarn(msg: String) = slog(msg, "WARNING")
    @elidable(SEVERE) def lerror(msg: String) = slog(msg, "ERROR")
}