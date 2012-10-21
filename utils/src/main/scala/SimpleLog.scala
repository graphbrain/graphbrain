package com.graphbrain.utils

import scala.annotation.elidable
import scala.annotation.elidable._


trait SimpleLog {
    def log(msg: String, level: String) = {
        val line = "[" + level + "] " + msg
        println(line) 
    }

    @elidable(ALL) def debug(msg: String) = log(msg, "DEBUG")
    @elidable(INFO) def info(msg: String) = log(msg, "INFO")
    @elidable(WARNING) def warning(msg: String) = log(msg, "WARNING")
    @elidable(SEVERE) def error(msg: String) = log(msg, "ERROR")
}