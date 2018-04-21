package club.kidgames.liquid

import club.kidgames.liquid.api.PlaceholderExtender
import club.kidgames.liquid.api.models.LiquidModelMap
import club.kidgames.liquid.plugin.LiquidRuntimeEngine
import com.google.common.collect.ImmutableMap
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.PlaceholderHook
import org.bukkit.entity.Player
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*

class LiquidRuntimeEngineTest {
  var _engine: LiquidRuntimeEngine? = null
  val engine: LiquidRuntimeEngine
    get() = _engine!!

  @Before
  fun setup() {
    PlaceholderAPI.unregisterPlaceholderHook("TestPlugin")

    val placeholders = ArrayList<PlaceholderExtender>()
    placeholders.add(object : PlaceholderExtender("test", "echo") {
      override fun resolvePlaceholder(model: LiquidModelMap): Any? {
        return LiquidModelMap { key,_ -> "echo: $key" }
      }
    })

    _engine = LiquidRuntimeEngine(placeholders = placeholders)
    val playerToMap = { player: Player ->
      ImmutableMap.of<String, Any>("name", player.name, "uniqueId",
          player.uniqueId)
    }
    PlaceholderAPI.registerPlaceholderHook("testplugin", object : PlaceholderHook() {
      override fun onPlaceholderRequest(player: Player, s: String): String? {
        return playerToMap.invoke(player)[s]?.toString()
      }
    })
  }

  @Test
  fun coldBenchmark() {
    val players = ArrayList<Player>()

    record<List<Player>>("Create contacts", {
      var i = 0
      while (i++ < 10000) {
        val player = mock<Player>(Player::class.java)
        `when`<String>(player.getName()).thenReturn("Player $i")
        `when`<UUID>(player.getUniqueId()).thenReturn(UUID.randomUUID())
        players.add(player)
      }
      players
    })

    val placeholder = {
      for (player in players) {
        PlaceholderAPI.setPlaceholders(player, "Hello, world. This is %testplugin_uniqueId% " + "%testplugin_name%")
      }
      true
    }

    record("PlaceholderAPI Cold", placeholder)
    record("PlaceholderAPI Warm", placeholder)
    record("PlaceholderAPI Hot", placeholder)
    record("PlaceholderAPI Final", placeholder)

    val liquidModels = record("Create models", {
      players.map { p -> engine.buildRenderContext({it.player = p}) }
    })

    val liquid = {
      for (player in liquidModels) {
        engine.renderWithContext("Hello, world. This is {{player.uniqueId}} {{player.name}}", player)
      }
    }
    record("Liquid Cold", liquid)
    record("Liquid Warm", liquid)
    record("Liquid Hot", liquid)
    record("Liquid Last", liquid)

    val liquidNoModel = {
      for (player in players) {
        engine.render("Hello, world. This is {{player.uniqueId}} {{player.name}}", player)
      }
    }

    record("Liquid No Model Cold", liquidNoModel)
    record("Liquid No Model Warm", liquidNoModel)
    record("Liquid No Model Hot", liquidNoModel)
    record("Liquid No Model Last", liquidNoModel)
  }

  fun <X> record(msg: String, runnable: () -> X): X {
    val start = System.currentTimeMillis()
    val value = runnable()
    println(msg + ": " + (System.currentTimeMillis() - start) + "ms")
    return value
  }
}
