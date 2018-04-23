package club.kidgames.liquid.plugin

import club.kidgames.integrations.LiquifyIntegrator
import club.kidgames.liquid.api.LiquifyRenderer
import club.kidgames.liquid.liquifyTestDir
import club.kidgames.liquid.liquifyTestServerDir
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.plugin.PluginManager
import org.junit.Before
import org.junit.Test

class LiquifyIntegratorTest {

  lateinit var liquify: Liquify
  lateinit var integrator: LiquifyIntegrator
  lateinit var dummyPlugin: PluginInfo

  val renderer: LiquifyRenderer
    get() = liquify.renderer

  @Before
  fun setup() {
    liquify = Liquify(dataFolder = liquifyTestDir, isInitialized = true)
    val (renderer, extenders) = liquify
    val pluginManager = mock<PluginManager>()
    integrator = LiquifyIntegrator(renderer, extenders, pluginManager, liquify.dataFolder, liquify.logger)
    dummyPlugin = PluginInfo("Dummy", liquifyTestServerDir.resolve("plugins/Dummy"))
  }

  @Test
  fun testSnippetInclude() {
    integrator.integrateWith3rdPartyPlugin(dummyPlugin)
    val rendered = renderer.render("{% include 'Dummy/dummy' %}")
    assertThat(rendered).isEqualToIgnoringWhitespace("Hello, dummy §cRED§r")
  }

  @Test
  fun testSnippetInclude2() {
    val integrator = this.integrator
    integrator.integrateWith3rdPartyPlugin(dummyPlugin)
    val rendered = renderer.render("{% include 'Dummy/subfolder/reticle' %}{% include 'Dummy/dummy' %}")
    assertThat(rendered).isEqualToIgnoringWhitespace("Reticulation is most resplendent Hello, dummy §cRED§r")
  }
}
