package club.kidgames.liquid

import club.kidgames.liquid.plugin.LiquidRuntimeEngine
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.entity.Player
import org.junit.Before
import org.junit.Test
import java.io.File

class LiquidExtensionsTest {
  private var _engine: LiquidRuntimeEngine? = null
  val engine: LiquidRuntimeEngine
    get() = _engine!!

  var player: Player? = null

  @Before
  fun setup() {
    _engine = LiquidRuntimeEngine(
        baseDir = File("./build/plugin/Liquify").apply { mkdirs() },
        configureParser = {
          stripSpacesAroundTags(false)
              .strictVariables(true)
              .stripSingleLine(false)
        },
        configureRenderer = {
          withMaxRenderTimeMillis(Long.MAX_VALUE)
              .withStrictVariables(true)
        })

    player = mock {
      on { displayName } doReturn "bobby"
    }
  }

  @Test
  fun testFilters() {
    val rendered = engine.render("Hello {{ player.displayName | red | bold }} and then some", player = player!!)
    println(rendered.replace('§', '&'))
    assertThat(rendered).isEqualToIgnoringCase("Hello §c§lbobby§r and then some")
  }

  @Test
  fun testFormatTags() {
    // This tests a number of things:
    // * Nested colors/formats always render exactly one color followed by one style, eg.  red->bold->blue "Hello" would render as §5§l (blue+bold)
    // * Extraneous reset/format strings are eliminated from "nestled" blocks (format blocks that contain only a single formatting tag)
    // * Colors are removed from the stack after the tag renders
    val rendered = engine.render("{%red%}{%bold%}{%yellow%}Am I Wry{%endyellow%}? {%green%}{%underline%}No{%endunderline%}{%endgreen%} by {%endbold%}Mew{%endred%} -- Eric", player = player!!)
    println(rendered.replace('§', '&'))
    assertThat(rendered).isEqualToIgnoringCase("§e§lAm I Wry§r§c§l? §a§nNo§r§c§l by §r§cMew§r -- Eric")
  }

  @Test
  fun testTag() {
    val rendered = engine.render("" +
        "{%red%}{{ '(red)' | reset }}A " +
        "{%underline%}{{ '(red+underline)' | reset }} and underlined:  Hello -= " +
        "{{ '(darkBlue+bold)' | reset }} {{ player.displayName | darkBlue | bold }}" +
        "{{ '(red+underline)' | reset }} -= and then " +
        "{% black %}{{ '(black+underline)' | reset }}some black {%endblack%}" +
        "{{ '(red+underline)' | reset }} stuff is{%endunderline%}" +
        "{{ '(red)' | reset }}now red without underline{%endred%}" +
        "{{ '(plain)' | reset }} or plain text", player = player!!)
    println(rendered.replace('§', '&'))

    assertThat(rendered).isEqualToIgnoringCase("§c§r(red)§r§cA §c§r(red+underline)§r§c§n and " +
        "underlined:  Hello -= §c§r(darkBlue+bold)§r§c§n §1§lbobby§r§c§n§c§r(red+underline)§r§c§n " +
        "-= and then §0§r(black+underline)§r§0§nsome black §r§c§n§c§r(red+underline)§r§c§n stuff " +
        "is§r§c§c§r(red)§r§cnow red without underline§r§r(plain)§r or plain text")
  }
}

