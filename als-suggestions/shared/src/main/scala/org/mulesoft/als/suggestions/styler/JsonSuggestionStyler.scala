package org.mulesoft.als.suggestions.styler

import org.mulesoft.als.suggestions.RawSuggestion

case class JsonSuggestionStyler(override val params: StylerParams) extends SuggestionStyler {
  override def style(suggestion: RawSuggestion): Styled = {
    val isJSONObject = (suggestion.newText startsWith "{") && (suggestion.newText endsWith "}")
    var endingQuote  = false
    var postfix      = ""
    var prefix       = ""
    if (suggestion.options.isKey) {
      if (!suggestion.newText.startsWith("\"") && !params.hasKeyClosingQuote)
        prefix = "\""
      if (!params.hasKeyClosingQuote) {
        postfix += "\""
        if (!params.hasColon)
          postfix += ":"
        if (params.supportSnippets)
          if (!params.hasQuote && !suggestion.options.arrayProperty)
            postfix += "\"$1\""
          else if (!params.hasQuote && suggestion.options.arrayProperty)
            postfix += "[ $1 ]"
      } else if (!params.hasQuote && !suggestion.options.arrayProperty && params.supportSnippets) {
        postfix += "\"$1\""
        endingQuote = true
      } else if (!params.hasQuote && suggestion.options.arrayProperty && params.supportSnippets) {
        postfix += "[ $1 ]"
      }
    } else if (!params.hasQuote) {
      postfix += "\""
      endingQuote = true
    }
    val text = prefix + {
      if (!isJSONObject && (!endingQuote || !(suggestion.newText endsWith "\"")))
        suggestion.newText + postfix
      else suggestion.newText
    }
    if (postfix.contains("$1") && params.supportSnippets)
      Styled(text, plain = false)
    else Styled(text, plain = true)
  }

}
