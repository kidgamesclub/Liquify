package club.kidgames.liquid.extensions

import club.kidgames.liquid.extensions.MinecraftFormatType.*
import com.google.common.base.CaseFormat.LOWER_CAMEL
import com.google.common.base.CaseFormat.LOWER_HYPHEN
import com.google.common.base.CaseFormat.LOWER_UNDERSCORE
import com.google.common.base.CaseFormat.UPPER_CAMEL
import com.google.common.base.Converter
import java.awt.Color

val FORMAT_CHAR = '\u00A7'

val conversions: Set<Converter<String, String>> = setOf(LOWER_HYPHEN, LOWER_CAMEL, LOWER_UNDERSCORE)
    .map { caseFormat -> UPPER_CAMEL.converterTo(caseFormat) }
    .toSet()

val colorLookupMap by lazy {
  val allFormats = mutableMapOf<String, MinecraftFormat>()
  MinecraftFormat.values().forEach { format ->
    allFormats[format.name.toLowerCase()] = format
    allFormats[format.format.toString()] = format
    for (conversion in conversions) {
      val convertedName = conversion.convert(format.name)!!.toLowerCase()
      allFormats[convertedName.toLowerCase()] = format
    }
  }
  allFormats.toMap()
}


enum class MinecraftFormatType {
  Style,Color
}

enum class MinecraftFormat(val format: Char, val type:MinecraftFormatType = Color) {
  Black('0'),
  DarkBlue('1'),
  DarkGreen('2'),
  DarkAqua('3'),
  DarkRed('4'),
  DarkPurple('5'),
  Gold('6'),
  Gray('7'),
  DarkGray('8'),
  Blue('9'),
  Green('a'),
  Aqua('b'),
  Red('c'),
  LightPurple('d'),
  Yellow('e'),
  White('f'),
  Obfuscated('k', Style),
  Bold('l', Style),
  Strikethrough('m', Style),
  Underline('n', Style),
  Italic('o', Style),
  Reset('r', Style);

  override fun toString(): String {
    return format()
  }

  fun format(): String {
    return appendTo().toString()
  }

  fun appendTo(builder: StringBuilder = StringBuilder()): StringBuilder {
    return builder.append(FORMAT_CHAR).append(format)
  }

  companion object {
    @JvmStatic
    fun findFormat(input: Any): MinecraftFormat? {
      val key = input.toString().toLowerCase()
      return colorLookupMap[key]
    }

    @JvmStatic
    fun findFormats(vararg inputs: Any?): Set<MinecraftFormat> {
      val formats = mutableSetOf<MinecraftFormat>()
      inputs
          .filterNotNull()
          .map { input ->
            when (input) {
              is Array<*>-> input
                  .filterNotNull()
                  .forEach { findFormat(it)?.let(formats::add) }
              is Iterable<*> -> input
                  .filterNotNull()
                  .forEach { findFormat(it)?.let(formats::add) }
              else -> findFormat(input)?.let(formats::add)
            }
          }

      return formats
    }
  }
}

