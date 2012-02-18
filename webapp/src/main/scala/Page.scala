package com.graphbrain.webapp

import unfiltered.response._
import java.io.Writer

abstract class Page extends ResponseWriter {
	def html: scala.xml.NodeSeq
	def write(writer: Writer) {
		val content = """<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">""" + "\n" + html
		writer.write(content)
	} 
}