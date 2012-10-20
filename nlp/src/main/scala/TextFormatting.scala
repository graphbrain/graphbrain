package com.graphbrain.nlp

object TextFormatting {
	def deQuoteAndTrim(text: String): String = text.replace("`", "").replace("'", "").replace("\"", "").replace("\\", "").trim

}