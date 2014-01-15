package com.ncc.neon

object Headers {

  val json_header = Map(
    "Cache-Control" -> """no-cache""",
    "Content-Type" -> """application/json; charset=UTF-8""",
    "Pragma" -> """no-cache""",
    "X-Requested-With" -> """XMLHttpRequest"""
  )

  val connect_header = Map(
    "Accept" -> """*/*""",
    "Cache-Control" -> """no-cache""",
    "Content-Type" -> """application/x-www-form-urlencoded; charset=UTF-8""",
    "Pragma" -> """no-cache""",
    "X-Requested-With" -> """XMLHttpRequest"""
  )

}