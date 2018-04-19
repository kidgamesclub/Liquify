package club.kidgames.liquid

import club.kidgames.liquid.extensions.MinecraftFormatTag
import club.kidgames.liquid.plugin.LiquidRuntimeEngine
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.entity.Player
import org.junit.Before
import org.junit.Test

class LiquidExtensionsTest {
  var _engine: LiquidRuntimeEngine? = null
  val engine: LiquidRuntimeEngine
    get() = _engine!!

  var player: Player? = null

  @Before
  fun setup() {
    _engine = LiquidRuntimeEngine(
        configureTemplateFactory = {
          stripSpacesAroundTags(false)
              .strictVariables(true)
              .stripSingleLine(true)
        },
        configureRenderSettings = {
          maxRenderTimeMillis(Long.MAX_VALUE)
              .strictVariables(true)
        }, tags = listOf(MinecraftFormatTag()))

    player = mock {
      on { getDisplayName() } doReturn "bobby"
      on { displayName } doReturn "bobby"
    }
  }

  @Test
  fun testFormats() {

    val rendered = engine.render("Hello {{ player.displayName | red | bold }} and then some", player = player!!)
    println(rendered.replace('§', '&'))
    assertThat(rendered).isEqualToIgnoringCase("Hello §l§cbobby§r and then some")
  }

  @Test
  fun testTag() {
    val rendered = engine.render("" +
        "{%red%}{{ '(red)' | reset }}A " +
        "{%underline%}{{ '(red+underline)' | reset }} and underlined:  Hello -= " +
        "{{ '(darkBlue+bold)' | reset }} {{ player.displayName | bold | darkBlue }}" +
        "{{ '(red+underline)' | reset }} -= and then " +
        "{% black %}{{ '(black+underline)' | reset }}some black {%endblack%}" +
        "{{ '(red+underline)' | reset }} stuff is{%endunderline%}" +
        "{{ '(red)' | reset }}now red without underline{%endred%}" +
        "{{ '(plain)' | reset }} or plain text", player = player!!)
    println(rendered.replace('§', '&'))

    assertThat(rendered).isEqualToIgnoringCase("" +
        "§c§r(red)§r§cA " +
        "§n§r(red+underline)§r§c§n and underlined:  Hello -= " +
        "§r(darkBlue+bold)§r§c§n §1§lbobby§r§c§n§r§c§n§r" +
        "(red+underline)§r§c§n -= and then " +
        "§0§r(black+underline)§r§0§nsome black " +
        "§r§c§n§r(red+underline)§r§c§n stuff is" +
        "§r§c§r(red)§r§cnow red without underline§r§r" +
        "(plain)§r or plain text")
  }
}

