package com.graphbrain.nlp

object TextFormatting {
	
	def deQuoteAndTrim(text: String): String = text.replace("\"", "").trim
	def clean(text: String): String = text.replace("`", "").replace("'", "").replace("\\", "").trim

	
}