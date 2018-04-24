package club.kidgames.liquid.liqp

import club.kidgames.liquid.liqp.MinecraftFormatType.Color
import club.kidgames.liquid.liqp.MinecraftFormatType.Style
import com.google.common.base.CaseFormat.LOWER_CAMEL
import com.google.common.base.CaseFormat.LOWER_HYPHEN
import com.google.common.base.CaseFormat.LOWER_UNDERSCORE
import com.google.common.base.CaseFormat.UPPER_CAMEL
import com.google.common.base.Converter

const val FORMAT_CHAR = '\u00A7'

/**
 * We create tag/filters based on these variations in names.
 */
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

/**
 * Marks a format as being either style or color.
 */
enum class MinecraftFormatType {
  Style,
  Color
}

/**
 * Represents a minecraft formatting option, which can be referred to by name.
 */
enum class MinecraftFormat(val format: Char, val type: MinecraftFormatType = Color) {
  NoColor(' ', Color),
  NoStyle(' ', Style),
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

  val isNone: Boolean
    get() {
      return this == NoColor || this == NoStyle
    }

  fun appendTo(builder: StringBuilder): StringBuilder {
    if (!this.isNone) {
      builder.append(this.formatString)
    }
    return builder
  }

  override fun toString() = formatString

  val formatString: String = when {
    isNone -> ""
    else -> "$FORMAT_CHAR$format"
  }

  companion object {
    @JvmStatic
    fun findFormat(input: Any): MinecraftFormat? {
      val key = input.toString().toLowerCase()
      return colorLookupMap[key]
    }

    @JvmStatic
    val MINECRAFT_FORMAT_CHAR = FORMAT_CHAR
  }
}


