package club.kidgames.liquid.plugin

import club.kidgames.liquid.liquifyTestDir
import club.kidgames.liquid.liquifyTestServerDir
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LiquifyIntegratorTest {

  val integrator: LiquifyIntegrator
    get() {
      val liquify = Liquify(dataFolder = liquifyTestDir)
      return LiquifyIntegrator(liquify, liquify.logger)
    }

  val dummyPlugin = PluginInfo("Dummy", liquifyTestServerDir.resolve("plugins/Dummy"))

  @Test
  fun testSnippetInclude() {
    val integrator = this.integrator
    integrator.extractSnippets(dummyPlugin)
    val renderer = integrator.liquify.renderer
    val rendered = renderer.render("{% include 'Dummy/dummy' %}")
    assertThat(rendered).isEqualToIgnoringWhitespace("Hello, dummy")
  }

  @Test
  fun testSnippetInclude2() {
    val integrator = this.integrator
    integrator.extractSnippets(dummyPlugin)
    val renderer = integrator.liquify.renderer
    val rendered = renderer.render("{% include 'Dummy/subfolder/reticle' %}{% include 'Dummy/dummy' %}")
    assertThat(rendered).isEqualToIgnoringWhitespace("Hello, dummy")
  }
}
